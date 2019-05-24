package ru.impression.flow_architecture.mvvm_impl

import android.arch.lifecycle.ViewModel

class ViewStateSavingViewModel : ViewModel() {

    val savedViewAdditionalStates = HashMap<String, Any>()
}