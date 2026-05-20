package com.berling.marketplace.utils

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * RateLimitingManager handles post rate limiting for users
 * New users can post 2 times a day for the first 7 days
 * After 7 days, users can post unlimited times (subject to other limits)
 */
object RateLimitingManager {

    private const val NEW_USER_DAILY_LIMIT = 2
    private const val NEW_USER_GRACE_PERIOD_DAYS = 7
    private const val MAX_POSTS_PER_HOUR = 5

    /**
     * Check if user can post a new product
     */
    fun canPostProduct(
        userId: String,
        userCreatedAt: Long,
        postsToday: Int,
        postsThisHour: Int
    ): RateLimitResult {
        val now = System.currentTimeMillis()
        val accountAgeMs = now - userCreatedAt
        val accountAgeDays = accountAgeMs / (24 * 60 * 60 * 1000)
        
        // Check if user is still in grace period
        if (accountAgeDays < NEW_USER_GRACE_PERIOD_DAYS) {
            if (postsToday >= NEW_USER_DAILY_LIMIT) {
                return RateLimitResult(
                    canPost = false,
                    reason = "daily_limit_reached",
                    message = "New users can post $NEW_USER_DAILY_LIMIT times per day for the first $NEW_USER_GRACE_PERIOD_DAYS days",
                    remainingPosts = 0,
                    resetTime = getNextDayResetTime()
                )
            }
        }
        
        // Check hourly limit for all users
        if (postsThisHour >= MAX_POSTS_PER_HOUR) {
            return RateLimitResult(
                canPost = false,
                reason = "hourly_limit_reached",
                message = "You can post maximum $MAX_POSTS_PER_HOUR times per hour",
                remainingPosts = 0,
                resetTime = getNextHourResetTime()
            )
        }
        
        return RateLimitResult(canPost = true)
    }

    /**
     * Get remaining posts for today for a new user
     */
    fun getRemainingDailyPosts(
        userCreatedAt: Long,
        postsToday: Int
    ): Int {
        val now = System.currentTimeMillis()
        val accountAgeMs = now - userCreatedAt
        val accountAgeDays = accountAgeMs / (24 * 60 * 60 * 1000)
        
        return if (accountAgeDays < NEW_USER_GRACE_PERIOD_DAYS) {
            maxOf(0, NEW_USER_DAILY_LIMIT - postsToday)
        } else {
            Int.MAX_VALUE // Unlimited
        }
    }

    /**
     * Get remaining posts for this hour
     */
    fun getRemainingHourlyPosts(postsThisHour: Int): Int {
        return maxOf(0, MAX_POSTS_PER_HOUR - postsThisHour)
    }

    /**
     * Get time until next day reset (in milliseconds)
     */
    private fun getNextDayResetTime(): Long {
        val now = System.currentTimeMillis()
        val nextMidnight = LocalDate.now().plusDays(1).atStartOfDay()
        val nextMidnightMs = nextMidnight.toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        return maxOf(0, nextMidnightMs - now)
    }

    /**
     * Get time until next hour reset (in milliseconds)
     */
    private fun getNextHourResetTime(): Long {
        val now = System.currentTimeMillis()
        val nextHour = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0)
        val nextHourMs = nextHour.toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        return maxOf(0, nextHourMs - now)
    }

    /**
     * Check if user is still in grace period
     */
    fun isInGracePeriod(userCreatedAt: Long): Boolean {
        val now = System.currentTimeMillis()
        val accountAgeMs = now - userCreatedAt
        val accountAgeDays = accountAgeMs / (24 * 60 * 60 * 1000)
        return accountAgeDays < NEW_USER_GRACE_PERIOD_DAYS
    }

    /**
     * Get days remaining in grace period
     */
    fun getGracePeriodDaysRemaining(userCreatedAt: Long): Int {
        val now = System.currentTimeMillis()
        val accountAgeMs = now - userCreatedAt
        val accountAgeDays = accountAgeMs / (24 * 60 * 60 * 1000)
        return maxOf(0, (NEW_USER_GRACE_PERIOD_DAYS - accountAgeDays).toInt())
    }
}

data class RateLimitResult(
    val canPost: Boolean,
    val reason: String? = null,
    val message: String? = null,
    val remainingPosts: Int = 0,
    val resetTime: Long = 0 // in milliseconds
)
