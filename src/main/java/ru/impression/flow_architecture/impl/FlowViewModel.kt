package ru.impression.flow_architecture.impl

import android.arch.lifecycle.ViewModel
import ru.impression.flow_architecture.Flow
import ru.impression.flow_architecture.FlowEvent
import ru.impression.flow_architecture.FlowPerformer
import ru.impression.flow_architecture.StateStore

abstract class FlowViewModel<F : Flow>(final override val flowClass: Class<F>) : ViewModel(), FlowPerformer<F> {

    internal var stateStore: StateStore? = null

    final override fun attachToFlow() = super.attachToFlow()

    final override fun eventOccurred(event: FlowEvent) = super.eventOccurred(event)

    final override fun detachFromFlow() = super.detachFromFlow()

    init {
        attachToFlow()
    }

    override fun onCleared() {
        detachFromFlow()
        super.onCleared()
    }
}