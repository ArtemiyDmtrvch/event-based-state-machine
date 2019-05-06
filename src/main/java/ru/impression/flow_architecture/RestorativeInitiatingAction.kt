package ru.impression.flow_architecture

abstract class RestorativeInitiatingAction<S : StateStore>(flowClass: Class<out Flow>) :
    InitiatingAction(flowClass) {

    var stateStore: S? = null
}