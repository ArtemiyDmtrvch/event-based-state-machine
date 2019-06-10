package ru.impression.flow_architecture.mvvm_impl

import android.arch.lifecycle.ViewModel
import java.util.*
import kotlin.collections.HashMap

class ViewStateSavingViewModel(val performerGroupUUID: UUID) : ViewModel() {

    val savedViewAdditionalStates = HashMap<String, Any>()

    var isCleared = false

    override fun onCleared() {
        isCleared = true
    }
}