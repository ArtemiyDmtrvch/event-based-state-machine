package ru.impression.flow_architecture.impl

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ru.impression.flow_architecture.Flow
import ru.impression.flow_architecture.FlowEvent
import ru.impression.flow_architecture.FlowPerformer

abstract class FlowActivity<F : Flow>(final override val flowClass: Class<F>) :
    AppCompatActivity(), FlowPerformer<F> {

    final override fun attachToFlow() = super.attachToFlow()

    final override fun eventOccurred(event: FlowEvent) = super.eventOccurred(event)

    final override fun detachFromFlow() = super.detachFromFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachToFlow()
    }

    override fun onDestroy() {
        detachFromFlow()
        super.onDestroy()
    }
}