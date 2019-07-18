package ru.impression.flow_architecture.mvvm_impl

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import java.util.*

class FlowViewModelFactory(private val application: Application, private val performerGroupUUID: UUID) :
    ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        modelClass.newInstance().apply {
            when (this) {
                is FlowViewModel<*> -> {
                    groupUUID = performerGroupUUID
                    if (this is FlowAndroidViewModel<*>) application = this@FlowViewModelFactory.application
                    init()
                }
                is ViewStateSavingViewModel<*> -> performerGroupUUID = this@FlowViewModelFactory.performerGroupUUID
            }
        }
}