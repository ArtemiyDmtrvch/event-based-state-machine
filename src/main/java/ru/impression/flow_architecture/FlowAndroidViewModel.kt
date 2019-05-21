package ru.impression.flow_architecture

import android.app.Application
import android.arch.lifecycle.AndroidViewModel

abstract class FlowAndroidViewModel<F : Flow>(
    application: Application,
    override val flowClass: Class<F>
) : AndroidViewModel(application), IFlowViewModel<F> {

    override var flow: Flow? = null

    override val savedViewAdditionalStates = HashMap<String, Any>()

    final override fun attachToFlow() = super.attachToFlow()

    init {
        attachToFlow()
    }

    override fun onCleared() {
        detachFromFlow()
        super.onCleared()
    }
}