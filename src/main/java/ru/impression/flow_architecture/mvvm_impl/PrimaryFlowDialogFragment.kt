package ru.impression.flow_architecture.mvvm_impl

import ru.impression.flow_architecture.Flow
import java.util.*

abstract class PrimaryFlowDialogFragment<F : Flow, S : Any>(
    override val flowClass: Class<F>,
    isGraphical: Boolean = true
) : FlowDialogFragment<F, S>(isGraphical), PrimaryFlowView<F, S> {

    override lateinit var groupUUID: UUID

    override val flow by lazy { super<PrimaryFlowView>.flow }
}