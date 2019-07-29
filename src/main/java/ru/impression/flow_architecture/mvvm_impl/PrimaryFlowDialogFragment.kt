package ru.impression.flow_architecture.mvvm_impl

import ru.impression.flow_architecture.Flow

abstract class PrimaryFlowDialogFragment<F : Flow, S : Any>(
    override val flowClass: Class<F>,
    isGraphical: Boolean = true
) : FlowDialogFragment<F, S>(isGraphical), PrimaryFlowView<F, S> {

    override val groupUUID by lazy { super<PrimaryFlowView>.groupUUID }

    override val retrievedGroupUUID by lazy { super.retrievedGroupUUID }

    override val flow by lazy { super<PrimaryFlowView>.flow }
}