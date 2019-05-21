package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

abstract class FlowActivity<F : Flow, S : Any>(override val flowClass: Class<F>) : AppCompatActivity(), FlowView<F, S> {

    override val flowHost by lazy {
        ViewModelProviders.of(
            this,
            FlowHostViewModelFactory(flowClass)
        )[FlowHostViewModel::class.java] as FlowHostViewModel<F>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachToFlow()
    }

    override fun onDestroy() {
        detachFromFlow(isChangingConfigurations)
        super.onDestroy()
    }
}