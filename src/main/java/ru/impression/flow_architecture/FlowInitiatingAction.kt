package ru.impression.flow_architecture

abstract class FlowInitiatingAction(val flowClass: Class<out Flow>) : Action()