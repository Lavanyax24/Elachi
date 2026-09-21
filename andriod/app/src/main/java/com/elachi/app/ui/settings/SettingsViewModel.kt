package com.elachi.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elachi.app.data.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = authRepository.authState
        .flatMapLatest { user -> settingsRepository.isDarkTheme(user?.uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val units: StateFlow<String> = settingsRepository.units
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "metric")

    fun setDarkTheme(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setDarkTheme(authRepository.currentUser?.uid, enabled)
    }

    fun setUnits(units: String) = viewModelScope.launch {
        settingsRepository.setUnits(units)
    }

    fun logout(onDone: () -> Unit) {
        authRepository.signOut()
        onDone()
    }

    fun deleteAccount(api: com.elachi.app.data.remote.ApiService, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                api.deleteAccount()
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Remote account deletion failed", e)
            }
            try {
                authRepository.currentUser?.delete()?.also {
                    it.addOnCompleteListener { task ->
                        authRepository.signOut()
                        onDone()
                    }
                } ?: run {
                    authRepository.signOut()
                    onDone()
                }
            } catch (e: Exception) {
                authRepository.signOut()
                onDone()
            }
        }
    }
}