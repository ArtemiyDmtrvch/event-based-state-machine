package ru.impression.flow_architecture

import android.os.Handler
import android.os.Looper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.functions.Function4
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

abstract class Flow {

    @PublishedApi
    internal var parentFlow: Flow? = null

    private var replayableInitiatingAction: ReplayableInitiatingAction? = null

    @PublishedApi
    internal val onEvents: ConcurrentHashMap<String, (Event) -> Unit> = ConcurrentHashMap()

    @PublishedApi
    internal var eventSubject: PublishSubject<Event>? = null

    @PublishedApi
    internal val subscriptionScheduler = Schedulers.io()

    @PublishedApi
    internal val disposables: CompositeDisposable = CompositeDisposable()

    private val performActions: ConcurrentHashMap<String, (Action) -> Unit> = ConcurrentHashMap()

    private var lastAction: Action? = null

    internal val missedActions: ConcurrentHashMap<String, ConcurrentLinkedQueue<Action>> = ConcurrentHashMap()

    fun attachPerformer(performer: FlowPerformer<*>, attachMode: AttachMode) {
        performActions[performer.javaClass.notNullName] =
            { action: Action -> performer.performAction(action) }.also { performAction ->
                when (attachMode) {
                    AttachMode.CONTINUE -> lastAction?.let(performAction)
                    AttachMode.REPLAY -> replayableInitiatingAction?.let { this@Flow.performAction(it) }
                }
            }
    }

    protected inline fun <reified E : Event> whenEventOccurs(crossinline onEvent: (E) -> Unit) {
        onEvents[E::class.java.notNullName] = {
            onEvent(it as E)
            if (it is ResultingEvent) parentFlow?.eventOccurred(it)
        }
    }

    protected inline fun <reified E1 : Event, reified E2 : Event> whenSeriesOfEventsOccur(
        crossinline onSeriesOfEvents: (E1, E2) -> Unit
    ) {
        initEventSubject()
        Observable
            .zip(
                eventSubject!!
                    .filter { it is E1 }
                    .map { it as E1 },
                eventSubject!!
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
        initEventSubject()
        Observable
            .zip(
                eventSubject!!
                    .filter { it is E1 }
                    .map { it as E1 },
                eventSubject!!
                    .filter { it is E2 }
                    .map { it as E2 },
                eventSubject!!
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
        initEventSubject()
        Observable
            .zip(
                eventSubject!!
                    .filter { it is E1 }
                    .map { it as E1 },
                eventSubject!!
                    .filter { it is E2 }
                    .map { it as E2 },
                eventSubject!!
                    .filter { it is E3 }
                    .map { it as E3 },
                eventSubject!!
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

    @PublishedApi
    internal fun initEventSubject() {
        if (eventSubject == null) eventSubject = PublishSubject.create()
    }

    @PublishedApi
    internal fun eventOccurred(event: Event) {
        Handler(Looper.getMainLooper()).post {
            onEvents[event.javaClass.notNullName]?.invoke(event)
            eventSubject?.onNext(event)
        }
    }

    protected fun performAction(action: Action) {
        performActions.values.forEach { it.invoke(action) }
        missedActions.values.forEach { it.add(action) }
        if (action is InitiatingAction && action.flowClass != javaClass) {
            action.flowClass.newInstance()
                .apply {
                    if (action is ReplayableInitiatingAction) replayableInitiatingAction = action
                    performAction(action)
                }
                .let { FlowProvider.addWaitingFlow(it) }
        }
        lastAction = action
    }

    fun detachPerformer(performer: FlowPerformer<*>) {
        val performerName = performer.javaClass.notNullName
        performActions.remove(performerName)
        if (performActions.isEmpty()) {
            onEvents.clear()
            disposables.dispose()
        }
    }
}