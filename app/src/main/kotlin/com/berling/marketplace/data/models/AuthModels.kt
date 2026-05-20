package com.berling.marketplace.data.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthUser(
    val id: String,
    val email: String,
    val name: String,
    val photoUrl: String = "",
    val role: String = "buyer", // buyer, seller, admin
    val isVerified: Boolean = false,
    val createdAt: String = ""
)

@Serializable
data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val tokenType: String = "Bearer"
)

@Serializable
data class AuthSession(
    val user: AuthUser,
    val token: AuthToken
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val photoUrl: String? = null,
    val bio: String? = null,
    val location: String? = null
)
