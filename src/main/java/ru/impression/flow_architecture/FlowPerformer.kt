package ru.impression.flow_architecture

import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.impression.flow_architecture.mvvm_impl.FlowView
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

    val eventEnrichers: Array<FlowPerformer<F, U>> get() = emptyArray()

    fun groundStateIsSet() {
        performMissedActions()
    }

    fun eventOccurred(event: Event) {
        eventEnrichers.forEach { it.enrichEvent(event) }
        flow.eventOccurred(event)
    }

    fun enrichEvent(event: Event) = Unit

    fun performAction(action: Action)

    fun allActionsArePerformed() = Unit

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
    val isPrimaryPerformer = this is PrimaryFlowPerformer<F, U>
    underlay
        ?.apply {
            if (!performerIsTemporarilyDetached.get()) return
            performerIsTemporarilyDetached.set(false)
        }
        ?: run {
            underlay = U::class.java.newInstance()
            if (isPrimaryPerformer) flow.isInitialized.set(false)
        }
    if (attachmentType == FlowPerformer.AttachmentType.REPLAY_ATTACHMENT) {
        if (isPrimaryPerformer) flow.isInitialized.set(false)
        flow.replay()
    } else if (!flow.actionSubject.hasValue())
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
                if (numberOfUnperformedActions.decrementAndGet() == 0) allActionsArePerformed()
                if (action === initialAction) {
                    if (isPrimaryPerformer) flow.initializationCompleted()
                    if (this is FlowView.Underlay) viewIsDestroyed.set(false)
                }
            }
        }) { throw  it }
}