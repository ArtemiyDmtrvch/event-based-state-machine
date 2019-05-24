package ru.impression.flow_architecture

import java.util.*

interface PrimaryFlowPerformer<F : Flow> : FlowPerformer<F> {

    val flowClass: Class<F>

    override var groupUUID: UUID

    fun retrieveGroupUUIDFromExistingLinkedPerformers(): UUID? = null

    override val flow: F
        get() = retrieveGroupUUIDFromExistingLinkedPerformers()?.let { retrievedUUID ->
            groupUUID = retrievedUUID
            FlowStore.get<F>(groupUUID)!!
        } ?: run {
            groupUUID = UUID.randomUUID()
            FlowStore.add(flowClass, groupUUID)
        }
}