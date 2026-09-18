package com.elachi.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elachi.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = settingsRepository.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val units: StateFlow<String> = settingsRepository.units
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "metric")

    fun setDarkTheme(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setDarkTheme(enabled)
    }

    fun setUnits(units: String) = viewModelScope.launch {
        settingsRepository.setUnits(units)
    }

    fun logout(onDone: () -> Unit) {
        authRepository.signOut()
        onDone()
    }
}