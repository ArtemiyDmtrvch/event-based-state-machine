package ru.impression.flow_architecture

interface IFlowViewModel<F: Flow> : FlowPerformer<F> {

    val savedViewSecondaryStates: HashMap<String, Any>

    var needToRestoreView: Boolean
}