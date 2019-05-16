package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModel

abstract class FlowViewModel<F : Flow>(final override val flowClass: Class<F>) : ViewModel(), IFlowViewModel<F> {

    final override val savedViewSecondaryStates = HashMap<String, Any>()

    final override var needToRestoreView: Boolean = false

    final override fun attachToFlow() = super.attachToFlow()

    final override fun eventOccurred(event: Event) = super.eventOccurred(event)

    final override fun detachFromFlow() = super.detachFromFlow()

    init {
        attachToFlow()
    }

    override fun onCleared() {
        detachFromFlow()
        super.onCleared()
    }
}