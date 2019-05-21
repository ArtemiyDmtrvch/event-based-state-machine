package ru.impression.flow_architecture

interface FlowHost<F: Flow> {

    val flow: F
}