package ru.impression.flow_architecture.consideration

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import ru.impression.flow_architecture.Flow

class FlowViewModelFactory<F : Flow>(private val application: Application, private val flowClass: Class<F>) :
    ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T = when {
        FlowViewModel::class.java.isAssignableFrom(modelClass) ->
            modelClass.getConstructor(flowClass::class.java).newInstance(flowClass)
        FlowAndroidViewModel::class.java.isAssignableFrom(modelClass) ->
            modelClass
                .getConstructor(application::class.java, flowClass::class.java)
                .newInstance(application, flowClass)
        else -> super.create(modelClass)
    }
}