package ru.impression.flow_architecture

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

interface FlowPerformer<F : Flow> {

    val groupUUID: UUID

    val flow: F get() = FlowStore[groupUUID]!!

    val eventEnrichers: Array<FlowPerformer<F>> get() = emptyArray()

    fun attachToFlow() = attachToFlow(AttachmentType.NORMAL_ATTACHMENT)

    fun attachToFlow(attachmentType: AttachmentType) {
        val thisName = javaClass.notNullName
        if (flow.performerDisposables.containsKey(thisName)) return
        if (attachmentType == AttachmentType.REPLAY_ATTACHMENT) flow.replay()
        flow.performerDisposables[thisName] = flow.actionSubject
            .subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ performAction(it) }) { throw  it }
        flow.temporarilyDetachedPerformers.remove(thisName)
    }

    fun eventOccurred(event: Event) {
        eventEnrichers.forEach { it.enrichEvent(event) }
        flow.eventOccurred(event)
    }

    fun enrichEvent(event: Event) = Unit

    fun performAction(action: Action) = Unit

    fun performCachedActions() {
        flow.cachedActions.remove(javaClass.notNullName)?.forEach { performAction(it) }
    }

    fun temporarilyDetachFromFlow(cacheActions: Boolean) {
        val thisName = javaClass.notNullName
        flow.temporarilyDetachedPerformers.add(thisName)
        if (cacheActions) flow.cachedActions[thisName] = ConcurrentLinkedQueue()
        detachFromFlow()
    }

    fun detachFromFlow() {
        flow.performerDisposables.remove(javaClass.notNullName)?.dispose()
        flow.onPerformerDetached()
    }

    enum class AttachmentType {
        NORMAL_ATTACHMENT,
        REPLAY_ATTACHMENT
    }
}