package ru.impression.flow_architecture

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.ConcurrentLinkedQueue

interface FlowPerformer<F : Flow> {

    val flowHost: FlowHost<F>

    val eventEnrichers: Array<FlowPerformer<F>> get() = emptyArray()

    fun attachToFlow() = attachToFlow(AttachmentType.NORMAL_ATTACHMENT)

    fun attachToFlow(attachmentType: AttachmentType) {
        val thisName = javaClass.notNullName
        if (flowHost.flow.performerDisposables.containsKey(thisName)) return
        if (attachmentType == AttachmentType.REPLAY_ATTACHMENT) flowHost.flow.actionSubject.cleanupBuffer()
        flowHost.flow.performerDisposables[thisName] = flowHost.flow.actionSubject
            .subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ performAction(it) }) { throw  it }
        if (attachmentType == AttachmentType.REPLAY_ATTACHMENT) flowHost.flow.replay()
    }

    fun eventOccurred(event: Event) {
        eventEnrichers.forEach { it.enrichEvent(event) }
        flowHost.flow.eventOccurred(event)
    }

    fun enrichEvent(event: Event) = Unit

    fun performAction(action: Action) = Unit

    fun performCachedActions() {
        flowHost.flow.cachedActions.remove(javaClass.notNullName)?.forEach { performAction(it) }
    }

    fun detachFromFlow() = detachFromFlow(false)

    fun detachFromFlow(cacheActions: Boolean) {
        val thisName = javaClass.notNullName
        if (!flowHost.flow.performerDisposables.containsKey(thisName)) return
        flowHost.flow.performerDisposables.remove(thisName)?.dispose()
        if (cacheActions) flowHost.flow.cachedActions[thisName] = ConcurrentLinkedQueue()
        flowHost.flow.onPerformerDetached()
    }
}