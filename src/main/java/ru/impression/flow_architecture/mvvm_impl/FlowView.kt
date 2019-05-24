package ru.impression.flow_architecture.mvvm_impl

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import ru.impression.flow_architecture.Flow
import ru.impression.flow_architecture.FlowPerformer
import ru.impression.flow_architecture.notNullName

interface FlowView<F : Flow, S : Any> : FlowPerformer<F> {

    val flowViewModelClasses get() = emptyArray<Class<out FlowViewModel<F>>>()

    var additionalState: S

    val viewStateSavingViewModel
        get() = getViewModelProvider(ViewModelProvider.NewInstanceFactory())[ViewStateSavingViewModel::class.java]

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
        flowViewModelClasses.forEach { getViewModelProvider()[it] }
    }

    fun getViewModelProvider(factory: ViewModelProvider.Factory? = null) =
        when (this) {
            is Fragment -> ViewModelProviders.of(
                this,
                factory ?: FlowViewModelFactory(activity!!.application, groupUUID)
            )
            is FragmentActivity -> ViewModelProviders.of(
                this,
                factory ?: FlowViewModelFactory(application, groupUUID)
            )
            else -> throw UnsupportedOperationException("FlowView must be either FragmentActivity or Fragment.")
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