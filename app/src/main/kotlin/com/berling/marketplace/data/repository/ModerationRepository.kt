package com.berling.marketplace.data.repository

import com.berling.marketplace.data.local.MarketplaceDatabase
import com.berling.marketplace.data.models.*
import com.berling.marketplace.data.remote.SupabaseApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository for managing moderation: reports, bans, and appeals
 */
class ModerationRepository(
    private val supabaseApi: SupabaseApi,
    private val database: MarketplaceDatabase
) {

    private var linkPhoneWarningCount = mutableMapOf<String, Int>()

    /**
     * Create a report for a user, product, or review
     */
    suspend fun createReport(request: CreateReportRequest, reporterId: String): Result<Report> = try {
        val response = supabaseApi.createReport(
            mapOf(
                "reporter_id" to reporterId,
                "report_type" to request.reportType,
                "reason" to request.reason,
                "reported_user_id" to (request.reportedUserId ?: ""),
                "reported_product_id" to (request.reportedProductId ?: ""),
                "reported_review_id" to (request.reportedReviewId ?: ""),
                "status" to "pending"
            )
        )
        
        val report = Report(
            id = response["id"] as? String ?: "",
            reporterId = reporterId,
            reportedUserId = request.reportedUserId,
            reportedProductId = request.reportedProductId,
            reportedReviewId = request.reportedReviewId,
            reportType = request.reportType,
            reason = request.reason,
            status = "pending",
            createdAt = response["created_at"] as? String ?: ""
        )
        
        Result.success(report)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get user's reports
     */
    fun getUserReports(userId: String): Flow<List<Report>> = flow {
        try {
            val response = supabaseApi.getReports(userId)
            
            val reports = response.map { reportData ->
                Report(
                    id = reportData["id"] as? String ?: "",
                    reporterId = reportData["reporter_id"] as? String ?: "",
                    reportedUserId = reportData["reported_user_id"] as? String,
                    reportedProductId = reportData["reported_product_id"] as? String,
                    reportedReviewId = reportData["reported_review_id"] as? String,
                    reportType = reportData["report_type"] as? String ?: "",
                    reason = reportData["reason"] as? String ?: "",
                    status = reportData["status"] as? String ?: "pending",
                    createdAt = reportData["created_at"] as? String ?: ""
                )
            }
            
            emit(reports)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    /**
     * Create a ban for a user
     */
    suspend fun banUser(request: CreateBanRequest): Result<Ban> = try {
        val response = supabaseApi.createBan(
            mapOf(
                "user_id" to request.userId,
                "reason" to request.reason,
                "banned_until" to (request.bannedUntil ?: ""),
                "appeal_status" to "none"
            )
        )
        
        val ban = Ban(
            id = response["id"] as? String ?: "",
            userId = request.userId,
            reason = request.reason,
            bannedUntil = request.bannedUntil,
            appealStatus = "none",
            createdAt = response["created_at"] as? String ?: ""
        )
        
        Result.success(ban)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get ban status for a user
     */
    suspend fun getBanStatus(userId: String): Result<BanStatus> = try {
        val response = supabaseApi.getBans(userId)
        
        if (response.isEmpty()) {
            return Result.success(BanStatus(isBanned = false))
        }
        
        val banData = response[0]
        val bannedUntil = banData["banned_until"] as? String
        val remainingTime = if (bannedUntil != null) {
            // Calculate remaining ban time
            val bannedUntilMs = bannedUntil.toLongOrNull() ?: 0
            val currentTimeMs = System.currentTimeMillis()
            maxOf(0, bannedUntilMs - currentTimeMs)
        } else {
            Long.MAX_VALUE // Permanent ban
        }
        
        val ban = Ban(
            id = banData["id"] as? String ?: "",
            userId = userId,
            reason = banData["reason"] as? String ?: "",
            bannedUntil = bannedUntil,
            appealStatus = banData["appeal_status"] as? String ?: "none",
            createdAt = banData["created_at"] as? String ?: ""
        )
        
        Result.success(
            BanStatus(
                isBanned = true,
                ban = ban,
                remainingBanTime = remainingTime,
                canAppeal = banData["appeal_status"] as? String == "none"
            )
        )
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Submit a ban appeal
     */
    suspend fun submitBanAppeal(banId: String, appealMessage: String, userId: String): Result<Boolean> = try {
        supabaseApi.updateBan(
            banId,
            mapOf(
                "appeal_status" to "pending",
                "appeal_message" to appealMessage
            )
        )
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Detect links and phone numbers in message
     */
    fun detectLinksAndPhoneNumbers(message: String): LinkPhoneDetectionResult {
        val linkPattern = Regex("(https?://|www\\.|ftp://)")
        val phonePattern = Regex("(\\+?\\d{1,3}[-.]?)?\\(?\\d{3}\\)?[-.]?\\d{3}[-.]?\\d{4}")
        
        val hasLinks = linkPattern.containsMatchIn(message)
        val hasPhoneNumbers = phonePattern.containsMatchIn(message)
        
        val detectedItems = mutableListOf<String>()
        if (hasLinks) detectedItems.add("links")
        if (hasPhoneNumbers) detectedItems.add("phone_numbers")
        
        val rejectionReason = when {
            hasLinks && hasPhoneNumbers -> "link_and_phone_detected"
            hasLinks -> "link_detected"
            hasPhoneNumbers -> "phone_number_detected"
            else -> null
        }
        
        return LinkPhoneDetectionResult(
            hasLinks = hasLinks,
            hasPhoneNumbers = hasPhoneNumbers,
            detectedItems = detectedItems,
            rejectionReason = rejectionReason
        )
    }

    /**
     * Check for repeated link/phone violations and apply autoban
     */
    suspend fun checkAndApplyAutoban(userId: String, violationType: String): Result<Boolean> = try {
        val currentCount = linkPhoneWarningCount[userId] ?: 0
        
        when {
            currentCount == 0 -> {
                // First violation: warning
                linkPhoneWarningCount[userId] = 1
                Result.success(false) // No ban yet
            }
            currentCount == 1 -> {
                // Second violation: ban
                linkPhoneWarningCount[userId] = 2
                val banRequest = CreateBanRequest(
                    userId = userId,
                    reason = "Repeated violation: $violationType",
                    bannedUntil = (System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000).toString() // 7 days
                )
                banUser(banRequest)
                Result.success(true) // User banned
            }
            else -> {
                Result.success(true) // Already banned
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Reset violation count for a user (admin only)
     */
    fun resetViolationCount(userId: String) {
        linkPhoneWarningCount.remove(userId)
    }

    /**
     * Get violation count for a user
     */
    fun getViolationCount(userId: String): Int {
        return linkPhoneWarningCount[userId] ?: 0
    }
}
