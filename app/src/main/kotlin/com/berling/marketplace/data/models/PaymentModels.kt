package com.berling.marketplace.data.models

import kotlinx.serialization.Serializable

@Serializable
data class PaymentGateway(
    val id: String,
    val name: String, // razorpay, stripe, paypal, etc
    val isActive: Boolean,
    val config: Map<String, String> = emptyMap()
)

@Serializable
data class PaymentToken(
    val id: String,
    val userId: String,
    val gateway: String,
    val tokenValue: String,
    val last4Digits: String = "",
    val cardBrand: String = "", // visa, mastercard, amex, etc
    val expiryMonth: Int = 0,
    val expiryYear: Int = 0,
    val isDefault: Boolean = false,
    val createdAt: String = ""
)

@Serializable
data class PaymentAuthorization(
    val id: String,
    val userId: String,
    val gateway: String,
    val authToken: String,
    val apiKey: String = "",
    val apiSecret: String = "",
    val webhookSecret: String = "",
    val isActive: Boolean = false,
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: String = ""
)

@Serializable
data class PaymentRequest(
    val orderId: String,
    val amount: Double,
    val currency: String = "INR",
    val paymentMethodId: String? = null,
    val saveCard: Boolean = false
)

@Serializable
data class PaymentResponse(
    val paymentId: String,
    val orderId: String,
    val status: String, // pending, completed, failed
    val amount: Double,
    val currency: String,
    val gateway: String,
    val transactionId: String? = null,
    val errorMessage: String? = null,
    val createdAt: String = ""
)

@Serializable
data class RazorpayConfig(
    val keyId: String,
    val keySecret: String,
    val webhookUrl: String = ""
)

@Serializable
data class StripeConfig(
    val publishableKey: String,
    val secretKey: String,
    val webhookSecret: String = ""
)

@Serializable
data class PayPalConfig(
    val clientId: String,
    val clientSecret: String,
    val webhookId: String = ""
)
