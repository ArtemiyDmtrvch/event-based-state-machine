package ru.impression.flow_architecture

class FlowHostViewModel<F : Flow>(override val flowClass: Class<F>) : FlowViewModel<F>(flowClass) {

    val savedViewAdditionalStates = HashMap<String, Any>()
}

class FlowHostViewModelFactory