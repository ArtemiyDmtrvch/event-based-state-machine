package ru.impression.flow_architecture

import android.app.Application
import android.arch.lifecycle.AndroidViewModel

abstract class FlowAndroidViewModel<F : Flow>(
    application: Application,
    final override val flowClass: Class<F>
) : AndroidViewModel(application), IFlowViewModel<F> {

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