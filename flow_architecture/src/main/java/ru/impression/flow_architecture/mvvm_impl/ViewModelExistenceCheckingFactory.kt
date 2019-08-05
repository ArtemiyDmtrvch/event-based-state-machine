package ru.impression.flow_architecture.mvvm_impl

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import java.lang.UnsupportedOperationException

class ViewModelExistenceCheckingFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): Nothing =
        throw UnsupportedOperationException("ViewModel doesn't exist.")
}