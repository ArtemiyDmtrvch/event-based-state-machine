package ru.impression.flow_architecture

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

interface FlowPerformer<F : Flow> {

    val groupUUID: UUID

    val flow: F get() = FlowStore[groupUUID]!!

    var underlay: FlowPerformerUnderlay?
        get() = flow.performerUnderlays[javaClass.notNullName]
        set(value) {
            value
                ?.let { flow.performerUnderlays[javaClass.notNullName] = it }
                ?: flow.performerUnderlays.remove(javaClass.notNullName)
        }

    val eventEnrichers: Array<FlowPerformer<F>> get() = emptyArray()

    fun attachToFlow() = attachToFlow(AttachmentType.NORMAL_ATTACHMENT)

    fun attachToFlow(attachmentType: AttachmentType) {
        (underlay?.apply {
            if (!isTemporarilyDetached) return
            isTemporarilyDetached = false
        } ?: FlowPerformerUnderlay().also { underlay = it }).apply {
            if (attachmentType == AttachmentType.REPLAY_ATTACHMENT) flow.replay()
            disposable = flow.actionSubject
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    performAction(it)
                    numberOfUnperformedActions--
                    if (numberOfUnperformedActions == 0) onAllActionsPerformed()
                }) { throw  it }
        }
    }

    fun eventOccurred(event: Event) {
        eventEnrichers.forEach { it.enrichEvent(event) }
        flow.eventOccurred(event)
    }

    fun enrichEvent(event: Event) = Unit

    fun performAction(action: Action) = Unit

    fun onAllActionsPerformed() = Unit

    fun performMissedActions() {
        underlay?.apply {
            missedActions?.forEach { performAction(it) }
            missedActions = null
        }
    }

    fun temporarilyDetachFromFlow(cacheMissedActions: Boolean) {
        underlay?.apply {
            if (isTemporarilyDetached) return
            disposable?.dispose()
            if (cacheMissedActions) missedActions = ConcurrentLinkedQueue()
            isTemporarilyDetached = true
        }
    }

    fun completelyDetachFromFlow() {
        underlay?.disposable?.dispose() ?: return
        underlay = null
        flow.onPerformerCompletelyDetached()
    }

    enum class AttachmentType {
        NORMAL_ATTACHMENT,
        REPLAY_ATTACHMENT
    }
}