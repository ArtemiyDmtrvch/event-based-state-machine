package ru.impression.flow_architecture.mvvm_impl

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import java.util.*

class FlowViewModelFactory(private val application: Application, private val performerGroupUUID: UUID) :
    ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        when {
            FlowAndroidViewModel::class.java.isAssignableFrom(modelClass) ->
                modelClass
                    .getConstructor(Application::class.java, UUID::class.java)
                    .newInstance(application, performerGroupUUID)
            FlowViewModel::class.java.isAssignableFrom(modelClass) ->
                modelClass.getConstructor(UUID::class.java).newInstance(performerGroupUUID)
            else -> super.create(modelClass)
        }
}