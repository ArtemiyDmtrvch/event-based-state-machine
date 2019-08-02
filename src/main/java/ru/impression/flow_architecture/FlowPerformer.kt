package ru.impression.flow_architecture

import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

interface FlowPerformer<F : Flow, U : FlowPerformer.Underlay> {

    val groupUUID: UUID

    val flow: F get() = FlowStore[groupUUID]!!

    var underlay: U?
        get() = flow.performerUnderlays[javaClass.notNullName] as U?
        set(value) {
            value
                ?.let { flow.performerUnderlays[javaClass.notNullName] = it }
                ?: flow.performerUnderlays.remove(javaClass.notNullName)
        }

    val observingScheduler get() = Schedulers.single()

    var disposable: Disposable?
        get() = null
        set(_) {}

    val initialAction get() = flow.initialAction

    fun groundStateIsSet() {
        performMissedActions()
    }

    fun eventOccurred(event: Event) {
        flow.eventOccurred(event)
    }

    fun enrichEvent(event: Event) = Unit

    fun performAction(action: Action)

    fun onInitialActionPerformed() {
        if (this is PrimaryFlowPerformer<F, U>) flow.onPrimaryInitializationCompleted()
    }

    fun onAllActionsPerformed() = Unit

    fun performMissedActions() {
        underlay?.apply {
            while (true) missedActions?.poll()?.let { performAction(it) } ?: break
            missedActions = null
        }
    }

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

    enum class AttachmentType {
        NORMAL_ATTACHMENT,
        REPLAY_ATTACHMENT
    }
}

inline fun <F : Flow, reified U : FlowPerformer.Underlay> FlowPerformer<F, U>.attachToFlow(
    attachmentType: FlowPerformer.AttachmentType = FlowPerformer.AttachmentType.NORMAL_ATTACHMENT
) {
    var isAttached = false
    underlay
        ?.apply {
            if (!performerIsTemporarilyDetached.get()) return
            performerIsTemporarilyDetached.set(false)
        }
        ?: run { underlay = U::class.java.newInstance() }
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