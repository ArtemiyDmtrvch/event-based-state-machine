package ru.impression.flow_architecture.mvvm_impl

import ru.impression.flow_architecture.Flow
import ru.impression.flow_architecture.PrimaryFlowPerformer
import java.lang.UnsupportedOperationException
import java.util.*

interface PrimaryFlowView<F : Flow, S : Any> : FlowView<F, S>, PrimaryFlowPerformer<F, FlowView.Underlay> {

    override val retrievedGroupUUID
        get() = try {
            getViewModelProvider(
                ViewModelExistenceCheckingFactory()
            )[ViewStateSavingViewModel::class.java].performerGroupUUID
        } catch (e: UnsupportedOperationException) {
            super.retrievedGroupUUID
        }
}