package ru.impression.flow_architecture

import android.app.Application
import android.arch.lifecycle.AndroidViewModel

abstract class FlowAndroidViewModel<F : Flow>(
    application: Application,
    override val flowHost: FlowHost<F>
) : AndroidViewModel(application), FlowPerformer<F> {

    final override fun attachToFlow() = super.attachToFlow()

    init {
        attachToFlow()
    }

    override fun onCleared() {
        detachFromFlow()
        super.onCleared()
    }
}