package com.elachi.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// This class handles the creation of view models.

class SimpleViewModelFactory(private val creator: () -> ViewModel) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = creator() as T
}