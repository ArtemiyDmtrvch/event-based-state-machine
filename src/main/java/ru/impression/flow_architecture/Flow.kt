package ru.impression.flow_architecture

import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.functions.Function4
import io.reactivex.schedulers.Schedulers

abstract class Flow {

    protected inline fun <reified E : Event> whenEventOccurs(crossinline onEvent: (E) -> Unit) {
        val thisName = javaClass.notNullName
        EVENT_SUBJECTS[thisName]?.let { eventSubject ->
            eventSubject
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe({ event -> if (event is E) onEvent(event) }) { throw  it }
                .let { disposable -> FLOW_DISPOSABLES[thisName]?.add(disposable) }
        }
    }

    protected inline fun <reified E1 : Event, reified E2 : Event> whenSeriesOfEventsOccur(
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

    protected inline fun <reified E1 : Event, reified E2 : Event, reified E3 : Event> whenSeriesOfEventsOccur(
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

    protected inline fun <reified E1 : Event, reified E2 : Event, reified E3 : Event, reified E4 : Event> whenSeriesOfEventsOccur(
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

    protected fun performAction(action: Action) {
        val thisName = javaClass.notNullName
        ACTION_SUBJECTS[thisName]?.onNext(action)
    }

    abstract class Event

    abstract class Action
}