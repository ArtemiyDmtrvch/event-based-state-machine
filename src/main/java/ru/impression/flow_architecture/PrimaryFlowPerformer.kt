package ru.impression.flow_architecture

import java.util.*

interface PrimaryFlowPerformer<F : Flow, U : FlowPerformer.Underlay> : FlowPerformer<F, U> {

    val flowClass: Class<F>

    override val groupUUID: UUID get() = retrievedGroupUUID ?: UUID.randomUUID()

    val retrievedGroupUUID: UUID? get() = null

    override val flow get() = retrievedGroupUUID?.let { FlowStore.get<F>(it) } ?: FlowStore.add(flowClass, groupUUID)
}