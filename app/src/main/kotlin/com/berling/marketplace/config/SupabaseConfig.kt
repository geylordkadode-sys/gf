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
}
