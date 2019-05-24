package ru.impression.flow_architecture.mvvm_impl

import android.arch.lifecycle.ViewModel
import ru.impression.flow_architecture.Flow
import ru.impression.flow_architecture.FlowPerformer
import java.util.*

abstract class FlowViewModel<F : Flow>(override val groupUUID: UUID) : ViewModel(),
    FlowPerformer<F> {

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