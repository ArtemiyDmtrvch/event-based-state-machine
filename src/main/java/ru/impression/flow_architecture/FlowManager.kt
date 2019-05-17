package ru.impression.flow_architecture

internal object FlowManager {

    fun startFlow(
        flowClass: Class<out Flow>,
        parentFlowHashCode: Int? = null,
        initiatingAction: InitiatingAction? = null
    ): Flow = flowClass.newInstance()
        .apply {
            this.parentFlowHashCode = parentFlowHashCode
            if (initiatingAction is RestorativeInitiatingAction) initRestoration(initiatingAction)
        }
        .also { flow ->
            initiatingAction?.let { WAITING_FLOWS.add(flow) } ?: FLOWS.add(flow)
        }

    fun stopFlowIfNeeded(flow: Flow) {
        if (flow.performerDisposables.isEmpty()) {
            flow.disposables.dispose()
            FLOWS.remove(flow)
        }
    }
}