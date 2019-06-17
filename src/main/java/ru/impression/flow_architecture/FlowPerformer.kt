package ru.impression.flow_architecture

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

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

    var disposable: Disposable?
        get() = null
        set(_) {}

    val eventEnrichers: Array<FlowPerformer<F, U>> get() = emptyArray()

    fun groundStateIsSet() {
        performMissedActions()
    }

    fun eventOccurred(event: Event) {
        eventEnrichers.forEach { it.enrichEvent(event) }
        flow.eventOccurred(event)
    }

    fun enrichEvent(event: Event) = Unit

    fun performAction(action: Action) = Unit

    fun allActionsArePerformed() = Unit

    fun performMissedActions() {
        underlay?.apply {
            while (true) {
                missedActions?.poll()?.let { performAction(it) } ?: break
            }
            missedActions = null
        }
    }

    fun temporarilyDetachFromFlow(cacheMissedActions: Boolean) {
        underlay?.apply {
            if (performerIsTemporarilyDetached) return
            disposable?.dispose()
            if (cacheMissedActions) missedActions = ConcurrentLinkedQueue()
            performerIsTemporarilyDetached = true
        }
    }

    fun completelyDetachFromFlow() {
        underlay ?: return
        disposable?.dispose()
        underlay = null
        flow.onPerformerCompletelyDetached()
    }

    open class Underlay {
        @PublishedApi
        internal var lastPerformedAction: Action? = null
        @PublishedApi
        internal var numberOfUnperformedActions = 0
        @PublishedApi
        internal var performerIsTemporarilyDetached = false
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
            if (!performerIsTemporarilyDetached) return
            performerIsTemporarilyDetached = false
        }
        ?: run { underlay = U::class.java.newInstance() }
    if (attachmentType == FlowPerformer.AttachmentType.REPLAY_ATTACHMENT)
        flow.replay()
    else if (!flow.actionSubject.hasValue())
        isAttached = true
    disposable = flow.actionSubject
        .subscribeOn(Schedulers.single())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ action ->
            underlay?.apply {
                if (!isAttached) {
                    isAttached = true
                    if (attachmentType != FlowPerformer.AttachmentType.REPLAY_ATTACHMENT) {
                        if (action === lastPerformedAction) return@subscribe
                        missedActions?.remove(action) ?: numberOfUnperformedActions++
                        performMissedActions()
                    }
                }
                performAction(action)
                lastPerformedAction = action
                numberOfUnperformedActions--
                if (numberOfUnperformedActions == 0) allActionsArePerformed()
            }
        }) { throw  it }
}