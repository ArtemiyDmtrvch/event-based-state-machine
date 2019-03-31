package ru.impression.flow_machine

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.ReplaySubject
import io.reactivex.subjects.Subject
import java.util.concurrent.ConcurrentHashMap

@PublishedApi
internal val DISPOSABLES: ConcurrentHashMap<String, CompositeDisposable> = ConcurrentHashMap()

@PublishedApi
internal val EVENT_SUBJECTS: ConcurrentHashMap<String, Subject<Flow.Event>> = ConcurrentHashMap()

internal val ACTION_SUBJECTS: ConcurrentHashMap<String, Subject<Flow.Action>> = ConcurrentHashMap()