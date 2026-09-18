package com.elachi.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elachi.app.data.repository.AuthRepository
import com.elachi.app.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val isNewUser: Boolean) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signUp(firstName: String, surname: String, email: String, password: String, confirmPassword: String) {
        if (firstName.isBlank() || surname.isBlank() || email.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill in all fields.")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters.")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = AuthUiState.Error("Passwords do not match.")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.signUpWithEmail(firstName, surname, email, password)) {
                is AuthResult.Success -> _uiState.value = AuthUiState.Success(result.isNewUser)
                is AuthResult.Error -> _uiState.value = AuthUiState.Error(result.message)
            }
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your email and password.")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.signInWithEmail(email, password)) {
                is AuthResult.Success -> _uiState.value = AuthUiState.Success(result.isNewUser)
                is AuthResult.Error -> _uiState.value = AuthUiState.Error(result.message)
            }
        }
    }

    fun signInWithGoogleToken(idToken: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is AuthResult.Success -> _uiState.value = AuthUiState.Success(result.isNewUser)
                is AuthResult.Error -> _uiState.value = AuthUiState.Error(result.message)
            }
        }
    }

    fun setError(message: String) {
        _uiState.value = AuthUiState.Error(message)
    }

    fun resetState() { _uiState.value = AuthUiState.Idle }
}