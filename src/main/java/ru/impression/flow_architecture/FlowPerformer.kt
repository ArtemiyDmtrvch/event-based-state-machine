package ru.impression.flow_architecture

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.ConcurrentLinkedQueue

interface FlowPerformer<F : Flow> : FlowHost {

    val flowClass: Class<F>

    val flowHost: FlowHost

    val eventEnrichers: Array<FlowPerformer<F>> get() = emptyArray()

    fun performCachedActions() {
        flow.cachedActions.remove(javaClass.notNullName)?.forEach { performAction(it) }
    }

    fun attachToFlow() = attachToFlow(this, AttachmentType.NORMAL_ATTACHMENT)

    fun attachToFlow(flowHost: FlowHost, attachmentType: AttachmentType) {

            if (attachmentType == AttachmentType.REPLAY_ATTACHMENT) flow.replay()
            val thisName = javaClass.notNullName
            flow.performerDisposables.remove(thisName)?.dispose()
            flow.performerDisposables[thisName] = flow.actionSubject
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ performAction(it) }) { throw  it }
    }

    fun eventOccurred(event: Event) {
        eventEnrichers.forEach { it.enrichEvent(event) }
        flow?.eventOccurred(event)
    }

    fun enrichEvent(event: Event) = Unit

    fun performAction(action: Action) = Unit

    fun detachFromFlow() = detachFromFlow(false)

    fun detachFromFlow(cacheActions: Boolean) {
        flow?.let { flow ->
            val thisName = javaClass.notNullName
            flow.performerDisposables.remove(thisName)?.dispose()
            if (cacheActions) flow.cachedActions[thisName] = ConcurrentLinkedQueue()
            flow.onPerformerDetached()
        }
        flow = null
    }
}