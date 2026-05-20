package com.berling.marketplace.ui.screens.auth

import androidx.lifecycle.viewModelScope
import com.berling.marketplace.data.repository.AuthRepository
import com.berling.marketplace.ui.screens.BaseViewModel
import com.berling.marketplace.ui.screens.UiEvent
import com.berling.marketplace.ui.screens.UiState
import com.berling.marketplace.utils.PreferencesUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.content.Context
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    private val _loginState = MutableStateFlow<UiState<Unit>>(UiState.Success(Unit))
    val loginState: StateFlow<UiState<Unit>> = _loginState

    private val _signUpState = MutableStateFlow<UiState<Unit>>(UiState.Success(Unit))
    val signUpState: StateFlow<UiState<Unit>> = _signUpState

    fun signUpWithEmail(email: String, password: String, name: String, context: Context) {
        viewModelScope.launch {
            _signUpState.emit(UiState.Loading)
            val result = authRepository.signUpWithEmail(email, password, name)
            result.onSuccess { session ->
                PreferencesUtil.saveAuthToken(context, session.accessToken)
                PreferencesUtil.saveUserEmail(context, email)
                _signUpState.emit(UiState.Success(Unit))
                emitEvent(UiEvent.Navigate("auth/otp_verify/$email"))
            }.onFailure { e ->
                _signUpState.emit(UiState.Error(e.message ?: "Sign up failed", e))
            }
        }
    }

    fun verifyOtp(email: String, otp: String, context: Context) {
        viewModelScope.launch {
            _loginState.emit(UiState.Loading)
            val result = authRepository.verifyOtp(email, otp)
            result.onSuccess { session ->
                PreferencesUtil.saveAuthToken(context, session.accessToken)
                _loginState.emit(UiState.Success(Unit))
                emitEvent(UiEvent.Navigate("auth/profile_setup"))
            }.onFailure { e ->
                _loginState.emit(UiState.Error(e.message ?: "OTP verification failed", e))
            }
        }
    }

    fun loginWithEmail(emailOrPhone: String, password: String, context: Context) {
        viewModelScope.launch {
            _loginState.emit(UiState.Loading)
            val result = authRepository.loginWithEmail(emailOrPhone, password)
            result.onSuccess { session ->
                PreferencesUtil.saveAuthToken(context, session.accessToken)
                PreferencesUtil.saveUserEmail(context, emailOrPhone)
                _loginState.emit(UiState.Success(Unit))
                emitEvent(UiEvent.Navigate("home"))
            }.onFailure { e ->
                _loginState.emit(UiState.Error(e.message ?: "Login failed", e))
            }
        }
    }

    fun resetPassword(emailOrPhone: String) {
        viewModelScope.launch {
            _loginState.emit(UiState.Loading)
            val result = authRepository.resetPassword(emailOrPhone)
            result.onSuccess {
                _loginState.emit(UiState.Success(Unit))
                emitEvent(UiEvent.ShowMessage("OTP sent to your email"))
            }.onFailure { e ->
                _loginState.emit(UiState.Error(e.message ?: "Reset failed", e))
            }
        }
    }

    fun confirmPasswordReset(emailOrPhone: String, otp: String, newPassword: String) {
        viewModelScope.launch {
            _loginState.emit(UiState.Loading)
            val result = authRepository.confirmPasswordReset(emailOrPhone, otp, newPassword)
            result.onSuccess { session ->
                _loginState.emit(UiState.Success(Unit))
                emitEvent(UiEvent.ShowMessage("Password reset successfully"))
                emitEvent(UiEvent.Navigate("auth/login"))
            }.onFailure { e ->
                _loginState.emit(UiState.Error(e.message ?: "Password reset failed", e))
            }
        }
    }
}
