package ru.impression.flow_architecture

abstract class ResultingEvent : Event() {

    internal var occurredInChildFlow = false
}