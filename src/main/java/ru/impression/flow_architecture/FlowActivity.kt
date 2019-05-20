package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

abstract class FlowActivity<F : Flow, S : Any>(
    override val flowClass: Class<F>,
    override val flowViewModelClass: Class<out ViewModel>? = null
) : AppCompatActivity(), FlowView<F, S> {

    override var flow: Flow? = null

    override var flowViewModel: IFlowViewModel<F>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachToFlow()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        isActive = false
        super.onConfigurationChanged(newConfig)
    }

    override fun onDestroy() {
        detachFromFlow()
        super.onDestroy()
    }
}