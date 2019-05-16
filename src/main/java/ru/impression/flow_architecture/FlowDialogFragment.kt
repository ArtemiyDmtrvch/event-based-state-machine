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
    final override val flowClass: Class<F>,
    final override val viewModelClass: Class<out ViewModel>? = null
) : DialogFragment(), FlowView<F, S> {

    final override var viewModel: IFlowViewModel<F>? = null

    final override fun attachToFlow() = super.attachToFlow()

    final override fun eventOccurred(event: Event) = super.eventOccurred(event)

    final override fun detachFromFlow() = super.detachFromFlow()

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
        (view as ViewGroup).apply {
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