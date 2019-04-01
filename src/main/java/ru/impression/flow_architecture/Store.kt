package ru.impression.flow_architecture

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.ConcurrentHashMap

@PublishedApi
internal val FLOW_DISPOSABLES: ConcurrentHashMap<String, CompositeDisposable> = ConcurrentHashMap()

internal val FLOW_PERFORMER_DISPOSABLES: ConcurrentHashMap<String, ConcurrentHashMap<String, Disposable>> =
    ConcurrentHashMap()

@PublishedApi
internal val EVENT_SUBJECTS: ConcurrentHashMap<String, BehaviorSubject<Flow.Event>> = ConcurrentHashMap()

internal val ACTION_SUBJECTS: ConcurrentHashMap<String, BehaviorSubject<Flow.Action>> = ConcurrentHashMap()