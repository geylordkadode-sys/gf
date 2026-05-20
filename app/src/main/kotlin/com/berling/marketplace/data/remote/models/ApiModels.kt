package com.berling.marketplace.data.remote.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthSignUpRequest(
    val email: String,
    val password: String,
    val name: String = ""
)

@Serializable
data class AuthSignUpResponse(
    val user: AuthUser?,
    val session: AuthSession?,
    val error: ApiError? = null
)

@Serializable
data class AuthUser(
    val id: String,
    val email: String,
    @SerialName("user_metadata")
    val userMetadata: Map<String, String> = emptyMap()
)

@Serializable
data class AuthSession(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    @SerialName("expires_in")
    val expiresIn: Int,
    @SerialName("token_type")
    val tokenType: String = "Bearer",
    val user: AuthUser? = null
)

@Serializable
data class OtpVerifyRequest(
    val email: String,
    val token: String,
    val type: String = "signup"
)

@Serializable
data class PasswordResetRequest(
    val email: String
)

@Serializable
data class PasswordResetConfirmRequest(
    val email: String,
    val token: String,
    val password: String
)

@Serializable
data class UserProfileRequest(
    val name: String = "",
    val country: String = "",
    val location: String = "",
    val profilePhotoUrl: String = "",
    val website: String = "",
    val instagramHandle: String = "",
    val facebookHandle: String = "",
    val twitterHandle: String = ""
)

@Serializable
data class ProductCreateRequest(
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val imageUrl: String,
    val imageUrls: List<String> = emptyList(),
    val brand: String = "",
    val condition: String = "",
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val country: String = "", // New field for country
    val city: String = "", // New field for city
    val deliveryOptions: List<String> = emptyList(),
    val returnPolicy: String = "",
    val tags: List<String> = emptyList(),
    val productAttributes: Map<String, String> = emptyMap(),
    val isNew: Boolean = false,
    val boostListing: Boolean = false,
    val discountPrice: Double = 0.0
)

@Serializable
data class ProductUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val category: String? = null,
    val imageUrl: String? = null,
    val imageUrls: List<String>? = null,
    val isActive: Boolean? = null,
    val brand: String? = null,
    val condition: String? = null,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val country: String? = null, // New field for country
    val city: String? = null, // New field for city
    val deliveryOptions: List<String>? = null,
    val returnPolicy: String? = null,
    val tags: List<String>? = null,
    val productAttributes: Map<String, String>? = null,
    val isNew: Boolean? = null,
    val boostListing: Boolean? = null,
    val discountPrice: Double? = null
)

@Serializable
data class ApiResponse<T>(
    val data: T? = null,
    val error: ApiError? = null
)

@Serializable
data class ApiError(
    val message: String,
    val code: String? = null
)
