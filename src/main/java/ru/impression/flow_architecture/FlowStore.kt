package ru.impression.flow_architecture

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

internal object FlowStore : Iterable<Flow> {

    private val pendingFlows = ConcurrentLinkedQueue<Flow>()

    private val runningFlows = ConcurrentHashMap<UUID, Flow>()

    operator fun <F : Flow> get(performerGroupUUID: UUID): F? = runningFlows[performerGroupUUID] as F?

    fun <F : Flow> newPendingEntry(flowClass: Class<F>): F =
        flowClass.newInstance()
            .apply { start() }
            .also { pendingFlows.add(it) }

    fun <F : Flow> newEntry(performerGroupUUID: UUID, flowClass: Class<F>): F {
        val flow = pendingFlows
            .firstOrNull { it::class.java == flowClass }
            ?.also { FlowStore.pendingFlows.remove(it) } as F?
            ?: flowClass.newInstance().apply { start() }
        flow.performerGroupUUID = performerGroupUUID
        runningFlows[performerGroupUUID] = flow
        return flow
    }

    fun remove(performerGroupUUID: UUID) {
        runningFlows.remove(performerGroupUUID)
    }

    override fun iterator() = runningFlows.values.iterator()
}