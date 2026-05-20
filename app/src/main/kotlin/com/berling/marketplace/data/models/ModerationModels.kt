package com.berling.marketplace.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Report(
    val id: String,
    val reporterId: String,
    val reportedUserId: String? = null,
    val reportedProductId: String? = null,
    val reportedReviewId: String? = null,
    val reportType: String, // user, product, review, message
    val reason: String,
    val status: String = "pending", // pending, reviewed, resolved
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class CreateReportRequest(
    val reportType: String,
    val reason: String,
    val reportedUserId: String? = null,
    val reportedProductId: String? = null,
    val reportedReviewId: String? = null
)

@Serializable
data class Ban(
    val id: String,
    val userId: String,
    val reason: String,
    val bannedUntil: String? = null, // null for permanent ban
    val appealStatus: String = "none", // none, pending, approved, rejected
    val appealMessage: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class CreateBanRequest(
    val userId: String,
    val reason: String,
    val bannedUntil: String? = null
)

@Serializable
data class BanAppeal(
    val banId: String,
    val userId: String,
    val appealMessage: String,
    val status: String = "pending" // pending, approved, rejected
)

@Serializable
data class BanStatus(
    val isBanned: Boolean = false,
    val ban: Ban? = null,
    val remainingBanTime: Long = 0, // in milliseconds
    val canAppeal: Boolean = false,
    val appealStatus: String = "none"
)

@Serializable
data class MessageRejection(
    val id: String,
    val messageId: String,
    val senderId: String,
    val receiverId: String,
    val rejectionReason: String, // link_detected, phone_number_detected, etc.
    val rejectionMessage: String,
    val createdAt: String = ""
)

@Serializable
data class LinkPhoneDetectionResult(
    val hasLinks: Boolean = false,
    val hasPhoneNumbers: Boolean = false,
    val detectedItems: List<String> = emptyList(),
    val rejectionReason: String? = null
)
