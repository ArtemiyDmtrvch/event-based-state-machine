package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

abstract class FlowFragment<F : Flow, S : Any>(
    override val flowClass: Class<F>,
    override val viewModelClass: Class<out ViewModel>? = null
) : Fragment(), FlowView<F, S> {

    override var viewModel: IFlowViewModel<F>? = null

    override var primaryStateIsSet: Boolean = false
        set(value) {
            field = value
            super.primaryStateIsSet = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        FrameLayout(activity!!)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(ViewModelProviders.of(this))
        attachToFlow()
    }

    protected fun setView(layoutResId: Int) =
        setView(View.inflate(activity!!, layoutResId, null))

    protected open fun setView(view: View) {
        if (view is ViewGroup) view.apply {
            removeAllViews()
            addView(view)
        }
        restoreSecondaryState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        saveSecondaryState()
        super.onConfigurationChanged(newConfig)
    }

    override fun onDestroyView() {
        detachFromFlow()
        super.onDestroyView()
    }
}