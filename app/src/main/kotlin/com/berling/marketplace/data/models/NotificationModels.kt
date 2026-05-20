package com.berling.marketplace.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Notification(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String, // message, order, payment, achievement
    val relatedId: String? = null, // orderId, messageId, etc
    val isRead: Boolean = false,
    val createdAt: String = ""
)

@Serializable
data class NotificationEvent(
    val id: String,
    val userId: String,
    val eventType: String, // new_message, order_confirmed, payment_received, etc
    val title: String,
    val body: String,
    val data: Map<String, String> = emptyMap(),
    val deepLink: String? = null,
    val createdAt: String = ""
)

@Serializable
data class WebhookPayload(
    val event: String,
    val data: Map<String, JsonElement> = emptyMap(),
    val timestamp: String = "",
    val signature: String = ""
)

@Serializable
data class OrderWebhookPayload(
    val eventType: String, // order.created, order.confirmed, order.shipped, order.delivered
    val orderId: String,
    val buyerId: String,
    val sellerId: String,
    val status: String,
    val amount: Double,
    val productId: String,
    val timestamp: String = ""
)

@Serializable
data class PaymentWebhookPayload(
    val eventType: String, // payment.success, payment.failed, payment.refund
    val paymentId: String,
    val orderId: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val gateway: String,
    val timestamp: String = ""
)
