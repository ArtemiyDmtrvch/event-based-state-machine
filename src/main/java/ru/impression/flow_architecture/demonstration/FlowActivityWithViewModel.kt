package ru.impression.flow_architecture.demonstration

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ru.impression.flow_architecture.Flow
import ru.impression.flow_architecture.FlowPerformer
import ru.impression.flow_architecture.consideration.FlowViewModelFactory

abstract class FlowActivityWithViewModel<F : Flow, M : ViewModel>(
    final override val flowClass: Class<F>,
    private val viewModelClass: Class<M>
) : AppCompatActivity(), FlowPerformer<F> {

    lateinit var viewModel: M

    final override fun attachToFlow() = super.attachToFlow()

    final override fun eventOccurred(event: Flow.Event) = super.eventOccurred(event)

    final override fun detachFromFlow() = super.detachFromFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this,
            FlowViewModelFactory(application, flowClass)
        )[viewModelClass]
        attachToFlow()
    }

    override fun onDestroy() {
        detachFromFlow()
        super.onDestroy()
    }
}