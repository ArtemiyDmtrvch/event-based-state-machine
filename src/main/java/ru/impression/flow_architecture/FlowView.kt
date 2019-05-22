package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import java.lang.UnsupportedOperationException

interface FlowView<F : Flow, S : Any> : PrimaryFlowPerformer<F> {

    var additionalState: S

    val viewStateSavingViewModel: ViewStateSavingViewModel
        get() {
            val viewModelProvider = when (this) {
                is Fragment -> ViewModelProviders.of(this)
                is FragmentActivity -> ViewModelProviders.of(this)
                else -> throw UnsupportedOperationException("FlowView must be either FragmentActivity or Fragment")
            }
            return viewModelProvider[ViewStateSavingViewModel::class.java]
        }

    var isTemporarilyDestroying: Boolean
        get() = false
        set(_) {}

    override fun attachToFlow() {
        super.attachToFlow(
            if (flow.temporarilyDetachedPerformers.contains(javaClass.notNullName))
                FlowPerformer.AttachmentType.REPLAY_ATTACHMENT
            else
                FlowPerformer.AttachmentType.NORMAL_ATTACHMENT
        )
    }

    fun groundStateIsSet() {
        viewStateSavingViewModel.savedViewAdditionalStates
            .remove(javaClass.notNullName)
            ?.let { additionalState = it as S }
        performMissedActions()
    }

    fun temporarilyDetachFromFlow() {
        additionalState
            .takeIf { it !is Unit && it !is Nothing }
            ?.let { viewStateSavingViewModel.savedViewAdditionalStates.put(javaClass.notNullName, it) }
        super.temporarilyDetachFromFlow(true)
    }
}