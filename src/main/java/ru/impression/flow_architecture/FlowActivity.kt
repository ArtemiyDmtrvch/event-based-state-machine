package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

abstract class FlowActivity<F : Flow, S : Any>(
    final override val flowClass: Class<F>,
    final override val viewModelClass: Class<out ViewModel>? = null
) : AppCompatActivity(), FlowView<F, S> {

    final override var viewModel: IFlowViewModel<F>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initWithViewModel(ViewModelProviders.of(this))
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