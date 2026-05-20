package com.berling.marketplace.data.repository

import com.berling.marketplace.config.SupabaseConfig
import com.berling.marketplace.config.SupabaseEndpoints
import com.berling.marketplace.data.local.MarketplaceDatabase
import com.berling.marketplace.data.models.KYCApplication
import com.berling.marketplace.data.models.KYCApplicationRequest
import com.berling.marketplace.data.models.KYCStatus
import com.berling.marketplace.data.remote.SupabaseApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

/**
 * Repository for KYC (Know Your Customer) application management
 */
class KYCRepository(
    private val supabaseApi: SupabaseApi,
    private val database: MarketplaceDatabase
) {

    private val _kycStatusState = MutableStateFlow<KYCStatus>(KYCStatus())
    val kycStatusState = _kycStatusState.asStateFlow()

    /**
     * Submit KYC application for high-value products (>$1000)
     */
    suspend fun submitKYCApplication(
        userId: String,
        request: KYCApplicationRequest
    ): Result<KYCApplication> = try {
        val response = supabaseApi.createKYCApplication(
            mapOf(
                "user_id" to userId,
                "document_type" to request.documentType,
                "document_url" to request.documentUrl,
                "full_name" to request.fullName,
                "address" to request.address,
                "date_of_birth" to request.dateOfBirth,
                "status" to "pending"
            )
        )
        
        val application = KYCApplication(
            id = response["id"] as? String ?: "",
            userId = userId,
            status = "pending",
            documentType = request.documentType,
            documentUrl = request.documentUrl,
            fullName = request.fullName,
            address = request.address,
            dateOfBirth = request.dateOfBirth,
            createdAt = response["created_at"] as? String ?: ""
        )
        
        Result.success(application)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get KYC application status for user
     */
    suspend fun getKYCStatus(userId: String): Result<KYCStatus> = try {
        val response = supabaseApi.getKYCApplication(userId)
        
        if (response.isNotEmpty()) {
            val application = KYCApplication(
                id = response[0]["id"] as? String ?: "",
                userId = userId,
                status = response[0]["status"] as? String ?: "pending",
                documentType = response[0]["document_type"] as? String ?: "",
                documentUrl = response[0]["document_url"] as? String ?: "",
                fullName = response[0]["full_name"] as? String ?: "",
                address = response[0]["address"] as? String ?: "",
                dateOfBirth = response[0]["date_of_birth"] as? String ?: "",
                createdAt = response[0]["created_at"] as? String ?: ""
            )
            
            val status = KYCStatus(
                status = application.status,
                application = application
            )
            
            _kycStatusState.value = status
            Result.success(status)
        } else {
            Result.success(KYCStatus(status = "none"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Check if product requires KYC verification (price > $1000)
     */
    fun requiresKYCVerification(productPrice: Double): Boolean {
        return productPrice > 1000.0
    }

    /**
     * Check if user can sell high-value products
     */
    suspend fun canSellHighValueProduct(userId: String): Boolean {
        val kycStatus = getKYCStatus(userId).getOrNull()
        return kycStatus?.status == "approved"
    }

    /**
     * Get KYC application flow for user
     */
    fun getKYCApplicationFlow(userId: String): Flow<KYCStatus> = flow {
        try {
            val response = supabaseApi.getKYCApplication(userId)
            
            if (response.isNotEmpty()) {
                val application = KYCApplication(
                    id = response[0]["id"] as? String ?: "",
                    userId = userId,
                    status = response[0]["status"] as? String ?: "pending",
                    documentType = response[0]["document_type"] as? String ?: "",
                    documentUrl = response[0]["document_url"] as? String ?: "",
                    fullName = response[0]["full_name"] as? String ?: "",
                    address = response[0]["address"] as? String ?: "",
                    dateOfBirth = response[0]["date_of_birth"] as? String ?: "",
                    createdAt = response[0]["created_at"] as? String ?: ""
                )
                
                emit(KYCStatus(status = application.status, application = application))
            } else {
                emit(KYCStatus(status = "none"))
            }
        } catch (e: Exception) {
            emit(KYCStatus(status = "error"))
        }
    }

    /**
     * Update KYC application status (admin only)
     */
    suspend fun updateKYCApplicationStatus(
        applicationId: String,
        status: String,
        rejectionReason: String? = null
    ): Result<Boolean> = try {
        val updateData = mutableMapOf(
            "status" to status,
            "updated_at" to System.currentTimeMillis().toString()
        )
        
        if (rejectionReason != null) {
            updateData["rejection_reason"] = rejectionReason
        }
        
        supabaseApi.updateKYCApplication(applicationId, updateData)
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get all pending KYC applications (admin only)
     */
    suspend fun getPendingKYCApplications(): Result<List<KYCApplication>> = try {
        val response = supabaseApi.getPendingKYCApplications()
        
        val applications = response.map { item ->
            KYCApplication(
                id = item["id"] as? String ?: "",
                userId = item["user_id"] as? String ?: "",
                status = item["status"] as? String ?: "pending",
                documentType = item["document_type"] as? String ?: "",
                documentUrl = item["document_url"] as? String ?: "",
                fullName = item["full_name"] as? String ?: "",
                address = item["address"] as? String ?: "",
                dateOfBirth = item["date_of_birth"] as? String ?: "",
                createdAt = item["created_at"] as? String ?: ""
            )
        }
        
        Result.success(applications)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
