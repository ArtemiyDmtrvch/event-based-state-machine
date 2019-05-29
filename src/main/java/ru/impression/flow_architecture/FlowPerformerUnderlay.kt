package ru.impression.flow_architecture

import io.reactivex.disposables.Disposable
import java.util.concurrent.ConcurrentLinkedQueue

class FlowPerformerUnderlay {

    internal var disposable: Disposable? = null

    internal var numberOfUnperformedActions = 0

    internal var isTemporarilyDetached = false

    internal var missedActions: ConcurrentLinkedQueue<Action>? = null
}