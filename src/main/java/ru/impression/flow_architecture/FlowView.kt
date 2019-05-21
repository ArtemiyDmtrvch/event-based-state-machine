package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity

interface FlowView<F : Flow, S : Any> : FlowPerformer<F> {

    val flowViewModelClass: Class<out ViewModel>?

    var flowViewModel: IFlowViewModel<F>?

    var additionalState: S

    override fun attachToFlow() {
        val viewModelProvider = when (this) {
            is Fragment -> ViewModelProviders.of(this)
            is FragmentActivity -> ViewModelProviders.of(this)
            else -> null
        }
        val flowHostViewModel = viewModelProvider?.get(FlowViewModel::class.java)
        initFlow(flowViewModel)
        super.attachToFlow(
            flowViewModel ?: this,
            if (flow?.cachedActions?.containsKey(javaClass.notNullName) == true)
                AttachmentType.REPLAY_ATTACHMENT
            else
                AttachmentType.NORMAL_ATTACHMENT
        )
    }

    fun groundStateIsSet() {
        flowViewModel?.savedViewAdditionalStates
            ?.remove(javaClass.notNullName)
            ?.let { additionalState = it as S }
        performCachedActions()
    }

    override fun detachFromFlow(cacheActions: Boolean) {
        if (cacheActions)
            additionalState
                .takeIf { it !is Unit && it !is Nothing }
                ?.let { flowViewModel?.savedViewAdditionalStates?.put(javaClass.notNullName, it) }
        super.detachFromFlow(cacheActions)
    }
}