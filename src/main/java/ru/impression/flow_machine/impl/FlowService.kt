package ru.impression.flow_machine.impl

import android.app.Service
import ru.impression.flow_machine.Flow
import ru.impression.flow_machine.FlowPerformer

abstract class FlowService<F : Flow<*>>(final override val flowClass: Class<F>) : Service(), FlowPerformer<F> {

    final override fun attachToFlow() = super.attachToFlow()

    final override fun eventOccurred(event: Flow.Event) = super.eventOccurred(event)

    final override fun detachFromFlow() = super.detachFromFlow()

    override fun onCreate() {
        super.onCreate()
        attachToFlow()
    }

    override fun onDestroy() {
        detachFromFlow()
        super.onDestroy()
    }
}