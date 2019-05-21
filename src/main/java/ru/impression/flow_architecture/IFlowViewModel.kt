package ru.impression.flow_architecture

interface IFlowViewModel<F : Flow> : FlowPerformer<F> {

    val savedViewAdditionalStates: HashMap<String, Any>
}