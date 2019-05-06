package ru.impression.flow_architecture

interface RestorableFlowPerformer<F : Flow, S : StateStore> : FlowPerformer<F> {

    val stateStoreClass: Class<S>

    fun saveState(stateStore: S)
}