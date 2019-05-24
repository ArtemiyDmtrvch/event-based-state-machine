package ru.impression.flow_architecture

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

internal object FlowStore {

    private val pendingFlows = ConcurrentLinkedQueue<Flow>()

    private val runningFlows = ConcurrentHashMap<UUID, Flow>()

    operator fun <F : Flow> get(performerGroupUUID: UUID): F? = runningFlows[performerGroupUUID] as F?

    fun <F : Flow> add(flowClass: Class<F>): F = flowClass.newInstance().also { pendingFlows.add(it) }

    fun <F : Flow> add(flowClass: Class<F>, performerGroupUUID: UUID): F {
        val flow = pendingFlows
            .firstOrNull { it::class.java == flowClass }
            ?.also { FlowStore.pendingFlows.remove(it) } as F?
            ?: flowClass.newInstance()
        flow.performerGroupUUID = performerGroupUUID
        runningFlows[performerGroupUUID] = flow
        return flow
    }

    fun remove(performerGroupUUID: UUID) {
        runningFlows.remove(performerGroupUUID)
    }
}