package ru.impression.flow_machine.impl

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ru.impression.flow_machine.Flow
import ru.impression.flow_machine.FlowInitiator
import ru.impression.flow_machine.FlowPerformer

abstract class FlowInitiatingActivity<F : Flow<*>>(final override val flowClass: Class<F>) :
    AppCompatActivity(), FlowInitiator<F>, FlowPerformer<F> {

    final override fun startFlow(holdLastEventsCount: Int, holdLastActionsCount: Int) =
        super.startFlow(holdLastEventsCount, holdLastActionsCount)

    final override fun attachToFlow() = super.attachToFlow()

    final override fun eventOccurred(event: Flow.Event) = super.eventOccurred(event)

    final override fun detachFromFlow() = super.detachFromFlow()

    final override fun finishFlow() = super.finishFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startFlow()
        attachToFlow()
    }

    override fun onDestroy() {
        detachFromFlow()
        finishFlow()
        super.onDestroy()
    }
}