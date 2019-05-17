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
                val thisName = javaClass.notNullName
                if (value)
                    missedActions.remove(thisName)?.forEach { performAction(it) }
                else
                    missedActions[thisName] = ConcurrentLinkedQueue()
            }
        }

    fun attachToFlow() {
        val thisName = javaClass.notNullName
        (flowHashCode
            ?.let { getFlow(it) }
            ?: WAITING_FLOWS.firstOrNull { it.javaClass == flowClass }?.also {
                WAITING_FLOWS.remove(it)
                FLOWS.add(it)
            }
            ?: FlowManager.startFlow(flowClass))
            .also { flowHashCode = it.hashCode() }
            .apply {
                performerDisposables.remove(thisName)?.dispose()
                actionSubject
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ performAction(it) }) { throw it }
                    .let { performerDisposables.put(thisName, it) }
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