package ru.impression.flow_architecture

import android.os.Bundle
import android.support.v4.app.FragmentActivity

abstract class FlowActivity<F : Flow, S : Any>(override val flowClass: Class<F>) : FragmentActivity(), FlowView<F, S> {

    override val groupUUID = super.groupUUID

    override val flow = super.flow

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