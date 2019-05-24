package ru.impression.flow_architecture.mvvm_impl

import ru.impression.flow_architecture.Flow
import ru.impression.flow_architecture.FlowPerformer
import ru.impression.flow_architecture.PrimaryFlowPerformer
import java.lang.UnsupportedOperationException
import java.util.*

interface PrimaryFlowView<F : Flow, S : Any> : FlowView<F, S>, PrimaryFlowPerformer<F> {

    override fun retrieveGroupUUIDFromExistingLinkedPerformers(): UUID? {
        for (clazz in flowViewModelClasses) {
            try {
                return (getViewModelProvider(ViewModelExistenceCheckingFactory())[clazz] as FlowPerformer<F>).groupUUID
            } catch (e: UnsupportedOperationException) {
                continue
            }
        }
        return super.retrieveGroupUUIDFromExistingLinkedPerformers()
    }
}