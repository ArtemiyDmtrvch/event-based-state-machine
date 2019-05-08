package ru.impression.flow_architecture.impl

import android.arch.lifecycle.ViewModel
import ru.impression.flow_architecture.Event
import ru.impression.flow_architecture.Flow
import ru.impression.flow_architecture.FlowPerformer

abstract class FlowViewModel<F : Flow>(final override val flowClass: Class<F>) : ViewModel(), FlowPerformer<F> {

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