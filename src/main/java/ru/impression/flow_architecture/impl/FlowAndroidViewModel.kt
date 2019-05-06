package ru.impression.flow_architecture.impl

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import ru.impression.flow_architecture.Flow
import ru.impression.flow_architecture.FlowEvent
import ru.impression.flow_architecture.FlowPerformer
import ru.impression.flow_architecture.StateStore

abstract class FlowAndroidViewModel<F : Flow>(
    application: Application,
    final override val flowClass: Class<F>
) : AndroidViewModel(application), FlowPerformer<F> {

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