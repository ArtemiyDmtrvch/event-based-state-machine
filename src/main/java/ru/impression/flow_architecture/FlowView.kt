package ru.impression.flow_architecture

interface FlowView<F : Flow, S : Any> : FlowPerformer<F> {

    val flowClass: Class<F>

    override val flowHost: FlowHostViewModel<F>

    var additionalState: S

    override fun attachToFlow() {
        super.attachToFlow(
            if (flowHost.flow.cachedActions.containsKey(javaClass.notNullName))
                AttachmentType.REPLAY_ATTACHMENT
            else
                AttachmentType.NORMAL_ATTACHMENT
        )
    }

    fun groundStateIsSet() {
        flowHost.savedViewAdditionalStates
            .remove(javaClass.notNullName)
            ?.let { additionalState = it as S }
        performCachedActions()
    }

    fun detachFromFlowForAWhile() {
        detachFromFlow(true)
        additionalState
            .takeIf { it !is Unit && it !is Nothing }
            ?.let { flowHost.savedViewAdditionalStates.put(javaClass.notNullName, it) }
    }
}