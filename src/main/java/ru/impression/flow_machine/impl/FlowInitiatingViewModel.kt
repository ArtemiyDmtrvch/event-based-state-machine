package ru.impression.flow_machine.impl

import android.arch.lifecycle.ViewModel
import io.reactivex.subjects.BehaviorSubject
import ru.impression.flow_machine.Flow
import ru.impression.flow_machine.FlowInitiator
import ru.impression.flow_machine.FlowPerformer

abstract class FlowInitiatingViewModel<F : Flow<*>>(
    final override val flowClass: Class<F>
) : ViewModel(), FlowInitiator<F>, FlowPerformer<F> {

    final override fun startFlow(holdLastEventsCount: Int, holdLastActionsCount: Int) =
        super.startFlow(holdLastEventsCount, holdLastActionsCount)

    final override fun attachToFlow() = super.attachToFlow()

    final override fun eventOccurred(event: Flow.Event) = super.eventOccurred(event)

    final override fun detachFromFlow() = super.detachFromFlow()

    final override fun finishFlow() = super.finishFlow()

    init {
        startFlow()
        attachToFlow()
    }

    override fun onCleared() {
        detachFromFlow()
        finishFlow()
        super.onCleared()
    }
}