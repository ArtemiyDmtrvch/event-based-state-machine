package ru.impression.flow_architecture

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.functions.Function4
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

abstract class Flow {

    internal lateinit var performerGroupUUID: UUID

    @PublishedApi
    internal var initialAction: InitialAction? = null

    private val primaryInitializationCompleted = AtomicBoolean(false)

    @PublishedApi
    internal val actionSubject = ReplaySubject.createWithSize<Action>(1)

    @PublishedApi
    internal val onEvents = ConcurrentLinkedQueue<(Event) -> Unit>()

    @PublishedApi
    internal val eventSubject = PublishSubject.create<Event>()

    @PublishedApi
    internal val eventSeriesSubscriptionScheduler = Schedulers.io()

    @PublishedApi
    internal val eventSeriesObservingScheduler = Schedulers.single()

    @PublishedApi
    internal val disposables = CompositeDisposable()

    private val pendingEvents = ConcurrentLinkedQueue<Event>()

    internal val performerUnderlays = ConcurrentHashMap<String, FlowPerformer.Underlay>()

    private var isReplaying = false

    abstract fun start()

    inline fun <reified E : Event> whenEventOccurs(crossinline onEvent: (E) -> Unit) {
        onEvents.add { if (it is E) onEvent(it) }
    }

    inline fun <reified E1 : Event, reified E2 : Event> whenSeriesOfEventsOccur(
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
            .subscribeOn(eventSeriesSubscriptionScheduler)
            .observeOn(eventSeriesObservingScheduler)
            .doOnError { throw it }
            .subscribe()
            .let { disposables.add(it) }
    }

    inline fun <reified E1 : Event, reified E2 : Event, reified E3 : Event> whenSeriesOfEventsOccur(
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
            .subscribeOn(eventSeriesSubscriptionScheduler)
            .observeOn(eventSeriesObservingScheduler)
            .doOnError { throw it }
            .subscribe()
            .let { disposables.add(it) }
    }

    inline fun <reified E1 : Event, reified E2 : Event, reified E3 : Event, reified E4 : Event> whenSeriesOfEventsOccur(
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
            .subscribeOn(eventSeriesSubscriptionScheduler)
            .observeOn(eventSeriesObservingScheduler)
            .doOnError { throw it }
            .subscribe()
            .let { disposables.add(it) }
    }

    @PublishedApi
    internal fun eventOccurred(event: Event) {
        if (primaryInitializationCompleted.get()) {
            onEvents.forEach { it(event) }
            eventSubject.onNext(event)
            if (event is GlobalEvent && !event.occurred) {
                event.occurred = true
                FlowStore.forEach { it.eventOccurred(event) }
            }
        } else
            pendingEvents.add(event)
    }

    open fun performAction(action: Action) {
        if (action is InitialAction
            && (action is UnilateralInitialAction
                    || (action is BilateralInitialAction && action.flowClass == javaClass))
            && !isReplaying
        ) initialAction = action
        if (action === initialAction) primaryInitializationCompleted.set(false)
        performerUnderlays.values.forEach { underlay ->
            if (underlay.performerIsTemporarilyDetached.get())
                underlay.missedActions?.add(action)?.also { underlay.numberOfUnperformedActions.incrementAndGet() }
            else
                underlay.numberOfUnperformedActions.incrementAndGet()
        }
        actionSubject.onNext(action)
        if (action is BilateralInitialAction && action.flowClass != javaClass)
            FlowStore.newPendingEntry(action.flowClass).performAction(action)
    }

    @PublishedApi
    internal fun onPrimaryInitializationCompleted() {
        primaryInitializationCompleted.set(true)
        while (true) pendingEvents.poll()?.let { eventOccurred(it) } ?: break
    }

    @PublishedApi
    internal fun replay() {
        initialAction?.let {
            isReplaying = true
            performAction(it)
            isReplaying = false
        }
    }

    internal fun onPerformerCompletelyDetached() {
        if (performerUnderlays.isEmpty()) {
            onEvents.clear()
            disposables.dispose()
            FlowStore.remove(performerGroupUUID)
        }
    }
}