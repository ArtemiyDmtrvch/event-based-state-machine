package ru.impression.flow_architecture

import java.util.concurrent.ConcurrentLinkedQueue

internal object FlowProvider {

    private val waitingFlows: ConcurrentLinkedQueue<Flow> = ConcurrentLinkedQueue()

    operator fun <F : Flow> get(flowClass: Class<F>): Flow = waitingFlows
        .firstOrNull { it::class.java == flowClass }
        ?.also { waitingFlows.remove(it) }
        ?: flowClass.newInstance()

    fun addWaitingFlow(flow: Flow) {
        waitingFlows.add(flow)
    }
}