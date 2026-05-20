package com.berling.marketplace.data.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class SendOtpEmailRequest(
    val email: String,
    val type: String = "signup" // signup, password_reset
)

@Serializable
data class SendEmailResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class MessageRequest(
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: String
)

@Serializable
data class MessageResponse(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: String,
    val isRead: Boolean = false
)

@Serializable
data class CreateOrderRequest(
    val productId: String,
    val productTitle: String,
    val quantity: Int,
    val price: Double,
    val buyerId: String,
    val sellerId: String
)

@Serializable
data class PaymentInitiateRequest(
    val orderId: String,
    val amount: Double,
    val currency: String = "INR"
)

@Serializable
data class PaymentVerificationRequest(
    val orderId: String,
    val paymentId: String,
    val signature: String
)

@Serializable
data class AnalyticsEventRequest(
    val eventName: String,
    val eventData: Map<String, String> = emptyMap(),
    val timestamp: String
)
