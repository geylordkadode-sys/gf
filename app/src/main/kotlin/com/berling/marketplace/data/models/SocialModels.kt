package com.berling.marketplace.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Follow(
    val id: String,
    val followerId: String,
    val followedId: String,
    val createdAt: String = ""
)

@Serializable
data class Follower(
    val id: String,
    val name: String,
    val photoUrl: String,
    val isFollowing: Boolean = false
)

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String = "",
    val bio: String = "",
    val location: String = "",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val followers: Int = 0,
    val following: Int = 0,
    val productCount: Int = 0,
    val isVerified: Boolean = false,
    val isBanned: Boolean = false,
    val isFollowedByCurrentUser: Boolean = false,
    val joinedDate: String = ""
)

@Serializable
data class Notification(
    val id: String,
    val userId: String,
    val type: String, // review, reply, message, report_status, follow
    val sourceId: String? = null,
    val message: String,
    val isRead: Boolean = false,
    val createdAt: String = ""
)

@Serializable
data class NotificationPayload(
    val type: String,
    val sourceId: String? = null,
    val message: String,
    val userId: String
)

@Serializable
data class BlockedUser(
    val id: String,
    val blockerId: String,
    val blockedId: String,
    val createdAt: String = ""
)
