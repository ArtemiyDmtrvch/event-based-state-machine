package ru.impression.flow_architecture.mvvm_impl

import android.app.Application
import ru.impression.flow_architecture.Flow

abstract class FlowAndroidViewModel<F : Flow> : FlowViewModel<F>() {

    lateinit var application: Application
}