package ru.impression.flow_architecture

import java.util.*

/**
 * [FlowPerformer] that starts working with [Flow] - it creates Flow upon [attachment][attachToFlow] and other
 * FlowPerformers.
 */
interface PrimaryFlowPerformer<F : Flow, U : FlowPerformer.Underlay> :
    FlowPerformer<F, U> {

    /**
     * The class of the performable [Flow] the instance of which this PrimaryFlowPerformer creates when
     * [attaching][attachToFlow].
     */
    val flowClass: Class<F>

    override val groupUUID: UUID get() = retrievedGroupUUID ?: UUID.randomUUID()

    /**
     * The group UUID of this PrimaryFlowPerformer, which is used to retrieve [Flow] after temporarily detaching this
     * PrimaryFlowPerformer.
     */
    val retrievedGroupUUID: UUID? get() = null

    override val flow
        get() = retrievedGroupUUID
            ?.let { FlowStore.get<F>(it) }
            ?: FlowStore.newEntry(groupUUID, flowClass)
}