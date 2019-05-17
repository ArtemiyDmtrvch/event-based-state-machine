package ru.impression.flow_architecture

import android.app.Application
import android.arch.lifecycle.AndroidViewModel

abstract class FlowAndroidViewModel<F : Flow>(
    application: Application,
    override val flowClass: Class<F>
) : AndroidViewModel(application), IFlowViewModel<F> {

    override var flowHashCode: Int? = null

    override val savedViewAcquiredStates = HashMap<String, Any>()

    override var needToRestoreView: Boolean = false

    override fun onCleared() {
        detachFromFlow()
        super.onCleared()
    }
}