package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModel

abstract class FlowViewModel<F : Flow>(override val flowClass: Class<F>) : ViewModel(), IFlowViewModel<F> {

    override var flow: Flow? = null

    override val savedViewAcquiredStates = HashMap<String, Any>()

    final override fun attachToFlow() = super.attachToFlow()

    init {
        attachToFlow()
    }

    override fun onCleared() {
        detachFromFlow()
        super.onCleared()
    }
}