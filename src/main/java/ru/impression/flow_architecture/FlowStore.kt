package ru.impression.flow_architecture

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

internal object FlowStore {

    private val pendingFlows = ConcurrentLinkedQueue<Flow>()

    private val runningFlows = ConcurrentHashMap<UUID, Flow>()

    operator fun <F : Flow> get(performerGroupUUID: UUID): F? = runningFlows[performerGroupUUID] as F?

    fun <F : Flow> add(flowClass: Class<F>, performerGroupUUID: UUID? = null): F = performerGroupUUID
        ?.let {
            pendingFlows
                .firstOrNull { it::class.java == flowClass }
                ?.also { FlowStore.pendingFlows.remove(it) } as F?
                ?: flowClass.newInstance()
        }
        ?.apply { this.performerGroupUUID = performerGroupUUID }
        ?.also { runningFlows[performerGroupUUID] = it }
        ?: flowClass.newInstance().also { pendingFlows.add(it) }

    fun remove(performerGroupUUID: UUID) {
        runningFlows.remove(performerGroupUUID)
    }
}