package ru.impression.flow_machine

import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import io.reactivex.subjects.Subject


/**
 * Subject selection by neccessary saved items count.
 * This method created because 0 size buffer in ReplaySubject throws IllegalStateException, and we need use
 * another subjects in 0-size case.
 * @param bufferedItemsCount - count of buffered items
 */
internal fun <T> createSubjectForBufferedItemsCount(bufferedItemsCount: Int): Subject<T> =
    if (bufferedItemsCount == 0)
        PublishSubject.create()
    else
        ReplaySubject.createWithSize(bufferedItemsCount)