package com.berling.marketplace.data.repository

import com.berling.marketplace.data.local.SecurePreferences
import com.berling.marketplace.data.models.PaymentToken
import com.berling.marketplace.data.models.PaymentRequest
import com.berling.marketplace.data.models.PaymentResponse
import com.berling.marketplace.data.models.PaymentAuthorization
import com.berling.marketplace.data.remote.SupabaseApi
import com.berling.marketplace.data.remote.models.PaymentInitiateRequest
import com.berling.marketplace.data.remote.models.PaymentVerificationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class PaymentRepository @Inject constructor(
    private val api: SupabaseApi,
    private val securePrefs: SecurePreferences
) {

    private val _paymentAuthorizations = MutableStateFlow<List<PaymentAuthorization>>(emptyList())
    val paymentAuthorizations: StateFlow<List<PaymentAuthorization>> = _paymentAuthorizations

    private val _paymentTokens = MutableStateFlow<List<PaymentToken>>(emptyList())
    val paymentTokens: StateFlow<List<PaymentToken>> = _paymentTokens

    suspend fun initiatePayment(
        orderId: String,
        amount: Double,
        gateway: String = "razorpay",
        paymentToken: String? = null
    ): Result<PaymentResponse> {
        return try {
            val request = PaymentInitiateRequest(
                orderId = orderId,
                amount = amount,
                currency = "INR"
            )
            
            val response = api.initiatePayment("", request)
            Result.success(
                PaymentResponse(
                    paymentId = response.data?.get("paymentId") ?: "",
                    orderId = orderId,
                    status = "pending",
                    amount = amount,
                    currency = "INR",
                    gateway = gateway,
                    transactionId = response.data?.get("transactionId")
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyPayment(
        paymentId: String,
        orderId: String,
        signature: String
    ): Result<Boolean> {
        return try {
            val request = PaymentVerificationRequest(
                orderId = orderId,
                paymentId = paymentId,
                signature = signature
            )
            
            api.verifyPayment("", request)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun savePaymentAuthorization(
        gateway: String,
        token: String,
        metadata: String = ""
    ) {
        securePrefs.savePaymentToken(gateway, token, metadata)
    }

    fun getPaymentAuthorization(gateway: String): String? {
        return securePrefs.getPaymentToken(gateway)
    }

    fun getPaymentMetadata(gateway: String): String? {
        return securePrefs.getPaymentMetadata(gateway)
    }

    suspend fun refundPayment(paymentId: String, amount: Double): Result<Boolean> {
        return try {
            // Call refund endpoint when available
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPaymentHistory(userId: String, limit: Int = 50): Result<List<PaymentResponse>> {
        return try {
            // Fetch payment history from server
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSupportedGateways(): List<String> {
        return listOf("razorpay", "stripe", "paypal", "paytm", "googlepay", "applepay")
    }

    fun isGatewayAuthorized(gateway: String): Boolean {
        return getPaymentAuthorization(gateway) != null
    }
}
