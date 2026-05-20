package com.berling.marketplace.ui.screens.profile

import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.berling.marketplace.data.models.AuthUser
import com.berling.marketplace.data.repository.AuthenticationRepository
import com.berling.marketplace.ui.screens.BaseViewModel
import com.berling.marketplace.ui.screens.UiState
import com.berling.marketplace.utils.ImageUploadUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthenticationRepository,
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    private val _userState = MutableStateFlow<UiState<AuthUser>>(UiState.Loading)
    val userState: StateFlow<UiState<AuthUser>> = _userState

    private val _imageUploadProgress = MutableStateFlow(0)
    val imageUploadProgress: StateFlow<Int> = _imageUploadProgress

    private val _profileUpdateState = MutableStateFlow<UiState<AuthUser>>(UiState.Loading)
    val profileUpdateState: StateFlow<UiState<AuthUser>> = _profileUpdateState

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val user = authRepository.getCurrentUserOrNull()
        if (user != null) {
            _userState.value = UiState.Success(user)
        } else {
            _userState.value = UiState.Error("Not logged in")
        }
    }

    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            _imageUploadProgress.emit(10)
            
            val compressedFile = ImageUploadUtil.compressImage(context, imageUri, 800, 800)
            if (compressedFile == null) {
                logError("Failed to compress image")
                return@launch
            }

            _imageUploadProgress.emit(50)

            // Simulate upload progress
            for (progress in 50..100 step 10) {
                _imageUploadProgress.emit(progress)
                kotlinx.coroutines.delay(100)
            }

            try {
                val photoUrl = "file://${compressedFile.absolutePath}"
                
                val result = authRepository.updateProfile(
                    mapOf("photoUrl" to photoUrl)
                )

                result.onSuccess { user ->
                    _userState.emit(UiState.Success(user))
                    logInfo("Profile image updated successfully")
                }
                result.onFailure { error ->
                    logError("Failed to update profile: ${error.message}")
                }
            } catch (e: Exception) {
                logError("Upload failed: ${e.message}")
            }
        }
    }

    fun updateProfile(name: String? = null, bio: String? = null) {
        viewModelScope.launch {
            _profileUpdateState.emit(UiState.Loading)
            
            try {
                val updates = mutableMapOf<String, String>()
                name?.let { updates["name"] = it }
                bio?.let { updates["bio"] = it }

                val result = authRepository.updateProfile(updates)
                result.onSuccess { user ->
                    _profileUpdateState.emit(UiState.Success(user))
                    _userState.emit(UiState.Success(user))
                }
                result.onFailure { error ->
                    _profileUpdateState.emit(UiState.Error(error.message ?: "Update failed"))
                }
            } catch (e: Exception) {
                _profileUpdateState.emit(UiState.Error(e.message ?: "Unknown error"))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            logInfo("User logged out")
        }
    }
}
