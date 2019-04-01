package ru.impression.flow_architecture

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.ConcurrentHashMap

interface FlowPerformer<F : Flow> {

    val flowClass: Class<F>

    val eventEnrichers: List<FlowPerformer<F>> get() = emptyList()

    fun attachToFlow() {
        val flowName = flowClass.notNullName
        val thisName = javaClass.notNullName
        if (!FLOW_PERFORMER_DISPOSABLES.containsKey(flowName)) {
            FLOW_PERFORMER_DISPOSABLES[flowName] = ConcurrentHashMap()
            FLOW_DISPOSABLES[flowName] = CompositeDisposable()
            EVENT_SUBJECTS[flowName] = BehaviorSubject.create()
            ACTION_SUBJECTS[flowName] = BehaviorSubject.create()
            flowClass.newInstance()
        } else {
            FLOW_PERFORMER_DISPOSABLES[flowName]?.get(thisName)?.dispose()
        }
        ACTION_SUBJECTS[flowName]?.let { actionSubject ->
            actionSubject
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ performAction(it) }) { throw it }
                .let { FLOW_PERFORMER_DISPOSABLES[flowName]?.put(thisName, it) }
        }
    }

    fun eventOccurred(event: Flow.Event) {
        val flowName = flowClass.notNullName
        eventEnrichers.forEach { it.enrichEvent(event) }
        EVENT_SUBJECTS[flowName]?.onNext(event)
    }

    fun enrichEvent(event: Flow.Event) = Unit

    fun performAction(action: Flow.Action) = Unit

    fun detachFromFlow() {
        val flowName = flowClass.notNullName
        val thisName = javaClass.notNullName
        FLOW_PERFORMER_DISPOSABLES[flowName]?.remove(thisName)?.dispose()
        if (FLOW_PERFORMER_DISPOSABLES[flowName]?.isEmpty() != false) {
            FLOW_PERFORMER_DISPOSABLES.remove(flowName)
            FLOW_DISPOSABLES.remove(flowName)?.dispose()
            EVENT_SUBJECTS.remove(flowName)
            ACTION_SUBJECTS.remove(flowName)
        }
    }
}