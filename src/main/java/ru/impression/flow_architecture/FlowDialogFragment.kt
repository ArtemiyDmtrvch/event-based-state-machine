package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

abstract class FlowDialogFragment<F : Flow, S : Any>(
    override val flowClass: Class<F>,
    override val viewModelClass: Class<out ViewModel>? = null
) : DialogFragment(), FlowView<F, S> {

    override var viewModel: IFlowViewModel<F>? = null

    override var initialStateIsSet: Boolean = super.initialStateIsSet
        set(value) {
            field = value
            super.initialStateIsSet = value
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
        this.view.apply {
            if (this is ViewGroup) {
                removeAllViews()
                addView(view)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        saveAcquiredState()
        super.onConfigurationChanged(newConfig)
    }

    override fun onDestroyView() {
        detachFromFlow()
        super.onDestroyView()
    }
}