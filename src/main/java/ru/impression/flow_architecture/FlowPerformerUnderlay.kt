package ru.impression.flow_architecture

import java.util.concurrent.ConcurrentLinkedQueue

class FlowPerformerUnderlay {

    internal var numberOfUnperformedActions = 0

    internal var isTemporarilyDetached = false

    internal var missedActions: ConcurrentLinkedQueue<Action>? = null
}