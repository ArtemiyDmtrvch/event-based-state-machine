package ru.impression.flow_architecture

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.functions.Function4
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue


abstract class Flow {

    @PublishedApi
    internal var parentFlowHashCode: Int? = null

    @PublishedApi
    internal val subscriptionScheduler = Schedulers.io()

    @PublishedApi
    internal val observingScheduler = Schedulers.single()

    @PublishedApi
    internal val onEvents: ConcurrentHashMap<String, (Event) -> Unit> = ConcurrentHashMap()

    @PublishedApi
    internal val disposables: CompositeDisposable = CompositeDisposable()

    internal val performerDisposables: ConcurrentHashMap<String, Disposable> = ConcurrentHashMap()

    @PublishedApi
    internal val eventSubject = PublishSubject.create<Event>().also {
        it.subscribeOn(subscriptionScheduler)
            .observeOn(observingScheduler)
            .subscribe({ event -> onEvents[event::class.java.notNullName]?.invoke(event) }) { throw  it }
            .let { disposable -> disposables.add(disposable) }
    }

    internal val actionSubject = ReplaySubject.createWithSize<Action>(1)

    internal val missedActions: ConcurrentHashMap<String, ConcurrentLinkedQueue<Action>> = ConcurrentHashMap()

    internal fun initRestoration(restorativeInitiatingAction: RestorativeInitiatingAction) {
        whenEventOccurs<RestorationRequested> { performAction(restorativeInitiatingAction) }
    }

    protected inline fun <reified E : Event> whenEventOccurs(crossinline onEvent: (E) -> Unit) {
        onEvents[E::class.java.notNullName] = {
            onEvent(it as E)
            if (it is ResultingEvent && parentFlowHashCode != null) getFlow(parentFlowHashCode!!)?.eventSubject?.onNext(
                it
            )
        }
    }

    protected inline fun <reified E1 : Event, reified E2 : Event> whenSeriesOfEventsOccur(
        crossinline onSeriesOfEvents: (E1, E2) -> Unit
    ) {
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
            .subscribeOn(subscriptionScheduler)
            .observeOn(observingScheduler)
            .doOnError { throw it }
            .subscribe()
            .let { disposable -> disposables.add(disposable) }
    }

    protected inline fun <reified E1 : Event, reified E2 : Event, reified E3 : Event> whenSeriesOfEventsOccur(
        crossinline onSeriesOfEvents: (E1, E2, E3) -> Unit
    ) {
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
            .subscribeOn(subscriptionScheduler)
            .observeOn(observingScheduler)
            .doOnError { throw it }
            .subscribe()
            .let { disposable -> disposables.add(disposable) }
    }

    protected inline fun <reified E1 : Event, reified E2 : Event, reified E3 : Event, reified E4 : Event> whenSeriesOfEventsOccur(
        crossinline onSeriesOfEvents: (E1, E2, E3, E4) -> Unit
    ) {
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
            .subscribeOn(subscriptionScheduler)
            .observeOn(observingScheduler)
            .doOnError { throw it }
            .subscribe()
            .let { disposable -> disposables.add(disposable) }
    }

    protected fun performAction(action: Action) {
        actionSubject.onNext(action)
        missedActions.forEach { it.value.add(action) }
        if (action is InitiatingAction && action.flowClass != javaClass) {
            FlowManager.startFlow(action.flowClass, hashCode(), action).apply { actionSubject.onNext(action) }
        }
    }
}