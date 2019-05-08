package ru.impression.flow_architecture.impl

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import ru.impression.flow_architecture.Event
import ru.impression.flow_architecture.Flow
import ru.impression.flow_architecture.FlowPerformer

abstract class FlowAndroidViewModel<F : Flow>(
    application: Application,
    final override val flowClass: Class<F>
) : AndroidViewModel(application), FlowPerformer<F> {

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