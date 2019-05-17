package ru.impression.flow_architecture

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

@PublishedApi
internal val FLOW_DISPOSABLES: ConcurrentHashMap<String, CompositeDisposable> = ConcurrentHashMap()

internal val FLOW_PERFORMER_DISPOSABLES: ConcurrentHashMap<String, ConcurrentHashMap<String, Disposable>> =
    ConcurrentHashMap()

@PublishedApi
internal val EVENT_SUBJECTS: ConcurrentHashMap<String, PublishSubject<Event>> = ConcurrentHashMap()

internal val ACTION_SUBJECTS: ConcurrentHashMap<String, ReplaySubject<Action>> = ConcurrentHashMap()

internal val MISSED_ACTIONS: ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentLinkedQueue<Action>>> =
    ConcurrentHashMap()