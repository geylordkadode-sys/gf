package com.berling.marketplace.data.models

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class KYCApplication(
    val id: String,
    val userId: String,
    val status: String = "pending", // pending, approved, rejected
    val documentType: String = "", // passport, national_id, driver_license
    val documentUrl: String = "",
    val fullName: String = "",
    val address: String = "",
    val dateOfBirth: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class KYCApplicationRequest(
    val documentType: String,
    val documentUrl: String,
    val fullName: String,
    val address: String,
    val dateOfBirth: String
)

@Serializable
data class KYCStatus(
    val status: String = "none", // none, pending, approved, rejected
    val application: KYCApplication? = null,
    val rejectionReason: String? = null
)

@Serializable
data class HighValueProductKYCRequirement(
    val productId: String,
    val price: Double,
    val requiresKYC: Boolean = false,
    val kycStatus: String = "none"
)
