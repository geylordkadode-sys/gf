package com.berling.marketplace.data.repository

import com.berling.marketplace.data.local.MarketplaceDatabase
import com.berling.marketplace.data.models.*
import com.berling.marketplace.data.remote.SupabaseApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository for managing product reviews and review replies
 */
class ReviewRepository(
    private val supabaseApi: SupabaseApi,
    private val database: MarketplaceDatabase
) {

    /**
     * Get all reviews for a product
     */
    fun getProductReviews(productId: String): Flow<List<Review>> = flow {
        try {
            val response = supabaseApi.getReviews(productId)
            
            val reviews = response.map { reviewData ->
                val reviewId = reviewData["id"] as? String ?: ""
                
                // Get replies for this review
                val repliesResponse = supabaseApi.getReviewReplies(reviewId)
                val replies = repliesResponse.map { replyData ->
                    ReviewReply(
                        id = replyData["id"] as? String ?: "",
                        reviewId = reviewId,
                        userId = replyData["user_id"] as? String ?: "",
                        userName = replyData["user_name"] as? String ?: "",
                        userPhotoUrl = replyData["user_photo_url"] as? String ?: "",
                        replyText = replyData["reply_text"] as? String ?: "",
                        createdAt = replyData["created_at"] as? String ?: "",
                        updatedAt = replyData["updated_at"] as? String ?: ""
                    )
                }
                
                Review(
                    id = reviewId,
                    productId = productId,
                    buyerId = reviewData["buyer_id"] as? String ?: "",
                    buyerName = reviewData["buyer_name"] as? String ?: "",
                    buyerPhotoUrl = reviewData["buyer_photo_url"] as? String ?: "",
                    sellerId = reviewData["seller_id"] as? String ?: "",
                    rating = (reviewData["rating"] as? Number)?.toInt() ?: 0,
                    comment = reviewData["comment"] as? String ?: "",
                    verifiedPurchase = reviewData["verified_purchase"] as? Boolean ?: false,
                    createdAt = reviewData["created_at"] as? String ?: "",
                    updatedAt = reviewData["updated_at"] as? String ?: "",
                    replies = replies
                )
            }
            
            emit(reviews)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    /**
     * Create a new review
     */
    suspend fun createReview(request: CreateReviewRequest): Result<Review> = try {
        val response = supabaseApi.createReview(
            mapOf(
                "product_id" to request.productId,
                "buyer_id" to request.buyerId,
                "seller_id" to request.sellerId,
                "rating" to request.rating,
                "comment" to request.comment,
                "verified_purchase" to request.verifiedPurchase
            )
        )
        
        val review = Review(
            id = response["id"] as? String ?: "",
            productId = request.productId,
            buyerId = request.buyerId,
            sellerId = request.sellerId,
            rating = request.rating,
            comment = request.comment,
            verifiedPurchase = request.verifiedPurchase,
            createdAt = response["created_at"] as? String ?: ""
        )
        
        Result.success(review)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Add a reply to a review
     */
    suspend fun addReviewReply(request: CreateReviewReplyRequest): Result<ReviewReply> = try {
        val response = supabaseApi.createReviewReply(
            mapOf(
                "review_id" to request.reviewId,
                "user_id" to request.userId,
                "reply_text" to request.replyText
            )
        )
        
        val reply = ReviewReply(
            id = response["id"] as? String ?: "",
            reviewId = request.reviewId,
            userId = request.userId,
            replyText = request.replyText,
            createdAt = response["created_at"] as? String ?: ""
        )
        
        Result.success(reply)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get product rating statistics
     */
    suspend fun getProductRating(productId: String): Result<ProductRating> = try {
        val reviews = supabaseApi.getReviews(productId)
        
        if (reviews.isEmpty()) {
            return Result.success(ProductRating())
        }
        
        val ratings = reviews.mapNotNull { (it["rating"] as? Number)?.toInt() }
        val averageRating = ratings.average()
        val ratingDistribution = ratings.groupingBy { it }.eachCount()
        
        Result.success(
            ProductRating(
                averageRating = averageRating,
                totalReviews = reviews.size,
                ratingDistribution = ratingDistribution
            )
        )
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get seller rating statistics
     */
    suspend fun getSellerRating(sellerId: String): Result<SellerRating> = try {
        // This would fetch all reviews for this seller's products
        // For now, returning a basic structure
        Result.success(
            SellerRating(
                sellerId = sellerId,
                averageRating = 0.0,
                totalReviews = 0,
                responseRate = 0.0,
                positiveReviews = 0
            )
        )
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Delete a review (buyer only)
     */
    suspend fun deleteReview(reviewId: String): Result<Boolean> = try {
        // Implementation would call delete endpoint
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Delete a review reply
     */
    suspend fun deleteReviewReply(replyId: String): Result<Boolean> = try {
        // Implementation would call delete endpoint
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Report a review
     */
    suspend fun reportReview(reviewId: String, reason: String): Result<Boolean> = try {
        // This would create a report entry
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
