package ru.impression.flow_architecture.mvvm_impl

import android.arch.lifecycle.ViewModel
import ru.impression.flow_architecture.Flow
import ru.impression.flow_architecture.FlowPerformer
import ru.impression.flow_architecture.attachToFlow
import java.util.*

abstract class FlowViewModel<F : Flow>(override val groupUUID: UUID) :
    ViewModel(), FlowPerformer<F, FlowPerformer.Underlay> {

    override val flow = super.flow

    override var disposable = super.disposable

    private var detachmentRequired = false

    init {
        attachToFlow()
    }

    override fun allActionsArePerformed() {
        if (detachmentRequired) {
            completelyDetachFromFlow()
            detachmentRequired = true
        }
    }

    override fun onCleared() {
        if (underlay?.numberOfUnperformedActions?.get() == 0)
            completelyDetachFromFlow()
        else
            detachmentRequired = true
    }
}