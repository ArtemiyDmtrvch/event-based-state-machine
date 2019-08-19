package ru.impression.flow_architecture

import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.impression.flow_architecture.FlowPerformer.Underlay
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Component that performs the business logic of the application described in [Flow]. To do this, it uses two main
 * methods: [performAction] and [eventOccurred]. In the scope of one Flow there can be several FlowPerformers
 * (performer group) that work in isolation from each other (don't keep references to each other). However, one
 * FlowPerformer can create another. In this case, it passes the [groupUUID] to the child FlowPerformer.
 * @see PrimaryFlowPerformer
 * @param F - [Flow] performed by this FlowPerformer
 * @param U - [Underlay] of this FlowPerformer
 */
interface FlowPerformer<F : Flow, U : Underlay> {

    /**
     * Identifier of a group of FlowPerformers that are performing the same [Flow].
     */
    val groupUUID: UUID

    /**
     * [Flow] this FlowPerformer is attached to.
     */
    val flow: F get() = FlowStore[groupUUID]!!

    @Suppress("UNCHECKED_CAST")
    var underlay: U?
        get() = flow.performerUnderlays[javaClass.notNullName] as U?
        set(value) {
            value
                ?.let { flow.performerUnderlays[javaClass.notNullName] = it }
                ?: flow.performerUnderlays.remove(javaClass.notNullName)
        }

    /**
     * Defines the thread in which [performAction] method will be called.
     */
    val observingScheduler get() = Schedulers.single()

    var disposable: Disposable?
        get() = null
        set(_) {}

    /**
     * [InitialAction] cached by [Flow] to which this FlowPerformer is attached.
     */
    val initialAction get() = flow.initialAction

    fun onFlowInitializationFailure() = Unit

    /**
     * Called after performing some [Action], when we are convinced that the FlowPerformer has acquired its ground
     * state and is ready to acquire an additional state. The ground state is a state that is acquired in the process of
     * performing the first few actions, before the user interacts (for example, data downloaded from the server and
     * displayed immediately after going to the page). An additional state is a state acquired as a result of
     * interaction with a user (scroll position, text in a text field), as well as missed actions. You should call this
     * method in FlowPerformer that uses [FlowPerformer.AttachmentType.REPLAY_ATTACHMENT].
     */
    fun groundStateIsSet() {
        performMissedActions()
    }

    /**
     * Called every time [Flow] instructs to perform [Action] if current FlowPerformer is attached to Flow. All
     * FlowPerformer logic should be initiated by this method.
     * @see Flow.performAction
     * @see attachToFlow
     * @param action
     */
    fun performAction(action: Action)

    fun onInitialActionPerformed() {
        if (this is PrimaryFlowPerformer<F, U>) flow.onPrimaryPerformerInitializationCompleted()
    }

    fun onAllActionsPerformed() = Unit

    /**
     * Perform the [actions][Action] that [Flow] cached for this FlowPerformer while it was temporarily detached.
     */
    fun performMissedActions() {
        underlay?.apply {
            while (true) missedActions?.poll()?.let { performAction(it) } ?: break
            missedActions = null
        }
    }

    /**
     * Method by which FlowPerformer informs [Flow] that an [Event] has occurred. Call it, for example, in listeners:
     * <pre>`buttonRegister.setOnClickListener { eventOccurred(RegistrationRequested()) }`</pre> or after completion of
     * some action.
     * @param event - Event to be passed to Flow
     */
    fun eventOccurred(event: Event) {
        flow.eventOccurred(event)
    }

    /**
     * Called when FlowPerformer wants to temporarily stop performing [actions][Action] and inform [Flow] about
     * [events][Event] that have occurred. By calling this method, FlowPerformer agrees to
     * [re-attach to Flow][attachToFlow] after some time. NOTE that if, during a temporary detachment, FlowPerformer is
     * destroyed and another instance of FlowPerformer class attaches to Flow, then this instance will be considered
     * temporarily detached.
     * @param cacheMissedActions - if true, then Flow will cache the emitted actions for this FlowPerformer,
     * while it is temporarily detached.
     * @see performMissedActions
     */
    fun temporarilyDetachFromFlow(cacheMissedActions: Boolean) {
        underlay?.apply {
            if (performerIsTemporarilyDetached.get()) return
            disposable?.dispose()
            performerIsTemporarilyDetached.set(true)
            numberOfUnperformedActions.set(0)
            if (cacheMissedActions) missedActions = ConcurrentLinkedQueue()
        }
    }

    fun completelyDetachFromFlow() {
        underlay ?: return
        disposable?.dispose()
        underlay = null
        flow.onPerformerCompletelyDetached()
    }

    /**
     * Some FlowPerformer data stored in [Flow] in case of temporary detachment.
     * @see temporarilyDetachFromFlow
     */
    open class Underlay {
        @Volatile
        @PublishedApi
        internal var lastPerformedAction: Action? = null
        @PublishedApi
        internal val numberOfUnperformedActions = AtomicInteger(0)
        @PublishedApi
        internal val performerIsTemporarilyDetached = AtomicBoolean(false)
        @Volatile
        @PublishedApi
        internal var missedActions: ConcurrentLinkedQueue<Action>? = null
    }

    /**
     * FlowPerformer and [Flow] behavior when attaching.
     * @see attachToFlow
     */
    enum class AttachmentType {
        /**
         * After attaching, FlowPerformer will immediately receive the last [Action] that was emitted by [Flow].
         * @see FlowPerformer.performAction
         * @see Flow.performAction
         */
        NORMAL_ATTACHMENT,

        /**
         * Before attaching, [Flow] will emit to all its FlowPerformers [InitialAction] that it cached, so after
         * attaching the attached FlowPerformer will receive this InitialAction, as well as all other FlowPerformers.
         * As a result, it turns out that the sequence of [actions][Action] and [events][Event] that occurred in the
         * scope of current Flow begins to repeat. This type of attachment is used so that FlowPerformer can regain
         * its ground state after temporary detachment.
         * @see groundStateIsSet
         * @see temporarilyDetachFromFlow
         */
        REPLAY_ATTACHMENT
    }
}

/**
 * Called when [FlowPerformer] is ready to perform [actions][Action] and inform [Flow] about [events][Event] that have
 * occurred.
 * @param F - Flow performed by this FlowPerformer
 * @param U - [Underlay] of this FlowPerformer
 * @param attachmentType - defines the behavior of FlowPerformer and Flow when attaching
 */
inline fun <F : Flow, reified U : Underlay> FlowPerformer<F, U>.attachToFlow(
    attachmentType: FlowPerformer.AttachmentType = FlowPerformer.AttachmentType.NORMAL_ATTACHMENT
) {
    var isAttached = false
    underlay
        ?.apply {
            if (!performerIsTemporarilyDetached.get()) return
            performerIsTemporarilyDetached.set(false)
        }
        ?: run { underlay = U::class.java.newInstance() }
    initialAction ?: onFlowInitializationFailure()
    if (attachmentType == FlowPerformer.AttachmentType.REPLAY_ATTACHMENT)
        flow.replay()
    else if (!flow.actionSubject.hasValue())
        isAttached = true
    disposable = flow.actionSubject
        .subscribeOn(Schedulers.newThread())
        .observeOn(observingScheduler)
        .subscribe({ action ->
            underlay?.apply {
                if (!isAttached) {
                    if (attachmentType != FlowPerformer.AttachmentType.REPLAY_ATTACHMENT) {
                        if (action === lastPerformedAction) return@subscribe
                        missedActions?.remove(action) ?: numberOfUnperformedActions.incrementAndGet()
                        performMissedActions()
                    }
                    isAttached = true
                }
                performAction(action)
                lastPerformedAction = action
                if (action === initialAction) onInitialActionPerformed()
                if (numberOfUnperformedActions.decrementAndGet() == 0) onAllActionsPerformed()
            }
        }) { throw  it }
}