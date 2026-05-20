package com.berling.marketplace.data.repository

import com.berling.marketplace.data.local.UserDao
import com.berling.marketplace.data.local.entities.UserEntity
import com.berling.marketplace.data.remote.SupabaseApi
import com.berling.marketplace.data.remote.models.*
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class AuthRepository @Inject constructor(
    private val api: SupabaseApi,
    private val userDao: UserDao
) {
    suspend fun signUpWithEmail(email: String, password: String, name: String): Result<AuthSession> {
        return try {
            val response = api.signUp(AuthSignUpRequest(email, password, name))
            if (response.session != null) {
                val user = UserEntity(
                    id = response.user?.id ?: "",
                    email = email,
                    password = password,
                    name = name,
                    isSynced = false,
                    createdAt = System.currentTimeMillis().toString()
                )
                userDao.insertUser(user)
                Result.success(response.session)
            } else {
                Result.failure(Exception("Sign up failed: ${response.error?.message ?: "Unknown error"}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sign up error: ${e.message}"))
        }
    }

    suspend fun verifyOtp(email: String, otp: String): Result<AuthSession> {
        return try {
            val response = api.verifyOtp(OtpVerifyRequest(email, otp, "signup"))
            if (response.session != null) {
                Result.success(response.session)
            } else {
                Result.failure(Exception("OTP verification failed: ${response.error?.message ?: "Invalid OTP"}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("OTP verification error: ${e.message}"))
        }
    }

    suspend fun resetPassword(emailOrPhone: String): Result<Unit> {
        return try {
            val response = api.resetPassword(PasswordResetRequest(emailOrPhone))
            if (response.error == null) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.error.message))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Password reset error: ${e.message}"))
        }
    }

    suspend fun confirmPasswordReset(emailOrPhone: String, otp: String, newPassword: String): Result<AuthSession> {
        return try {
            val response = api.verifyPasswordReset(PasswordResetConfirmRequest(emailOrPhone, otp, newPassword))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("Password reset confirmation error: ${e.message}"))
        }
    }

    suspend fun loginWithEmail(emailOrPhone: String, password: String): Result<AuthSession> {
        return try {
            val response = api.loginWithEmail(mapOf("email" to emailOrPhone, "password" to password))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("Login error: ${e.message}"))
        }
    }

    suspend fun updateUserProfile(token: String, profile: UserProfileRequest): Result<Unit> {
        return try {
            val response = api.updateUserProfile("Bearer $token", profile)
            if (response.error == null) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.error.message))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Profile update error: ${e.message}"))
        }
    }

    fun getUserProfile(userId: String): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)

    suspend fun getUserById(userId: String): UserEntity? = userDao.getUserById(userId)
}
