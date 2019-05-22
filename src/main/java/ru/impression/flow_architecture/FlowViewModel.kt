package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModel
import java.util.*

abstract class FlowViewModel<F : Flow>(override val groupUUID: UUID) : ViewModel(), FlowPerformer<F> {

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