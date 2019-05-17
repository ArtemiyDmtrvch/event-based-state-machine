package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModel

abstract class FlowViewModel<F : Flow>(override val flowClass: Class<F>) : ViewModel(), IFlowViewModel<F> {

    override var flowHashCode: Int? = null

    override val savedViewAcquiredStates = HashMap<String, Any>()

    override var needToRestoreView: Boolean = false

    override fun onCleared() {
        detachFromFlow()
        super.onCleared()
    }
}