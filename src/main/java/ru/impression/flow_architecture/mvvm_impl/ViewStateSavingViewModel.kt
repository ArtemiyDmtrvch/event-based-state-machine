package ru.impression.flow_architecture.mvvm_impl

import android.arch.lifecycle.ViewModel
import java.util.*

class ViewStateSavingViewModel<S: Any>(val performerGroupUUID: UUID) : ViewModel() {

    var additionalViewState: S? = null

    var isCleared = false

    override fun onCleared() {
        isCleared = true
    }
}