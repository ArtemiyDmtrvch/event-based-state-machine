package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity

interface FlowView<F : Flow, S : Any> : FlowPerformer<F> {

    val flowViewModelClass: Class<out ViewModel>?

    var flowViewModel: IFlowViewModel<F>?

    var acquiredState: S

    override var isActive: Boolean
        get() = super.isActive
        set(value) {
            if (value)
                flowViewModel?.savedViewAcquiredStates?.remove(javaClass.notNullName)?.let { acquiredState = it as S }
            else
                acquiredState.takeIf { it !is Unit && it !is Nothing }?.let {
                    flowViewModel?.savedViewAcquiredStates?.put(javaClass.notNullName, it)
                }
            super.isActive = value
        }

    override fun attachToFlow() {
        flowViewModelClass
            ?.let { clazz ->
                val viewModelProvider = when (this) {
                    is Fragment -> ViewModelProviders.of(this)
                    is FragmentActivity -> ViewModelProviders.of(this)
                    else -> null
                }
                viewModelProvider?.get(clazz)
            }
            ?.let { if (it is IFlowViewModel<*>) flowViewModel = (it as IFlowViewModel<F>) }
        super.attachToFlow(flowViewModel, AttachMode.REPLAY)
    }

    fun initialStateIsSet() {
        isActive = true
    }

    override fun performAction(action: Action) {
        if (!isActive && action !is InitiatingAction) return
    }
}