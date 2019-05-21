package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModel

class FlowHostViewModel<F : Flow>(flowClass: Class<F>) : ViewModel(), FlowHost<F> {

    override val flow = FlowProvider[flowClass]

    val savedViewAdditionalStates = HashMap<String, Any>()
}