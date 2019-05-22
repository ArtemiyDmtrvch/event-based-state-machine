package ru.impression.flow_architecture

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import java.util.*

abstract class FlowAndroidViewModel<F : Flow>(
    application: Application,
    override val groupUUID: UUID
) : AndroidViewModel(application), FlowPerformer<F> {

    override val flow = super.flow

    final override fun attachToFlow() = super.attachToFlow()

    init {
        attachToFlow()
    }

    override fun onCleared() {
        detachFromFlow()
        super.onCleared()
    }
}