package ru.impression.flow_architecture

import java.util.concurrent.ConcurrentLinkedQueue

internal object FlowStore {

    val waitingFlows = ConcurrentLinkedQueue<Flow>()
}