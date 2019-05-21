package ru.impression.flow_architecture

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class FlowViewModelFactory(private val application: Application, private val flowHost: FlowHost<*>) :
    ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        when {
            FlowViewModel::class.java.isAssignableFrom(modelClass) ->
                modelClass.getConstructor(FlowHost::class.java).newInstance(flowHost)
            FlowAndroidViewModel::class.java.isAssignableFrom(modelClass) ->
                modelClass
                    .getConstructor(Application::class.java, FlowHost::class.java)
                    .newInstance(application, flowHost)
            else -> super.create(modelClass)
        }


}