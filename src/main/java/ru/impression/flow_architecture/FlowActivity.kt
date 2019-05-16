package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

abstract class FlowActivity<F : Flow, S : Any>(
    final override val flowClass: Class<F>,
    final override val viewModelClass: Class<out ViewModel>? = null
) : AppCompatActivity(), FlowView<F, S> {

    final override var viewModel: IFlowViewModel<F>? = null

    final override fun attachToFlow() = super.attachToFlow()

    final override fun eventOccurred(event: Event) = super.eventOccurred(event)

    final override fun detachFromFlow() = super.detachFromFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init(ViewModelProviders.of(this))
        attachToFlow()
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        restoreSecondaryState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        saveSecondaryState()
        super.onConfigurationChanged(newConfig)
    }

    override fun onDestroy() {
        detachFromFlow()
        super.onDestroy()
    }
}