package ru.impression.flow_architecture.impl

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import ru.impression.flow_architecture.Flow
import ru.impression.flow_architecture.FlowEvent
import ru.impression.flow_architecture.FlowPerformer

abstract class FlowFragment<F : Flow>(final override val flowClass: Class<F>) : Fragment(), FlowPerformer<F> {

    final override fun attachToFlow() = super.attachToFlow()

    final override fun eventOccurred(event: FlowEvent) = super.eventOccurred(event)

    final override fun detachFromFlow() = super.detachFromFlow()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachToFlow()
    }

    override fun onDestroyView() {
        detachFromFlow()
        super.onDestroyView()
    }
}