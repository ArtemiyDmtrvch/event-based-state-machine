package ru.impression.flow_architecture.impl

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import ru.impression.flow_architecture.Flow
import ru.impression.flow_architecture.RestorableFlowPerformer
import ru.impression.flow_architecture.RestoringRequested
import ru.impression.flow_architecture.StateStore

abstract class RestorableFlowFragment<F : Flow, VM : ViewModel, S : StateStore>(
    flowClass: Class<F>,
    private val viewModelClass: Class<VM>,
    final override val stateStoreClass: Class<S>
) : FlowFragment<F>(flowClass), RestorableFlowPerformer<F, S> {

    lateinit var viewModel: VM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(
            this,
            ViewModelProvider.AndroidViewModelFactory(activity!!.application)
        )[viewModelClass]
        attachToFlow()
        savedInstanceState?.let {
            eventOccurred(
                RestoringRequested(
                    when (viewModel) {
                        is FlowViewModel<*> -> (viewModel as FlowViewModel<*>).stateStore!!
                        is FlowAndroidViewModel<*> -> (viewModel as FlowAndroidViewModel<*>).stateStore!!
                        else -> return
                    }
                )
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (viewModel is FlowViewModel<*>)
            (viewModel as FlowViewModel<*>).apply {
                if (stateStore == null) stateStore = stateStoreClass.newInstance()
                saveState(stateStore as S)
            }
    }
}