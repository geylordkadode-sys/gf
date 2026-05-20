package com.berling.marketplace.data.repository

import android.content.Context
import com.berling.marketplace.data.local.SecurePreferences
import com.berling.marketplace.data.models.AuthSession
import com.berling.marketplace.data.models.AuthUser
import com.berling.marketplace.data.models.AuthToken
import com.berling.marketplace.data.models.LoginRequest
import com.berling.marketplace.data.models.RegisterRequest
import com.berling.marketplace.data.remote.SupabaseApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class AuthenticationRepository @Inject constructor(
    private val api: SupabaseApi,
    @ApplicationContext private val context: Context
) {
    private val securePrefs = SecurePreferences(context)

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _authToken = MutableStateFlow<AuthToken?>(null)
    val authToken: StateFlow<AuthToken?> = _authToken

    init {
        // Check if user is already logged in
        val savedUser = securePrefs.getUser()
        val savedToken = securePrefs.getToken()
        
        if (savedUser != null && savedToken != null && !securePrefs.isTokenExpired()) {
            _currentUser.value = savedUser
            _authToken.value = savedToken
            _isAuthenticated.value = true
        }
    }

    suspend fun login(email: String, password: String): Result<AuthSession> {
        return try {
            val response = api.loginWithEmail(mapOf("email" to email, "password" to password))
            
            val remoteUser = response.user ?: throw Exception("User data not found")
            
            val user = AuthUser(
                id = remoteUser.id,
                email = remoteUser.email,
                name = remoteUser.userMetadata["name"] ?: "",
                photoUrl = remoteUser.userMetadata["photo_url"] ?: "",
                role = remoteUser.userMetadata["role"] ?: "buyer",
                isVerified = true
            )
            
            val token = AuthToken(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken ?: "",
                expiresIn = response.expiresIn,
                tokenType = response.tokenType
            )
            
            val session = AuthSession(user, token)
            
            _currentUser.value = user
            _authToken.value = token
            _isAuthenticated.value = true
            
            securePrefs.saveUser(user)
            securePrefs.saveToken(token)
            
            Result.success(session)
        } catch (e: Exception) {
            _isAuthenticated.value = false
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, name: String): Result<AuthSession> {
        return try {
            val signUpResponse = api.signUp(
                com.berling.marketplace.data.remote.models.AuthSignUpRequest(email, password, name)
            )
            
            if (signUpResponse.error != null) {
                return Result.failure(Exception(signUpResponse.error.message))
            }

            val remoteUser = signUpResponse.user ?: throw Exception("User data not found after signup")
            val remoteSession = signUpResponse.session

            val user = AuthUser(
                id = remoteUser.id,
                email = remoteUser.email,
                name = name,
                role = "buyer",
                isVerified = false
            )
            
            val token = if (remoteSession != null) {
                AuthToken(
                    accessToken = remoteSession.accessToken,
                    refreshToken = remoteSession.refreshToken ?: "",
                    expiresIn = remoteSession.expiresIn,
                    tokenType = remoteSession.tokenType
                )
            } else {
                AuthToken("", "", 0)
            }
            
            val session = AuthSession(user, token)
            
            if (remoteSession != null) {
                _currentUser.value = user
                _authToken.value = token
                _isAuthenticated.value = true
                securePrefs.saveUser(user)
                securePrefs.saveToken(token)
            }
            
            Result.success(session)
        } catch (e: Exception) {
            _isAuthenticated.value = false
            Result.failure(e)
        }
    }

    suspend fun logout() {
        _currentUser.value = null
        _authToken.value = null
        _isAuthenticated.value = false
        securePrefs.logout()
    }

    suspend fun updateProfile(updates: Map<String, String>): Result<AuthUser> {
        return try {
            val currentUser = _currentUser.value ?: return Result.failure(Exception("No user logged in"))
            
            val updatedUser = currentUser.copy(
                name = updates["name"] ?: currentUser.name,
                photoUrl = updates["photoUrl"] ?: currentUser.photoUrl
            )
            
            _currentUser.value = updatedUser
            securePrefs.saveUser(updatedUser)
            
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserOrNull(): AuthUser? = _currentUser.value

    fun getAccessToken(): String? = _authToken.value?.accessToken

    suspend fun refreshToken(): Boolean {
        return try {
            val currentToken = _authToken.value ?: return false
            // In a real implementation, call refresh endpoint
            // For now, just validate expiry
            !securePrefs.isTokenExpired()
        } catch (e: Exception) {
            false
        }
    }
}
