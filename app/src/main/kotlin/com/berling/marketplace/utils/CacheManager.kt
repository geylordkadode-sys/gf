package com.berling.marketplace.utils

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * CacheManager handles caching of products, user data, and other marketplace content
 * Implements lazy loading and offline access
 */
object CacheManager {

    private const val CACHE_DIR = "marketplace_cache"
    private const val PRODUCTS_CACHE_FILE = "products_cache.json"
    private const val USERS_CACHE_FILE = "users_cache.json"
    private const val REVIEWS_CACHE_FILE = "reviews_cache.json"
    private const val MESSAGES_CACHE_FILE = "messages_cache.json"
    
    // Cache expiration times
    private val PRODUCTS_CACHE_DURATION = TimeUnit.HOURS.toMillis(1)
    private val USERS_CACHE_DURATION = TimeUnit.HOURS.toMillis(2)
    private val REVIEWS_CACHE_DURATION = TimeUnit.HOURS.toMillis(1)
    private val MESSAGES_CACHE_DURATION = TimeUnit.MINUTES.toMillis(30)

    /**
     * Get cache directory
     */
    private fun getCacheDir(context: Context): File {
        val cacheDir = File(context.cacheDir, CACHE_DIR)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return cacheDir
    }

    /**
     * Save data to cache
     */
    fun saveToCache(context: Context, fileName: String, data: String): Boolean {
        return try {
            val cacheDir = getCacheDir(context)
            val file = File(cacheDir, fileName)
            file.writeText(data)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Read data from cache
     */
    fun readFromCache(context: Context, fileName: String): String? {
        return try {
            val cacheDir = getCacheDir(context)
            val file = File(cacheDir, fileName)
            if (file.exists()) {
                file.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if cache is valid (not expired)
     */
    fun isCacheValid(context: Context, fileName: String, maxAge: Long): Boolean {
        return try {
            val cacheDir = getCacheDir(context)
            val file = File(cacheDir, fileName)
            
            if (!file.exists()) {
                return false
            }
            
            val fileAge = System.currentTimeMillis() - file.lastModified()
            fileAge < maxAge
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Clear specific cache file
     */
    fun clearCache(context: Context, fileName: String): Boolean {
        return try {
            val cacheDir = getCacheDir(context)
            val file = File(cacheDir, fileName)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Clear all cache
     */
    fun clearAllCache(context: Context): Boolean {
        return try {
            val cacheDir = getCacheDir(context)
            cacheDir.deleteRecursively()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get cache size in bytes
     */
    fun getCacheSize(context: Context): Long {
        return try {
            val cacheDir = getCacheDir(context)
            cacheDir.walkTopDown().map { it.length() }.sum()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Check if cache exists and is valid
     */
    fun hasCachedProducts(context: Context): Boolean {
        return isCacheValid(context, PRODUCTS_CACHE_FILE, PRODUCTS_CACHE_DURATION)
    }

    /**
     * Check if cache exists and is valid for users
     */
    fun hasCachedUsers(context: Context): Boolean {
        return isCacheValid(context, USERS_CACHE_FILE, USERS_CACHE_DURATION)
    }

    /**
     * Check if cache exists and is valid for reviews
     */
    fun hasCachedReviews(context: Context): Boolean {
        return isCacheValid(context, REVIEWS_CACHE_FILE, REVIEWS_CACHE_DURATION)
    }

    /**
     * Check if cache exists and is valid for messages
     */
    fun hasCachedMessages(context: Context): Boolean {
        return isCacheValid(context, MESSAGES_CACHE_FILE, MESSAGES_CACHE_DURATION)
    }

    /**
     * Get cache expiration time for products
     */
    fun getProductsCacheExpiration(): Long {
        return PRODUCTS_CACHE_DURATION
    }

    /**
     * Get cache expiration time for users
     */
    fun getUsersCacheExpiration(): Long {
        return USERS_CACHE_DURATION
    }

    /**
     * Get cache expiration time for reviews
     */
    fun getReviewsCacheExpiration(): Long {
        return REVIEWS_CACHE_DURATION
    }

    /**
     * Get cache expiration time for messages
     */
    fun getMessagesCacheExpiration(): Long {
        return MESSAGES_CACHE_DURATION
    }

    /**
     * Get time until cache expires
     */
    fun getTimeUntilExpiration(context: Context, fileName: String, maxAge: Long): Long {
        return try {
            val cacheDir = getCacheDir(context)
            val file = File(cacheDir, fileName)
            
            if (!file.exists()) {
                return 0
            }
            
            val fileAge = System.currentTimeMillis() - file.lastModified()
            maxOf(0, maxAge - fileAge)
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Implement lazy loading with pagination
     */
    data class LazyLoadConfig(
        val pageSize: Int = 20,
        val preloadThreshold: Int = 5, // Load next page when within 5 items of end
        val enableCache: Boolean = true
    )

    /**
     * Check if should load next page
     */
    fun shouldLoadNextPage(
        currentIndex: Int,
        totalItems: Int,
        pageSize: Int,
        preloadThreshold: Int = 5
    ): Boolean {
        val itemsUntilEnd = totalItems - currentIndex
        return itemsUntilEnd <= preloadThreshold
    }

    /**
     * Calculate next page offset
     */
    fun getNextPageOffset(currentPage: Int, pageSize: Int): Int {
        return currentPage * pageSize
    }

    /**
     * Get page number from offset
     */
    fun getPageFromOffset(offset: Int, pageSize: Int): Int {
        return offset / pageSize
    }
}
