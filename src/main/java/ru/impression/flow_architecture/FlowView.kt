package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

interface FlowView<F : Flow, S : Any> : FlowPerformer<F> {

    val viewModelClass: Class<out ViewModel>?

    var viewModel: IFlowViewModel<F>?

    var primaryStateIsSet: Boolean
        get() = false
        set(value) {
            if (value) restoreSecondaryState()
        }

    var secondaryState: S

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

    fun saveSecondaryState() {
        viewModel?.savedViewSecondaryStates?.put(javaClass.notNullName, secondaryState)
    }

    fun restoreSecondaryState() {
        viewModel?.savedViewSecondaryStates?.remove(javaClass.notNullName)?.let { secondaryState = it as S }
    }

    override fun detachFromFlow() {
        viewModel?.needToRestoreView = true
        super.detachFromFlow()
    }
}