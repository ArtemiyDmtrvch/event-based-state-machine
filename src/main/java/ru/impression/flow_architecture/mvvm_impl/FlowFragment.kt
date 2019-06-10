package ru.impression.flow_architecture.mvvm_impl

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import ru.impression.flow_architecture.Flow
import java.util.*

private const val KEY_GROUP_UUID = "GROUP_UUID"

abstract class FlowFragment<F : Flow, S : Any>(private val isGraphical: Boolean = true) : Fragment(), FlowView<F, S> {

    override val groupUUID: UUID by lazy { UUID.fromString(arguments!!.getString(KEY_GROUP_UUID)) }

    override val flow by lazy { super.flow }

    override var disposable = super.disposable

    override var viewWasDestroyed = super.viewWasDestroyed

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        if (isGraphical) FrameLayout(activity!!) else null

    override fun onResume() {
        super.onResume()
        attachToFlow()
    }

    protected open fun setView(layoutResId: Int) =
        setView(View.inflate(activity!!, layoutResId, null))

    protected open fun setView(view: View) {
        this.view.apply {
            if (this is ViewGroup) {
                removeAllViews()
                addView(view)
            }
        }
    }

    override fun onPause() {
        temporarilyDetachFromFlow()
        super.onPause()
    }

    override fun onDestroyView() {
        viewWasDestroyed = true
        super.onDestroyView()
    }

    override fun onDestroy() {
        completelyDetachFromFlow()
        super.onDestroy()
    }

    internal companion object {
        fun <F : Flow, S : Any, T : FlowFragment<F, S>> newInstance(clazz: Class<T>, groupUUID: UUID): T =
            clazz.newInstance().apply { arguments = Bundle().apply { putString(KEY_GROUP_UUID, groupUUID.toString()) } }
    }
}