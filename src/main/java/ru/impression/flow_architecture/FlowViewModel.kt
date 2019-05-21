package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModel

abstract class FlowViewModel<F : Flow>(override val flowHost: FlowHost<F>) : ViewModel(), FlowPerformer<F> {

    final override fun attachToFlow() = super.attachToFlow()

    init {
        attachToFlow()
    }

    override fun onCleared() {
        detachFromFlow()
        super.onCleared()
    }
}