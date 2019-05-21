package ru.impression.flow_architecture

internal object FlowProvider {

    operator fun <F : Flow> get(flowClass: Class<F>): Flow = FlowStore.waitingFlows
        .firstOrNull { it::class.java == flowClass }
        ?.also { FlowStore.waitingFlows.remove(it) }
        ?: flowClass.newInstance()
}