package ru.impression.flow_architecture

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
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
    internal var parentFlow: Flow? = null

    private var replayableInitiatingAction: ReplayableInitiatingAction? = null

    internal val performerDisposables = ConcurrentHashMap<String, Disposable>()

    @PublishedApi
    internal val onEvents = ConcurrentHashMap<String, (Event) -> Unit>()

    @PublishedApi
    internal val eventSubject = PublishSubject.create<Event>()

    @PublishedApi
    internal val subscriptionScheduler = Schedulers.io()

    @PublishedApi
    internal val disposables = CompositeDisposable()

    internal val actionSubject = ReplaySubject.createWithSize<Action>(1)

    internal val temporarilyDetachedPerformers = ConcurrentLinkedQueue<String>()

    @PublishedApi
    internal val pendingResultingEvents = ConcurrentLinkedQueue<ResultingEvent>()

    @PublishedApi
    internal val performingMode
        get() = if (temporarilyDetachedPerformers.isEmpty()) PerformingMode.FULL else PerformingMode.PARTIAL

    protected inline fun <reified E : Event> whenEventOccurs(crossinline onEvent: (E) -> Unit) {
        onEvents[E::class.java.notNullName] = { event ->
            onEvent(event as E)
            if (event is ResultingEvent) parentFlow?.let { parentFlow ->
                if (parentFlow.performingMode == PerformingMode.FULL)
                    parentFlow.eventOccurred(event)
                else
                    parentFlow.pendingResultingEvents.add(event)
            }
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
            .observeOn(AndroidSchedulers.mainThread())
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
            .observeOn(AndroidSchedulers.mainThread())
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
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { throw it }
            .subscribe()
            .let { disposable -> disposables.add(disposable) }
    }

    internal fun onPerformerAttached() {
        if (performingMode == PerformingMode.FULL) pendingResultingEvents.forEach {
            eventOccurred(it)
            pendingResultingEvents.remove(it)
        }
    }

    @PublishedApi
    internal fun eventOccurred(event: Event) {
        onEvents[event.javaClass.notNullName]?.invoke(event)
        eventSubject.onNext(event)
    }

    protected fun performAction(action: Action) {
        actionSubject.onNext(action)
        if (action is InitiatingAction && action.flowClass != javaClass) {
            action.flowClass.newInstance()
                .also { it.parentFlow = this }
                .apply {
                    if (action is ReplayableInitiatingAction) replayableInitiatingAction = action
                    performAction(action)
                }
                .let { FlowStore.waitingFlows.add(it) }
        }
    }

    internal fun replay() = replayableInitiatingAction?.let { performAction(it) }

    fun onPerformerDetached() {
        if (performerDisposables.isEmpty()) {
            onEvents.clear()
            disposables.dispose()
        }
    }

    @PublishedApi
    internal enum class PerformingMode {
        FULL,
        PARTIAL
    }
}