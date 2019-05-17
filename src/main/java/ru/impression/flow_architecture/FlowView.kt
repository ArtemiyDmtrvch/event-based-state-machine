package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

interface FlowView<F : Flow, S : Any> : FlowPerformer<F> {

    val viewModelClass: Class<out ViewModel>?

    var viewModel: IFlowViewModel<F>?

    var acquiredState: S

    override var isActive: Boolean
        get() = super.isActive
        set(value) {
            if (value)
                viewModel?.savedViewAcquiredStates?.remove(javaClass.notNullName)?.let { acquiredState = it as S }
            else
                acquiredState.takeIf { it !is Unit && it !is Nothing }?.let {
                    viewModel?.savedViewAcquiredStates?.put(javaClass.notNullName, it)
                }
            super.isActive = value
        }

    fun init(viewModelProvider: ViewModelProvider) {
        viewModelClass?.let { clazz ->
            viewModel = (viewModelProvider[clazz] as IFlowViewModel<F>).also { flowHashCode = it.flowHashCode }
        }
        attachToFlow()
        viewModel?.init(flowHashCode!!)
    }

    fun initialStateIsSet() {
        isActive = true
    }

    override fun performAction(action: Action) {
        if (!isActive && action !is InitiatingAction) return
    }

    override fun detachFromFlow() {
        viewModel?.needToRestoreView = true
        super.detachFromFlow()
    }
}