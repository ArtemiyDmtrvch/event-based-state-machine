package ru.impression.flow_architecture.mvvm_impl

import android.arch.lifecycle.ViewModel
import android.os.Handler
import android.os.Looper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.impression.flow_architecture.Flow
import ru.impression.flow_architecture.FlowPerformer
import ru.impression.flow_architecture.notNullName
import java.util.*
import kotlin.concurrent.thread

abstract class FlowViewModel<F : Flow>(override val groupUUID: UUID) : ViewModel(), FlowPerformer<F> {

    override val flow = super.flow

    override var disposable = super.disposable

    var detachmentRequired = false

    init {
        attachToFlow()
    }

    final override fun attachToFlow() = super.attachToFlow()

    override fun onAllActionsPerformed() {
        if (detachmentRequired) {
            completelyDetachFromFlow()
            detachmentRequired = true
        }
    }

    override fun onCleared() {
        if (underlay?.numberOfUnperformedActions == 0)
            completelyDetachFromFlow()
        else
            detachmentRequired = true
    }
}