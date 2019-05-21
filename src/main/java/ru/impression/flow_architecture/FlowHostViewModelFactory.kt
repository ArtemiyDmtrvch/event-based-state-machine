package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

internal class FlowHostViewModelFactory<F : Flow>(private val flowClass: Class<F>) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T = FlowHostViewModel(flowClass) as T
}