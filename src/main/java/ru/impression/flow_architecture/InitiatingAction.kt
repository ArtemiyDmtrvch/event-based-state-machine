package ru.impression.flow_architecture

abstract class InitiatingAction(val flowClass: Class<out Flow>) : FlowAction()