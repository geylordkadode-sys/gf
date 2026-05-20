package com.berling.marketplace.data.services

import com.berling.marketplace.data.models.OrderWebhookPayload
import com.berling.marketplace.data.models.PaymentWebhookPayload
import com.berling.marketplace.data.repository.OrderRepository
import com.berling.marketplace.data.repository.NotificationRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject

class WebhookService @Inject constructor(
    private val orderRepository: OrderRepository,
    private val notificationRepository: NotificationRepository
) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun handleOrderWebhook(payload: String): Boolean {
        return try {
            val webhookData = json.decodeFromString(OrderWebhookPayload.serializer(), payload)
            
            when (webhookData.eventType) {
                "order.created" -> {
                    notificationRepository.notifyOrderCreated(
                        webhookData.orderId,
                        webhookData.buyerId,
                        webhookData.sellerId,
                        webhookData.amount
                    )
                }
                "order.confirmed" -> {
                    orderRepository.updateOrderStatus(webhookData.orderId, "paid")
                }
                "order.shipped" -> {
                    notificationRepository.notifyOrderShipped(
                        webhookData.orderId,
                        webhookData.buyerId,
                        "TRACKING_NUM_${System.currentTimeMillis()}"
                    )
                }
                "order.delivered" -> {
                    orderRepository.updateOrderStatus(webhookData.orderId, "delivered")
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun handlePaymentWebhook(payload: String): Boolean {
        return try {
            val webhookData = json.decodeFromString(PaymentWebhookPayload.serializer(), payload)
            
            when (webhookData.eventType) {
                "payment.success" -> {
                    orderRepository.updateOrderStatus(webhookData.orderId, "paid")
                    notificationRepository.notifyPaymentReceived(
                        webhookData.orderId,
                        "SELLER_ID", // Would need seller info
                        webhookData.amount
                    )
                }
                "payment.failed" -> {
                    // Handle payment failure
                }
                "payment.refund" -> {
                    // Handle refund
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun verifyWebhookSignature(payload: String, signature: String, secret: String): Boolean {
        // Implement HMAC verification based on payment gateway
        return try {
            val computed = javax.crypto.Mac.getInstance("HmacSHA256").apply {
                val secretKeySpec = javax.crypto.spec.SecretKeySpec(
                    secret.toByteArray(),
                    0,
                    secret.length,
                    "HmacSHA256"
                )
                init(secretKeySpec)
            }.doFinal(payload.toByteArray())
            
            val computedSignature = java.util.Base64.getEncoder().encodeToString(computed)
            computedSignature == signature
        } catch (e: Exception) {
            false
        }
    }
}
