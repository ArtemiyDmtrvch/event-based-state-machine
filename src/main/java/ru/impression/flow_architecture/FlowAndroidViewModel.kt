package ru.impression.flow_architecture

import android.app.Application
import android.arch.lifecycle.AndroidViewModel

abstract class FlowAndroidViewModel<F : Flow>(
    application: Application,
    override val flowClass: Class<F>
) : AndroidViewModel(application), IFlowViewModel<F> {

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