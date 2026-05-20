package com.berling.marketplace.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: String,
    val productId: String,
    val buyerId: String,
    val buyerName: String = "",
    val buyerPhotoUrl: String = "",
    val sellerId: String,
    val rating: Int,
    val comment: String = "",
    val verifiedPurchase: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = "",
    val replies: List<ReviewReply> = emptyList()
)

@Serializable
data class ReviewReply(
    val id: String,
    val reviewId: String,
    val userId: String,
    val userName: String = "",
    val userPhotoUrl: String = "",
    val replyText: String,
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class CreateReviewRequest(
    val productId: String,
    val buyerId: String,
    val sellerId: String,
    val rating: Int,
    val comment: String = "",
    val verifiedPurchase: Boolean = false
)

@Serializable
data class CreateReviewReplyRequest(
    val reviewId: String,
    val userId: String,
    val replyText: String
)

@Serializable
data class ProductRating(
    val averageRating: Double = 0.0,
    val totalReviews: Int = 0,
    val ratingDistribution: Map<Int, Int> = emptyMap() // rating -> count
)

@Serializable
data class SellerRating(
    val sellerId: String,
    val averageRating: Double = 0.0,
    val totalReviews: Int = 0,
    val responseRate: Double = 0.0,
    val positiveReviews: Int = 0
)
