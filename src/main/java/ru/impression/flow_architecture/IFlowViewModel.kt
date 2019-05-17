package ru.impression.flow_architecture

interface IFlowViewModel<F : Flow> : FlowPerformer<F> {

    val savedViewAcquiredStates: HashMap<String, Any>

    var needToRestoreView: Boolean

    fun init(flowHashCode: Int) {
        this.flowHashCode = flowHashCode
        attachToFlow()
        if (needToRestoreView) {
            eventOccurred(RestorationRequested())
            needToRestoreView = false
        }
    }
}