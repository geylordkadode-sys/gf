package com.berling.marketplace.config

/**
 * Supabase Configuration
 * Contains all necessary configuration for Supabase integration
 */
object SupabaseConfig {
    
    // Supabase credentials
    const val SUPABASE_URL = "https://fkeuioagahwqgpqjuwqj.supabase.co"
    const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZrZXVpb2FnYWh3cWdwcWp1d3FqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzkxOTE2NzgsImV4cCI6MjA5NDc2NzY3OH0.rRPejxJ4lVae57Y5IYoBA1dSYCWB24jSVxymZe7bqow"
    
    // API Endpoints
    const val PRODUCTS_TABLE = "products"
    const val USERS_TABLE = "users"
    const val ORDERS_TABLE = "orders"
    const val MESSAGES_TABLE = "messages"
    const val FAVORITES_TABLE = "favorites"
    const val REVIEWS_TABLE = "reviews"
    const val REVIEW_REPLIES_TABLE = "review_replies"
    const val FOLLOWS_TABLE = "follows"
    const val NOTIFICATIONS_TABLE = "notifications"
    const val REPORTS_TABLE = "reports"
    const val BANS_TABLE = "bans"
    const val KYC_APPLICATIONS_TABLE = "kyc_applications"
    const val DEVICE_ACCOUNTS_TABLE = "device_accounts"
    const val CONVERSATIONS_TABLE = "conversations"
    const val BLOCKED_USERS_TABLE = "blocked_users"
    const val RATE_LIMITS_TABLE = "rate_limits"
    
    // Storage buckets
    const val PRODUCTS_BUCKET = "products"
    const val USERS_BUCKET = "users"
    const val TEMP_BUCKET = "temp"
    
    // API Functions
    const val SEND_OTP_FUNCTION = "send-otp"
    const val VERIFY_OTP_FUNCTION = "verify-otp"
    const val SYNC_DATA_FUNCTION = "sync-data"
    
    // Timeouts (in milliseconds)
    const val CONNECT_TIMEOUT = 30000L
    const val READ_TIMEOUT = 30000L
    const val WRITE_TIMEOUT = 30000L
    
    // Retry configuration
    const val MAX_RETRIES = 3
    const val RETRY_DELAY_MS = 1000L
    const val RETRY_BACKOFF_MULTIPLIER = 2.0
    
    // Image upload configuration
    const val MAX_IMAGE_SIZE_BYTES = 5242880L // 5MB
    const val ALLOWED_IMAGE_FORMATS = "jpeg,jpg,png,webp"
    const val IMAGE_COMPRESSION_QUALITY = 85
    
    // Product upload configuration
    const val MAX_IMAGES_PER_PRODUCT = 10
    const val MIN_IMAGES_PER_PRODUCT = 1
    const val MAX_TITLE_LENGTH = 100
    const val MAX_DESCRIPTION_LENGTH = 2000
    
    // Pagination
    const val DEFAULT_PAGE_SIZE = 50
    const val MAX_PAGE_SIZE = 100
    
    // Feature flags
    const val ENABLE_OFFLINE_MODE = true
    const val ENABLE_BACKGROUND_SYNC = true
    const val ENABLE_COMPRESSION = true
    
    /**
     * Get full API endpoint URL
     */
    fun getApiEndpoint(path: String): String {
        return "$SUPABASE_URL/rest/v1$path"
    }

    /**
     * Get storage URL for an object
     */
    fun getStorageUrl(bucket: String, path: String): String {
        return "$SUPABASE_URL/storage/v1/object/public/$bucket/$path"
    }

    /**
     * Get function URL
     */
    fun getFunctionUrl(functionName: String): String {
        return "$SUPABASE_URL/functions/v1/$functionName"
    }

    /**
     * Get headers for authenticated requests
     */
    fun getAuthHeaders(token: String? = null): Map<String, String> {
        return mutableMapOf(
            "apikey" to SUPABASE_KEY,
            "Content-Type" to "application/json"
        ).apply {
            if (!token.isNullOrEmpty()) {
                put("Authorization", "Bearer $token")
            }
        }
    }

    /**
     * Validate configuration
     */
    fun isValid(): Boolean {
        return SUPABASE_URL.isNotEmpty() &&
                SUPABASE_KEY.isNotEmpty() &&
                SUPABASE_URL.startsWith("https://")
    }
}

/**
 * API Endpoints for common operations
 */
object SupabaseEndpoints {
    
    // Authentication
    fun signUp() = "/auth/v1/signup"
    fun signIn() = "/auth/v1/token?grant_type=password"
    fun refreshToken() = "/auth/v1/token?grant_type=refresh_token"
    fun signOut() = "/auth/v1/logout"
    
    // Products
    fun getProducts() = "/rest/v1/${SupabaseConfig.PRODUCTS_TABLE}"
    fun getProduct(id: String) = "/rest/v1/${SupabaseConfig.PRODUCTS_TABLE}?id=eq.$id"
    fun createProduct() = "/rest/v1/${SupabaseConfig.PRODUCTS_TABLE}"
    fun updateProduct(id: String) = "/rest/v1/${SupabaseConfig.PRODUCTS_TABLE}?id=eq.$id"
    fun deleteProduct(id: String) = "/rest/v1/${SupabaseConfig.PRODUCTS_TABLE}?id=eq.$id"
    fun getSellerProducts(sellerId: String) = "/rest/v1/${SupabaseConfig.PRODUCTS_TABLE}?seller_id=eq.$sellerId"
    
    // Users
    fun getUser(id: String) = "/rest/v1/${SupabaseConfig.USERS_TABLE}?id=eq.$id"
    fun updateUser(id: String) = "/rest/v1/${SupabaseConfig.USERS_TABLE}?id=eq.$id"
    fun getProfile(email: String) = "/rest/v1/${SupabaseConfig.USERS_TABLE}?email=eq.$email"
    
    // Orders
    fun getOrders(userId: String) = "/rest/v1/${SupabaseConfig.ORDERS_TABLE}?user_id=eq.$userId"
    fun createOrder() = "/rest/v1/${SupabaseConfig.ORDERS_TABLE}"
    
    // Messages
    fun getMessages(conversationId: String) = "/rest/v1/${SupabaseConfig.MESSAGES_TABLE}?conversation_id=eq.$conversationId"
    fun sendMessage() = "/rest/v1/${SupabaseConfig.MESSAGES_TABLE}"
    
    // Favorites
    fun getFavorites(userId: String) = "/rest/v1/${SupabaseConfig.FAVORITES_TABLE}?user_id=eq.$userId"
    fun addFavorite() = "/rest/v1/${SupabaseConfig.FAVORITES_TABLE}"
    fun removeFavorite(favoriteId: String) = "/rest/v1/${SupabaseConfig.FAVORITES_TABLE}?id=eq.$favoriteId"
    
    // Reviews
    fun getReviews(productId: String) = "/rest/v1/${SupabaseConfig.REVIEWS_TABLE}?product_id=eq.$productId"
    fun createReview() = "/rest/v1/${SupabaseConfig.REVIEWS_TABLE}"
    fun getReviewReplies(reviewId: String) = "/rest/v1/${SupabaseConfig.REVIEW_REPLIES_TABLE}?review_id=eq.$reviewId"
    fun createReviewReply() = "/rest/v1/${SupabaseConfig.REVIEW_REPLIES_TABLE}"

    // Follows
    fun getFollows(followerId: String) = "/rest/v1/${SupabaseConfig.FOLLOWS_TABLE}?follower_id=eq.$followerId"
    fun addFollow() = "/rest/v1/${SupabaseConfig.FOLLOWS_TABLE}"
    fun removeFollow(followId: String) = "/rest/v1/${SupabaseConfig.FOLLOWS_TABLE}?id=eq.$followId"

    // Notifications
    fun getNotifications(userId: String) = "/rest/v1/${SupabaseConfig.NOTIFICATIONS_TABLE}?user_id=eq.$userId"
    fun createNotification() = "/rest/v1/${SupabaseConfig.NOTIFICATIONS_TABLE}"
    fun updateNotification(id: String) = "/rest/v1/${SupabaseConfig.NOTIFICATIONS_TABLE}?id=eq.$id"

    // Reports
    fun createReport() = "/rest/v1/${SupabaseConfig.REPORTS_TABLE}"
    fun getReports(reporterId: String) = "/rest/v1/${SupabaseConfig.REPORTS_TABLE}?reporter_id=eq.$reporterId"
    fun updateReport(id: String) = "/rest/v1/${SupabaseConfig.REPORTS_TABLE}?id=eq.$id"

    // Bans
    fun getBans(userId: String) = "/rest/v1/${SupabaseConfig.BANS_TABLE}?user_id=eq.$userId"
    fun createBan() = "/rest/v1/${SupabaseConfig.BANS_TABLE}"
    fun updateBan(id: String) = "/rest/v1/${SupabaseConfig.BANS_TABLE}?id=eq.$id"

    // KYC Applications
    fun getKycApplication(userId: String) = "/rest/v1/${SupabaseConfig.KYC_APPLICATIONS_TABLE}?user_id=eq.$userId"
    fun createKycApplication() = "/rest/v1/${SupabaseConfig.KYC_APPLICATIONS_TABLE}"
    fun updateKycApplication(id: String) = "/rest/v1/${SupabaseConfig.KYC_APPLICATIONS_TABLE}?id=eq.$id"

    // Device Accounts
    fun getDeviceAccounts(deviceId: String) = "/rest/v1/${SupabaseConfig.DEVICE_ACCOUNTS_TABLE}?device_id=eq.$deviceId"
    fun createDeviceAccount() = "/rest/v1/${SupabaseConfig.DEVICE_ACCOUNTS_TABLE}"
    fun deleteDeviceAccount(id: String) = "/rest/v1/${SupabaseConfig.DEVICE_ACCOUNTS_TABLE}?id=eq.$id"

    // Conversations
    fun getConversations(userId: String) = "/rest/v1/${SupabaseConfig.CONVERSATIONS_TABLE}?participant1_id=eq.$userId"
    fun createConversation() = "/rest/v1/${SupabaseConfig.CONVERSATIONS_TABLE}"
    fun updateConversation(id: String) = "/rest/v1/${SupabaseConfig.CONVERSATIONS_TABLE}?id=eq.$id"

    // Messages (already present, but ensure it uses the correct table name)
    fun getMessages(conversationId: String) = "/rest/v1/${SupabaseConfig.MESSAGES_TABLE}?conversation_id=eq.$conversationId"
    fun sendMessage() = "/rest/v1/${SupabaseConfig.MESSAGES_TABLE}"

    // Blocked Users
    fun getBlockedUsers(blockerId: String) = "/rest/v1/${SupabaseConfig.BLOCKED_USERS_TABLE}?blocker_id=eq.$blockerId"
    fun blockUser() = "/rest/v1/${SupabaseConfig.BLOCKED_USERS_TABLE}"
    fun unblockUser(id: String) = "/rest/v1/${SupabaseConfig.BLOCKED_USERS_TABLE}?id=eq.$id"

    // Rate Limits
    fun getRateLimit(userId: String, feature: String) = "/rest/v1/${SupabaseConfig.RATE_LIMITS_TABLE}?user_id=eq.$userId&feature=eq.$feature"
    fun updateRateLimit(id: String) = "/rest/v1/${SupabaseConfig.RATE_LIMITS_TABLE}?id=eq.$id"
}
