package ru.impression.flow_architecture

/**
 * [Event] that, regardless of where it occurred, is delivered to all existing [Flows][Flow] and can be [observed in
 * them][Flow.whenEventOccurs].
 */
abstract class GlobalEvent : Event() {

    internal var occurred = false
}