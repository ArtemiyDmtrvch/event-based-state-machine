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

/**
 * The main class of the library, which contains all the business logic of the application, described by the principle:
 * an [Event] has occurred -> perform an [Action]. Inherit from this class, override the [start] method, inside which
 * create "event -> action" blocks using [whenEventOccurs], [whenSeriesOfEventsOccur] and [performAction] methods. If
 * your Flow was not launched from another Flow using [BilateralInitialAction], you need to perform
 * [UnilateralInitialAction] before the "event -> action" blocks. As a result, you get something like this:
 * <pre>
 * `class MyAwesomePageFlow : Flow() {
 *
 *      override fun start() {
 *           performAction(OpenMyAwesomePage())
 *
 *           whenEventOccurs<MyAwesomePageOpened> { performAction(LoadMyAwesomeData()) }
 *
 *           whenEventOccurs<MyAwesomeDataLoaded> { performAction(ShowMyAwesomeData(it.data)) }
 *      }
 * }
 *
 * class OpenMyAwesomePage : UnilateralInitialAction()
 *
 * class MyAwesomePageOpened : Event()
 * class LoadMyAwesomeData : Action()
 *
 * class DataMyAwesomeLoaded(val data: Array<String>) : Event()
 * class ShowMyAwesomeData(val data: Array<String>) : Action()`
 * </pre>
 *
 * NOTE that Flow should not contain any state and no logic other than the one described above. The flow should be
 * considered as a reflector - events fall into it and are reflected in the form of actions.
 */
abstract class Flow {

    @Volatile
    internal lateinit var performerGroupUUID: UUID

    @PublishedApi
    @Volatile
    internal var initialAction: InitialAction? = null

    private val primaryPerformerInitializationCompleted = AtomicBoolean(false)

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

    private val isReplaying = AtomicBoolean(false)

    /**
     * Describes the business-logic of the application using [whenEventOccurs], [whenSeriesOfEventsOccur] and
     * [performAction] methods.
     */
    abstract fun start()

    /**
     * Instructs [all performers][FlowPerformer] of current [Flow] to perform the specified action.
     * @param action - [Action] to be performed by [performers][FlowPerformer]
     */
    open fun performAction(action: Action) {
        if (action is InitialAction
            && (action is UnilateralInitialAction
                    || (action is BilateralInitialAction && action.flowClass == javaClass))
            && !isReplaying.get()
        ) initialAction = action
        if (action === initialAction) primaryPerformerInitializationCompleted.set(false)
        performerUnderlays.values.forEach {
            it.apply {
                if (performerIsTemporarilyDetached.get())
                    missedActions?.add(action)?.also { numberOfUnperformedActions.incrementAndGet() }
                else
                    numberOfUnperformedActions.incrementAndGet()
            }
        }
        actionSubject.onNext(action)
        if (action is BilateralInitialAction && action.flowClass != javaClass)
            FlowStore.newPendingEntry(action.flowClass).performAction(action)
    }

    @PublishedApi
    internal fun onPrimaryPerformerInitializationCompleted() {
        primaryPerformerInitializationCompleted.set(true)
        while (true) pendingEvents.poll()?.let { eventOccurred(it) } ?: break
    }

    /**
     * Memorizes what needs to be done when the specified [Event] or its heirs occurs.
     * @param E
     * @param onEvent - will be called every time the specified [Event] occurs. NOTE that the body of this lambda should
     * contain only calls to the [performAction] method and all the logic should be in [FlowPerformer].
     */
    inline fun <reified E : Event> whenEventOccurs(crossinline onEvent: (E) -> Unit) {
        onEvents.add { if (it is E) onEvent(it) }
    }

    /**
     * Memorizes what needs to be done when a series (sequence) of specified events or their heirs occurs.
     * @param E1
     * @param E2
     * @param onSeriesOfEvents - will be called every time a specified series of [events][Event] will occur. NOTE that
     * the body of this lambda should contain only calls to the [performAction] method and all the logic should be in
     * [FlowPerformer].
     */
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

    /**
     * Memorizes what needs to be done when a series (sequence) of specified events or their heirs occurs.
     * @param E1
     * @param E2
     * @param E3
     * @param onSeriesOfEvents - will be called every time a specified series of [events][Event] will occur. NOTE that
     * the body of this lambda should contain only calls to the [performAction] method and all the logic should be in
     * [FlowPerformer].
     */
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

    /**
     * Memorizes what needs to be done when a series (sequence) of specified events or their heirs occurs.
     * @param E1
     * @param E2
     * @param E3
     * @param E4
     * @param onSeriesOfEvents - will be called every time a specified series of [events][Event] will occur. NOTE that
     * the body of this lambda should contain only calls to the [performAction] method and all the logic should be in
     * [FlowPerformer].
     */
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
        if (primaryPerformerInitializationCompleted.get()) {
            onEvents.forEach { it(event) }
            eventSubject.onNext(event)
            if (event is GlobalEvent && !event.occurred) {
                event.occurred = true
                FlowStore.forEach { it.eventOccurred(event) }
            }
        } else
            pendingEvents.add(event)
    }

    @PublishedApi
    internal fun replay() {
        initialAction?.let {
            isReplaying.set(true)
            performAction(it)
            isReplaying.set(false)
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