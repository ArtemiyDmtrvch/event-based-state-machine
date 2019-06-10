package ru.impression.flow_architecture.mvvm_impl

import ru.impression.flow_architecture.Flow
import ru.impression.flow_architecture.PrimaryFlowPerformer
import java.lang.UnsupportedOperationException

interface PrimaryFlowView<F : Flow, S : Any> : FlowView<F, S>, PrimaryFlowPerformer<F> {

    override fun retrieveGroupUUID() = try {
        getViewModelProvider(
            ViewModelExistenceCheckingFactory()
        )[ViewStateSavingViewModel::class.java].performerGroupUUID
    } catch (e: UnsupportedOperationException) {
        super.retrieveGroupUUID()
    }
}