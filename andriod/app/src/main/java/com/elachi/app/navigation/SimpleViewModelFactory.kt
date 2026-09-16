package com.elachi.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Small generic factory so each screen can construct its ViewModel with
 * plain constructor arguments (repositories, ids) without pulling in a DI
 * framework.
 */
class SimpleViewModelFactory(private val creator: () -> ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = creator() as T
}