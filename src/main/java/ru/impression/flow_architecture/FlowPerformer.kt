package ru.impression.flow_architecture

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

interface FlowPerformer<F : Flow> {

    val flowHost: FlowHost<F>

    val eventEnrichers: Array<FlowPerformer<F>> get() = emptyArray()

    fun attachToFlow() = attachToFlow(AttachmentType.NORMAL_ATTACHMENT)

    fun attachToFlow(attachmentType: AttachmentType) {
        val thisName = javaClass.notNullName
        if (flowHost.flow.performerDisposables.containsKey(thisName)) return
        if (attachmentType == AttachmentType.REPLAY_ATTACHMENT) flowHost.flow.replay()
        flowHost.flow.performerDisposables[thisName] = flowHost.flow.actionSubject
            .subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ performAction(it) }) { throw  it }
        flowHost.flow.temporarilyDetachedPerformers.remove(thisName)
        flowHost.flow.onPerformerAttached()
    }

    fun eventOccurred(event: Event) {
        eventEnrichers.forEach { it.enrichEvent(event) }
        flowHost.flow.eventOccurred(event)
    }

    fun enrichEvent(event: Event) = Unit

    fun performAction(action: Action) = Unit

    fun temporarilyDetachFromFlow() {
        flowHost.flow.temporarilyDetachedPerformers.add(javaClass.notNullName)
        detachFromFlow()
    }

    fun detachFromFlow() {
        val thisName = javaClass.notNullName
        if (!flowHost.flow.performerDisposables.containsKey(thisName)) return
        flowHost.flow.performerDisposables.remove(thisName)?.dispose()
        flowHost.flow.onPerformerDetached()
    }

    enum class AttachmentType {
        NORMAL_ATTACHMENT,
        REPLAY_ATTACHMENT
    }
}