package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModel

class ViewStateSavingViewModel : ViewModel() {

    val savedViewAdditionalStates = HashMap<String, Any>()
}