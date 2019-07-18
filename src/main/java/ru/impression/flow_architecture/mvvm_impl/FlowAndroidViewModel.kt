package ru.impression.flow_architecture.mvvm_impl

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import ru.impression.flow_architecture.Flow
import ru.impression.flow_architecture.FlowPerformer
import java.util.*

abstract class FlowAndroidViewModel<F : Flow> : FlowViewModel<F>() {

    lateinit var application: Application
}