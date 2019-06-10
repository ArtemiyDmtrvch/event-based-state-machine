package ru.impression.flow_architecture.mvvm_impl

import android.support.v4.app.FragmentActivity
import ru.impression.flow_architecture.Flow
import java.util.*

abstract class FlowActivity<F : Flow, S : Any>(override val flowClass: Class<F>) : FragmentActivity(),
    PrimaryFlowView<F, S> {

    override lateinit var groupUUID: UUID

    override val flow by lazy { super.flow }

    override var disposable = super.disposable

    override var viewWasDestroyed = super.viewWasDestroyed

    override fun onResume() {
        super.onResume()
        attachToFlow()
    }

    override fun onPause() {
        temporarilyDetachFromFlow()
        super.onPause()
    }

    override fun onDestroy() {
        viewWasDestroyed = true
        completelyDetachFromFlow()
        super.onDestroy()
    }
}