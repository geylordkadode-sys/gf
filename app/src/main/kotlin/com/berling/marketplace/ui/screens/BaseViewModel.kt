package com.berling.marketplace.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val exception: Throwable? = null) : UiState<Nothing>()
}

sealed class UiEvent {
    data class ShowMessage(val message: String) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
}

abstract class BaseViewModel : ViewModel() {
    protected val _events = MutableStateFlow<UiEvent?>(null)
    val events = _events as StateFlow<UiEvent?>

    protected fun emitEvent(event: UiEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    fun clearEvent() {
        viewModelScope.launch {
            _events.emit(null)
        }
    }

    protected fun logError(message: String, throwable: Throwable? = null) {
        android.util.Log.e(this::class.simpleName ?: "ViewModel", message, throwable)
    }

    protected fun logInfo(message: String) {
        android.util.Log.i(this::class.simpleName ?: "ViewModel", message)
    }
}
