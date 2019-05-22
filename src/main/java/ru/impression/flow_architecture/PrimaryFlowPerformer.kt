package ru.impression.flow_architecture

import java.util.*

interface PrimaryFlowPerformer<F : Flow> : FlowPerformer<F> {

    override val groupUUID: UUID get() = UUID.randomUUID()

    val flowClass: Class<F>

    override val flow get() = FlowStore[groupUUID] ?: FlowStore.add(flowClass, groupUUID)
}