package com.berling.marketplace.data.services

import android.util.Log
import com.berling.marketplace.data.models.OrderWebhookPayload
import com.berling.marketplace.data.models.PaymentWebhookPayload
import com.berling.marketplace.data.repository.OrderRepository
import com.berling.marketplace.data.repository.NotificationRepository
import com.berling.marketplace.data.repository.AnalyticsRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.security.MessageDigest
import java.util.HexFormat
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class WebhookHandler @Inject constructor(
    private val orderRepository: OrderRepository,
    private val notificationRepository: NotificationRepository,
    private val analyticsRepository: AnalyticsRepository
) {
    companion object {
        private const val TAG = "WebhookHandler"
        private const val RAZORPAY_SIGNATURE_HEADER = "X-Razorpay-Signature"
        private const val STRIPE_SIGNATURE_HEADER = "Stripe-Signature"
    }

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun handleRazorpayWebhook(
        payload: String,
        signature: String,
        webhookSecret: String
    ): Result<Boolean> = runCatching {
        // Verify Razorpay signature
        if (!verifyRazorpaySignature(payload, signature, webhookSecret)) {
            Log.w(TAG, "Invalid Razorpay signature")
            return@runCatching false
        }

        val jsonPayload = Json.parseToJsonElement(payload).jsonObject
        val event = jsonPayload["event"]?.jsonPrimitive?.content ?: return@runCatching false
        val data = jsonPayload["payload"]?.jsonObject?.get("payment")?.jsonObject

        data?.let {
            val paymentId = it["id"]?.jsonPrimitive?.content ?: ""
            val orderId = it["notes"]?.jsonObject?.get("orderId")?.jsonPrimitive?.content ?: ""
            val amount = it["amount"]?.jsonPrimitive?.content ?: "0"
            val status = it["status"]?.jsonPrimitive?.content ?: "unknown"

            when (event) {
                "payment.authorized" -> {
                    Log.d(TAG, "Payment authorized: $paymentId for order: $orderId")
                    orderRepository.updateOrderStatus(orderId, "payment_authorized")
                    analyticsRepository.logEvent(
                        "payment_authorized",
                        "gateway=razorpay&paymentId=$paymentId",
                        orderId
                    )
                }
                "payment.failed" -> {
                    Log.d(TAG, "Payment failed: $paymentId for order: $orderId")
                    orderRepository.updateOrderStatus(orderId, "payment_failed")
                    analyticsRepository.logEvent(
                        "payment_failed",
                        "gateway=razorpay&paymentId=$paymentId",
                        orderId
                    )
                }
                "payment.captured" -> {
                    Log.d(TAG, "Payment captured: $paymentId for order: $orderId, amount: $amount")
                    orderRepository.updateOrderStatus(orderId, "paid")
                    analyticsRepository.logEvent(
                        "payment_captured",
                        "gateway=razorpay&paymentId=$paymentId&amount=$amount",
                        orderId
                    )
                }
            }
        }
        true
    }

    suspend fun handleStripeWebhook(
        payload: String,
        signature: String,
        webhookSecret: String
    ): Result<Boolean> = runCatching {
        // Verify Stripe signature
        if (!verifyStripeSignature(payload, signature, webhookSecret)) {
            Log.w(TAG, "Invalid Stripe signature")
            return@runCatching false
        }

        val jsonPayload = Json.parseToJsonElement(payload).jsonObject
        val type = jsonPayload["type"]?.jsonPrimitive?.content ?: ""
        val data = jsonPayload["data"]?.jsonObject?.get("object")?.jsonObject

        data?.let {
            val paymentIntentId = it["id"]?.jsonPrimitive?.content ?: ""
            val orderId = it["metadata"]?.jsonObject?.get("orderId")?.jsonPrimitive?.content ?: ""
            val status = it["status"]?.jsonPrimitive?.content ?: "unknown"

            when (type) {
                "payment_intent.succeeded" -> {
                    Log.d(TAG, "Stripe payment succeeded: $paymentIntentId for order: $orderId")
                    orderRepository.updateOrderStatus(orderId, "paid")
                    analyticsRepository.logEvent(
                        "payment_succeeded",
                        "gateway=stripe&paymentIntentId=$paymentIntentId",
                        orderId
                    )
                }
                "payment_intent.payment_failed" -> {
                    Log.d(TAG, "Stripe payment failed: $paymentIntentId for order: $orderId")
                    orderRepository.updateOrderStatus(orderId, "payment_failed")
                    analyticsRepository.logEvent(
                        "payment_failed",
                        "gateway=stripe&paymentIntentId=$paymentIntentId",
                        orderId
                    )
                }
                "charge.refunded" -> {
                    Log.d(TAG, "Stripe charge refunded: $paymentIntentId for order: $orderId")
                    orderRepository.updateOrderStatus(orderId, "refunded")
                    analyticsRepository.logEvent(
                        "payment_refunded",
                        "gateway=stripe&paymentIntentId=$paymentIntentId",
                        orderId
                    )
                }
            }
        }
        true
    }

    private fun verifyRazorpaySignature(payload: String, signature: String, secret: String): Boolean {
        return try {
            val mac = Mac.getInstance("HmacSHA256")
            val keySpec = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
            mac.init(keySpec)
            val computedSignature = HexFormat.of().formatHex(mac.doFinal(payload.toByteArray()))
            computedSignature.equals(signature, ignoreCase = true)
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying Razorpay signature", e)
            false
        }
    }

    private fun verifyStripeSignature(payload: String, signature: String, secret: String): Boolean {
        return try {
            val parts = signature.split(",")
            val timestamp = parts.find { it.startsWith("t=") }?.substring(2) ?: return false
            val signatures = parts.filter { it.startsWith("v1=") }.map { it.substring(3) }

            val toSign = "$timestamp.$payload"
            val mac = Mac.getInstance("HmacSHA256")
            val keySpec = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
            mac.init(keySpec)
            val computedSignature = HexFormat.of().formatHex(mac.doFinal(toSign.toByteArray()))

            signatures.any { it.equals(computedSignature, ignoreCase = true) }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying Stripe signature", e)
            false
        }
    }

    suspend fun handlePaymentWebhook(
        gateway: String,
        payload: String,
        signature: String,
        webhookSecret: String
    ): Result<Boolean> = runCatching {
        when (gateway.lowercase()) {
            "razorpay" -> {
                handleRazorpayWebhook(payload, signature, webhookSecret).getOrThrow()
            }
            "stripe" -> {
                handleStripeWebhook(payload, signature, webhookSecret).getOrThrow()
            }
            else -> {
                Log.w(TAG, "Unknown payment gateway: $gateway")
                false
            }
        }
    }
}
