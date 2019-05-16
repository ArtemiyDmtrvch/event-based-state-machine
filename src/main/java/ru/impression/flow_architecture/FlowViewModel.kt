package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModel

abstract class FlowViewModel<F : Flow>(override val flowClass: Class<F>) : ViewModel(), IFlowViewModel<F> {

    override val savedViewAcquiredStates = HashMap<String, Any>()

    override var needToRestoreView: Boolean = false

    final override fun attachToFlow() = super.attachToFlow()

    init {
        attachToFlow()
    }

    override fun onCleared() {
        detachFromFlow()
        super.onCleared()
    }
}