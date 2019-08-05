package ru.impression.flow_architecture.mvvm_impl

import android.arch.lifecycle.ViewModel
import java.util.*

class ViewStateSavingViewModel<S : Any> : ViewModel() {

    lateinit var performerGroupUUID: UUID

    var additionalViewState: S? = null

    var isCleared = false

    override fun onCleared() {
        isCleared = true
    }
}