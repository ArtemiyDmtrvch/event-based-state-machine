package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModel

abstract class FlowViewModel<F : Flow>(override val flowClass: Class<F>) : ViewModel(), FlowPerformer<F> {

    override var flow: Flow? = null

    final override fun attachToFlow() = super.attachToFlow()

    init {
        attachToFlow()
    }

    override fun onCleared() {
        detachFromFlow()
        super.onCleared()
    }
}