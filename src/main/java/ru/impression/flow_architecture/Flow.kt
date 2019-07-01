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

abstract class Flow {

    internal lateinit var performerGroupUUID: UUID

    @PublishedApi
    internal var replayableAction: Action? = null

    @PublishedApi
    internal val onEvents = ConcurrentHashMap<String, (Event) -> Unit>()

    @PublishedApi
    internal val eventSubject = PublishSubject.create<Event>()

    @PublishedApi
    internal val subscriptionScheduler = Schedulers.io()

    @PublishedApi
    internal val observingScheduler = Schedulers.single()

    @PublishedApi
    internal val disposables = CompositeDisposable()

    @PublishedApi
    internal val actionSubject = ReplaySubject.createWithSize<Action>(1)

    internal val performerUnderlays = ConcurrentHashMap<String, FlowPerformer.Underlay>()

    private var isReplaying = false

    abstract fun start()

    inline fun <reified E : Event> whenEventOccurs(crossinline onEvent: (E) -> Unit) {
        onEvents[E::class.java.notNullName] = { onEvent(it as E) }
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
            .subscribeOn(subscriptionScheduler)
            .observeOn(observingScheduler)
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
            .subscribeOn(subscriptionScheduler)
            .observeOn(observingScheduler)
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
            .subscribeOn(subscriptionScheduler)
            .observeOn(observingScheduler)
            .doOnError { throw it }
            .subscribe()
            .let { disposables.add(it) }
    }

    @PublishedApi
    internal fun eventOccurred(event: Event) {
        onEvents[event.javaClass.notNullName]?.invoke(event)
        eventSubject.onNext(event)
        if (event is GlobalEvent && !event.occurred) {
            event.occurred = true
            FlowStore.forEach { it.eventOccurred(event) }
        }
    }

    open fun performAction(action: Action) {
        if ((action is ReplayableAction || (action is ReplayableInitiatingAction && action.flowClass == javaClass))
            && !isReplaying
        ) replayableAction = action
        performerUnderlays.values.forEach { underlay ->
            if (underlay.performerIsTemporarilyDetached.get()) {
                underlay.missedActions?.add(action)?.also { underlay.numberOfUnperformedActions.incrementAndGet() }
            } else
                underlay.numberOfUnperformedActions.incrementAndGet()
        }
        actionSubject.onNext(action)
        if (action is InitiatingAction && action.flowClass != javaClass)
            FlowStore.add(action.flowClass).performAction(action)
    }

    @PublishedApi
    internal fun replay() {
        replayableAction?.let {
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