package com.berling.marketplace.data.repository

import com.berling.marketplace.data.local.MarketplaceDatabase
import com.berling.marketplace.data.models.*
import com.berling.marketplace.data.remote.SupabaseApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

/**
 * Repository for managing social features: follows, profiles, and notifications
 */
class SocialRepository(
    private val supabaseApi: SupabaseApi,
    private val database: MarketplaceDatabase
) {

    private val _followsState = MutableStateFlow<List<Follow>>(emptyList())
    val followsState = _followsState.asStateFlow()

    private val _notificationsState = MutableStateFlow<List<Notification>>(emptyList())
    val notificationsState = _notificationsState.asStateFlow()

    /**
     * Follow a user
     */
    suspend fun followUser(followerId: String, followedId: String): Result<Follow> = try {
        val response = supabaseApi.addFollow(
            mapOf(
                "follower_id" to followerId,
                "followed_id" to followedId
            )
        )
        
        val follow = Follow(
            id = response["id"] as? String ?: "",
            followerId = followerId,
            followedId = followedId,
            createdAt = response["created_at"] as? String ?: ""
        )
        
        Result.success(follow)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Unfollow a user
     */
    suspend fun unfollowUser(followId: String): Result<Boolean> = try {
        supabaseApi.removeFollow(followId)
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get user's follows
     */
    fun getUserFollows(followerId: String): Flow<List<Follow>> = flow {
        try {
            val response = supabaseApi.getFollows(followerId)
            
            val follows = response.map { followData ->
                Follow(
                    id = followData["id"] as? String ?: "",
                    followerId = followData["follower_id"] as? String ?: "",
                    followedId = followData["followed_id"] as? String ?: "",
                    createdAt = followData["created_at"] as? String ?: ""
                )
            }
            
            _followsState.value = follows
            emit(follows)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    /**
     * Check if current user follows another user
     */
    suspend fun isFollowing(followerId: String, followedId: String): Boolean = try {
        val response = supabaseApi.getFollows(followerId)
        response.any { (it["followed_id"] as? String) == followedId }
    } catch (e: Exception) {
        false
    }

    /**
     * Get user profile
     */
    suspend fun getUserProfile(userId: String): Result<UserProfile> = try {
        // This would fetch from users table with additional stats
        Result.success(
            UserProfile(
                id = userId,
                name = "",
                email = "",
                photoUrl = "",
                bio = "",
                location = "",
                rating = 0.0,
                reviewCount = 0,
                followers = 0,
                following = 0,
                productCount = 0,
                isVerified = false,
                isBanned = false
            )
        )
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get user notifications
     */
    fun getUserNotifications(userId: String): Flow<List<Notification>> = flow {
        try {
            val response = supabaseApi.getNotifications(userId)
            
            val notifications = response.map { notifData ->
                Notification(
                    id = notifData["id"] as? String ?: "",
                    userId = userId,
                    type = notifData["type"] as? String ?: "",
                    sourceId = notifData["source_id"] as? String,
                    message = notifData["message"] as? String ?: "",
                    isRead = notifData["is_read"] as? Boolean ?: false,
                    createdAt = notifData["created_at"] as? String ?: ""
                )
            }
            
            _notificationsState.value = notifications
            emit(notifications)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    /**
     * Create a notification
     */
    suspend fun createNotification(payload: NotificationPayload): Result<Notification> = try {
        val response = supabaseApi.createNotification(
            mapOf(
                "user_id" to payload.userId,
                "type" to payload.type,
                "source_id" to (payload.sourceId ?: ""),
                "message" to payload.message,
                "is_read" to false
            )
        )
        
        val notification = Notification(
            id = response["id"] as? String ?: "",
            userId = payload.userId,
            type = payload.type,
            sourceId = payload.sourceId,
            message = payload.message,
            isRead = false,
            createdAt = response["created_at"] as? String ?: ""
        )
        
        Result.success(notification)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Mark notification as read
     */
    suspend fun markNotificationAsRead(notificationId: String): Result<Boolean> = try {
        supabaseApi.updateNotification(
            notificationId,
            mapOf("is_read" to true)
        )
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Mark all notifications as read
     */
    suspend fun markAllNotificationsAsRead(userId: String): Result<Boolean> = try {
        // This would update all notifications for the user
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Block a user
     */
    suspend fun blockUser(blockerId: String, blockedId: String): Result<BlockedUser> = try {
        val response = supabaseApi.blockUser(
            mapOf(
                "blocker_id" to blockerId,
                "blocked_id" to blockedId
            )
        )
        
        val blockedUser = BlockedUser(
            id = response["id"] as? String ?: "",
            blockerId = blockerId,
            blockedId = blockedId,
            createdAt = response["created_at"] as? String ?: ""
        )
        
        Result.success(blockedUser)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Unblock a user
     */
    suspend fun unblockUser(blockedUserId: String): Result<Boolean> = try {
        supabaseApi.unblockUser(blockedUserId)
        Result.success(true)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Get blocked users list
     */
    fun getBlockedUsers(blockerId: String): Flow<List<BlockedUser>> = flow {
        try {
            val response = supabaseApi.getBlockedUsers(blockerId)
            
            val blockedUsers = response.map { userData ->
                BlockedUser(
                    id = userData["id"] as? String ?: "",
                    blockerId = blockerId,
                    blockedId = userData["blocked_id"] as? String ?: "",
                    createdAt = userData["created_at"] as? String ?: ""
                )
            }
            
            emit(blockedUsers)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}
