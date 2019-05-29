package ru.impression.flow_architecture

abstract class ResultingEvent(numberOfParentRecipients: Int = 1) : Event() {
    var numberOfParentRecipients: Int = numberOfParentRecipients
        internal set
}