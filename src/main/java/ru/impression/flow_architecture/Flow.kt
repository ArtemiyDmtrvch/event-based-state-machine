package ru.impression.flow_architecture

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.functions.Function4
import io.reactivex.schedulers.Schedulers

abstract class Flow {

    internal lateinit var initiatingAction: InitiatingAction

    fun init() {
        if (initiatingAction is RestorativeInitiatingAction<*>)
            whenEventOccurs<RestoringRequested> {
                performAction((initiatingAction as RestorativeInitiatingAction<*>).apply { stateStore = it.stateStore })
            }
    }

    protected inline fun <reified E : FlowEvent> whenEventOccurs(crossinline onEvent: (E) -> Unit) {
        val thisName = javaClass.notNullName
        EVENT_SUBJECTS[thisName]?.let { eventSubject ->
            eventSubject
                .filter { it is E }
                .map { it as E }
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe({ event -> onEvent(event) }) { throw  it }
                .let { disposable -> FLOW_DISPOSABLES[thisName]?.add(disposable) }
        }
    }

    protected inline fun <reified E1 : FlowEvent, reified E2 : FlowEvent> whenSeriesOfEventsOccur(
        crossinline onSeriesOfEvents: (E1, E2) -> Unit
    ) {
        val thisName = javaClass.notNullName
        EVENT_SUBJECTS[thisName]?.let { eventSubject ->
            Observable
                .zip(
                    eventSubject
                        .filter { it is E1 }
                        .map { it as E1 },
                    eventSubject
                        .filter { it is E2 }
                        .map { it as E2 },
                    BiFunction<E1, E2, Unit> { e1, e2 -> onSeriesOfEvents(e1, e2) }
                )
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .doOnError { throw it }
                .subscribe()
                .let { disposable -> FLOW_DISPOSABLES[thisName]?.add(disposable) }
        }
    }

    protected inline fun <reified E1 : FlowEvent, reified E2 : FlowEvent, reified E3 : FlowEvent> whenSeriesOfEventsOccur(
        crossinline onSeriesOfEvents: (E1, E2, E3) -> Unit
    ) {
        val thisName = javaClass.notNullName
        EVENT_SUBJECTS[thisName]?.let { eventSubject ->
            Observable
                .zip(
                    eventSubject
                        .filter { it is E1 }
                        .map { it as E1 },
                    eventSubject
                        .filter { it is E2 }
                        .map { it as E2 },
                    eventSubject
                        .filter { it is E3 }
                        .map { it as E3 },
                    Function3<E1, E2, E3, Unit> { e1, e2, e3 -> onSeriesOfEvents(e1, e2, e3) }
                )
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .doOnError { throw it }
                .subscribe()
                .let { disposable -> FLOW_DISPOSABLES[thisName]?.add(disposable) }
        }
    }

    protected inline fun <reified E1 : FlowEvent, reified E2 : FlowEvent, reified E3 : FlowEvent, reified E4 : FlowEvent> whenSeriesOfEventsOccur(
        crossinline onSeriesOfEvents: (E1, E2, E3, E4) -> Unit
    ) {
        val thisName = javaClass.notNullName
        EVENT_SUBJECTS[thisName]?.let { eventSubject ->
            Observable
                .zip(
                    eventSubject
                        .filter { it is E1 }
                        .map { it as E1 },
                    eventSubject
                        .filter { it is E2 }
                        .map { it as E2 },
                    eventSubject
                        .filter { it is E3 }
                        .map { it as E3 },
                    eventSubject
                        .filter { it is E4 }
                        .map { it as E4 },
                    Function4<E1, E2, E3, E4, Unit> { e1, e2, e3, e4 -> onSeriesOfEvents(e1, e2, e3, e4) }
                )
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .doOnError { throw it }
                .subscribe()
                .let { disposable -> FLOW_DISPOSABLES[thisName]?.add(disposable) }
        }
    }

    protected fun performAction(action: FlowAction) {
        val thisName = javaClass.notNullName
        ACTION_SUBJECTS[thisName]?.onNext(action)
        if (action is InitiatingAction) {
            FlowManager.startFlowIfNeeded(action.flowClass)
            ACTION_SUBJECTS[action.flowClass.notNullName]?.onNext(action)
        }
    }

}