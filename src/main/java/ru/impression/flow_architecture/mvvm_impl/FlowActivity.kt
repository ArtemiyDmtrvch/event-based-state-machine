package ru.impression.flow_architecture.mvvm_impl

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import ru.impression.flow_architecture.Flow
import java.util.*

abstract class FlowActivity<F : Flow, S : Any>(override val flowClass: Class<F>) : FragmentActivity(),
    PrimaryFlowView<F, S> {

    override lateinit var groupUUID: UUID

    override val flow by lazy { super.flow }

    override var isTemporarilyDestroying: Boolean = super.isTemporarilyDestroying

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachToFlow()
    }

    override fun onDestroy() {
        if (isTemporarilyDestroying)
            temporarilyDetachFromFlow()
        else
            detachFromFlow()
        super.onDestroy()
    }
}