package ru.impression.flow_architecture

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

abstract class FlowDialogFragment<F : Flow, S : Any>(override val flowClass: Class<F>) :
    DialogFragment(), FlowView<F, S> {

    override val flowHost by lazy {
        ViewModelProviders.of(
            this,
            FlowHostViewModelFactory(flowClass)
        )[FlowHostViewModel::class.java] as FlowHostViewModel<F>
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        FrameLayout(activity!!)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    override fun onDestroyView() {
        detachFromFlow()
        super.onDestroyView()
    }
}