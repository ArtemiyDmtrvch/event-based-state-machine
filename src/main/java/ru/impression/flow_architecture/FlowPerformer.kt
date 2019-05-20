package ru.impression.flow_architecture

import java.util.concurrent.ConcurrentLinkedQueue

interface FlowPerformer<F : Flow> {

    val flowClass: Class<F>

    var flow: Flow?

    val eventEnrichers: Array<FlowPerformer<F>> get() = emptyArray()

    var isActive
        get() = false
        set(value) {
            flow?.let { flow ->
                if (value)
                    flow.missedActions.remove(javaClass.notNullName)?.forEach { performAction(it) }
                else
                    flow.missedActions[javaClass.notNullName] = ConcurrentLinkedQueue()
            }
        }

    fun attachToFlow() = attachToFlow(null, AttachMode.CONTINUE)

    fun attachToFlow(primaryPerformer: FlowPerformer<F>?, attachMode: AttachMode) {
        flow = (primaryPerformer?.flow ?: FlowProvider[flowClass]).also {
            it.attachPerformer(this, attachMode)
        }
    }

    fun eventOccurred(event: Event) {
        eventEnrichers.forEach { it.enrichEvent(event) }
        flow?.eventOccurred(event)
    }

    fun enrichEvent(event: Event) = Unit

    fun performAction(action: Action) = Unit

    fun detachFromFlow() {
        flow?.detachPerformer(this)
    }
}