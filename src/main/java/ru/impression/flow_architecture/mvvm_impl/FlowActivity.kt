package ru.impression.flow_architecture.mvvm_impl

import android.support.v4.app.FragmentActivity
import ru.impression.flow_architecture.Flow

abstract class FlowActivity<F : Flow, S : Any>(override val flowClass: Class<F>) : FragmentActivity(),
    PrimaryFlowView<F, S> {

    override val groupUUID by lazy { super.groupUUID }

    override val retrievedGroupUUID by lazy { super.retrievedGroupUUID }

    override val flow by lazy { super.flow }

    override var disposable = super.disposable

    override fun onResume() {
        super.onResume()
        attachToFlow()
    }

    override fun onPause() {
        temporarilyDetachFromFlow()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        underlay?.viewIsDestroyed?.set(true)
    }

    override fun finish() {
        completelyDetachFromFlow()
        super.finish()
    }
}