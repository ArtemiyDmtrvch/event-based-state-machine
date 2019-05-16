package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

interface FlowView<F : Flow, S : Any> : FlowPerformer<F> {

    val viewModelClass: Class<out ViewModel>?

    var viewModel: IFlowViewModel<F>?

    var initialStateIsSet: Boolean
        get() = false
        set(value) {
            if (value) restoreAcquiredState()
        }

    var acquiredState: S

    fun init(viewModelProvider: ViewModelProvider) = viewModelClass?.let { clazz ->
        viewModelProvider[clazz].let {
            if (it is IFlowViewModel<*>) viewModel = (it as IFlowViewModel<F>).also {
                if (it.needToRestoreView) {
                    eventOccurred(RestorationRequested())
                    it.needToRestoreView = false
                }
            }
        }
    }

    fun saveAcquiredState() {
        viewModel?.savedViewAcquiredStates?.put(javaClass.notNullName, acquiredState)
    }

    fun restoreAcquiredState() {
        viewModel?.savedViewAcquiredStates?.remove(javaClass.notNullName)?.let { acquiredState = it as S }
    }

    override fun detachFromFlow() {
        viewModel?.needToRestoreView = true
        super.detachFromFlow()
    }
}