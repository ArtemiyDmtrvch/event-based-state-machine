package ru.impression.flow_architecture

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.ConcurrentLinkedQueue

interface FlowPerformer<F : Flow> {

    val flowClass: Class<F>

    var flowHashCode: Int?

    val eventEnrichers: Array<FlowPerformer<F>> get() = emptyArray()

    var isActive: Boolean
        get() = false
        set(value) {
            getFlow(flowHashCode!!)?.apply {
                if (value)
                    missedActions.remove(hashCode())?.forEach { performAction(it) }
                else
                    missedActions[hashCode()] = ConcurrentLinkedQueue()
            }
        }

    fun attachToFlow() {
        (flowHashCode
            ?.let { getFlow(it) }
            ?: WAITING_FLOWS.firstOrNull { it.javaClass == flowClass }?.also { WAITING_FLOWS.remove(it) }
            ?: FlowManager.startFlow(flowClass).also { flowHashCode = it.hashCode() })
            .apply {
                performerDisposables.remove(javaClass.notNullName)?.dispose()
                actionSubject
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ performAction(it) }) { throw it }
                    .let { performerDisposables.put(javaClass.notNullName, it) }
            }
    }

    fun eventOccurred(event: Event) {
        eventEnrichers.forEach { it.enrichEvent(event) }
        getFlow(flowHashCode!!)?.eventSubject?.onNext(event)
    }

    fun enrichEvent(event: Event) = Unit

    fun performAction(action: Action) = Unit

    fun detachFromFlow() {
        getFlow(flowHashCode!!)
            ?.apply {
                performerDisposables.remove(javaClass.notNullName)?.dispose()
            }
            ?.also {
                FlowManager.stopFlowIfNeeded(it)
            }
    }
}