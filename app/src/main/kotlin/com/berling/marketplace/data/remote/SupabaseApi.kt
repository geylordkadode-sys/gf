package com.berling.marketplace.data.remote

import com.berling.marketplace.data.remote.models.*
import com.berling.marketplace.data.local.entities.OrderEntity
import com.berling.marketplace.data.local.entities.AnalyticsEventEntity
import retrofit2.http.*

interface SupabaseApi {
    // Auth endpoints
    @POST("auth/v1/signup")
    suspend fun signUp(@Body request: AuthSignUpRequest): AuthSignUpResponse

    @POST("auth/v1/verify")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): AuthSignUpResponse

    @POST("auth/v1/recover")
    suspend fun resetPassword(@Body request: PasswordResetRequest): ApiResponse<Unit>

    @POST("auth/v1/verify")
    suspend fun verifyPasswordReset(@Body request: PasswordResetConfirmRequest): AuthSession

    @POST("rest/v1/rpc/login_with_email")
    suspend fun loginWithEmail(@Body request: Map<String, String>): AuthSession

    // User endpoints
    @GET("rest/v1/users")
    suspend fun getUser(@Header("Authorization") token: String): ApiResponse<UserProfileRequest>

    @PATCH("rest/v1/users")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Body request: UserProfileRequest
    ): ApiResponse<UserProfileRequest>

    // Product endpoints
    @POST("rest/v1/products")
    suspend fun createProduct(
        @Header("Authorization") token: String,
        @Body request: ProductCreateRequest
    ): ApiResponse<ProductCreateRequest>

    @GET("rest/v1/products")
    suspend fun getProducts(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): ApiResponse<List<ProductCreateRequest>>

    @GET("rest/v1/products/{id}")
    suspend fun getProduct(@Path("id") id: String): ApiResponse<ProductCreateRequest>

    @PATCH("rest/v1/products/{id}")
    suspend fun updateProduct(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: ProductUpdateRequest
    ): ApiResponse<ProductUpdateRequest>

    @DELETE("rest/v1/products/{id}")
    suspend fun deleteProduct(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ApiResponse<Unit>

    // Email & OTP
    @POST("functions/v1/send-otp")
    suspend fun sendOtpEmail(@Body request: SendOtpEmailRequest): SendEmailResponse

    // Messages
    @POST("rest/v1/messages")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Body request: MessageRequest
    ): ApiResponse<MessageResponse>

    @GET("rest/v1/messages")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Query("conversationId") conversationId: String
    ): ApiResponse<List<MessageResponse>>

    // Orders
    @POST("rest/v1/orders")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body request: CreateOrderRequest
    ): ApiResponse<OrderEntity>

    @GET("rest/v1/orders/{orderId}")
    suspend fun getOrder(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: String
    ): ApiResponse<OrderEntity>

    @PATCH("rest/v1/orders/{orderId}")
    suspend fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: String,
        @Body request: Map<String, String>
    ): ApiResponse<Unit>

    // Payments
    @POST("functions/v1/initiate-payment")
    suspend fun initiatePayment(
        @Header("Authorization") token: String,
        @Body request: PaymentInitiateRequest
    ): ApiResponse<Map<String, String>>

    @POST("functions/v1/verify-payment")
    suspend fun verifyPayment(
        @Header("Authorization") token: String,
        @Body request: PaymentVerificationRequest
    ): ApiResponse<Unit>

    // Analytics
    @POST("rest/v1/analytics_events")
    suspend fun logEvent(
        @Header("Authorization") token: String,
        @Body request: AnalyticsEventRequest
    ): ApiResponse<Unit>

    @GET("rest/v1/analytics_events")
    suspend fun getAnalytics(
        @Header("Authorization") token: String,
        @Query("eventName") eventName: String? = null
    ): ApiResponse<List<AnalyticsEventEntity>>

    // Reviews
    @GET("rest/v1/reviews")
    suspend fun getReviews(
        @Query("product_id") productId: String
    ): List<Map<String, Any>>

    @POST("rest/v1/reviews")
    suspend fun createReview(
        @Header("Authorization") token: String,
        @Body request: Map<String, Any>
    ): Map<String, Any>

    // Review Replies
    @GET("rest/v1/review_replies")
    suspend fun getReviewReplies(
        @Query("review_id") reviewId: String
    ): List<Map<String, Any>>

    @POST("rest/v1/review_replies")
    suspend fun createReviewReply(
        @Header("Authorization") token: String,
        @Body request: Map<String, Any>
    ): Map<String, Any>

    // Follows
    @GET("rest/v1/follows")
    suspend fun getFollows(
        @Query("follower_id") followerId: String
    ): List<Map<String, Any>>

    @POST("rest/v1/follows")
    suspend fun addFollow(
        @Header("Authorization") token: String,
        @Body request: Map<String, Any>
    ): Map<String, Any>

    @DELETE("rest/v1/follows/{id}")
    suspend fun removeFollow(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Unit

    // Notifications
    @GET("rest/v1/notifications")
    suspend fun getNotifications(
        @Query("user_id") userId: String
    ): List<Map<String, Any>>

    @POST("rest/v1/notifications")
    suspend fun createNotification(
        @Header("Authorization") token: String,
        @Body request: Map<String, Any>
    ): Map<String, Any>

    @PATCH("rest/v1/notifications/{id}")
    suspend fun updateNotification(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: Map<String, Any>
    ): Map<String, Any>

    // Reports
    @POST("rest/v1/reports")
    suspend fun createReport(
        @Header("Authorization") token: String,
        @Body request: Map<String, Any>
    ): Map<String, Any>

    @GET("rest/v1/reports")
    suspend fun getReports(
        @Query("reporter_id") reporterId: String
    ): List<Map<String, Any>>

    @PATCH("rest/v1/reports/{id}")
    suspend fun updateReport(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: Map<String, Any>
    ): Map<String, Any>

    // Bans
    @GET("rest/v1/bans")
    suspend fun getBans(
        @Query("user_id") userId: String
    ): List<Map<String, Any>>

    @POST("rest/v1/bans")
    suspend fun createBan(
        @Header("Authorization") token: String,
        @Body request: Map<String, Any>
    ): Map<String, Any>

    @PATCH("rest/v1/bans/{id}")
    suspend fun updateBan(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: Map<String, Any>
    ): Map<String, Any>

    // KYC Applications
    @GET("rest/v1/kyc_applications")
    suspend fun getKYCApplication(
        @Query("user_id") userId: String
    ): List<Map<String, Any>>

    @POST("rest/v1/kyc_applications")
    suspend fun createKYCApplication(
        @Header("Authorization") token: String,
        @Body request: Map<String, Any>
    ): Map<String, Any>

    @PATCH("rest/v1/kyc_applications/{id}")
    suspend fun updateKYCApplication(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: Map<String, Any>
    ): Map<String, Any>

    @GET("rest/v1/kyc_applications?status=eq.pending")
    suspend fun getPendingKYCApplications(
        @Header("Authorization") token: String
    ): List<Map<String, Any>>

    // Device Accounts
    @GET("rest/v1/device_accounts")
    suspend fun getDeviceAccounts(
        @Query("device_id") deviceId: String
    ): List<Map<String, Any>>

    @POST("rest/v1/device_accounts")
    suspend fun createDeviceAccount(
        @Header("Authorization") token: String,
        @Body request: Map<String, Any>
    ): Map<String, Any>

    @DELETE("rest/v1/device_accounts/{id}")
    suspend fun deleteDeviceAccount(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Unit

    // Conversations
    @GET("rest/v1/conversations")
    suspend fun getConversations(
        @Query("participant1_id") userId: String
    ): List<Map<String, Any>>

    @POST("rest/v1/conversations")
    suspend fun createConversation(
        @Header("Authorization") token: String,
        @Body request: Map<String, Any>
    ): Map<String, Any>

    @PATCH("rest/v1/conversations/{id}")
    suspend fun updateConversation(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: Map<String, Any>
    ): Map<String, Any>

    // Blocked Users
    @GET("rest/v1/blocked_users")
    suspend fun getBlockedUsers(
        @Query("blocker_id") blockerId: String
    ): List<Map<String, Any>>

    @POST("rest/v1/blocked_users")
    suspend fun blockUser(
        @Header("Authorization") token: String,
        @Body request: Map<String, Any>
    ): Map<String, Any>

    @DELETE("rest/v1/blocked_users/{id}")
    suspend fun unblockUser(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Unit

    // Rate Limits
    @GET("rest/v1/rate_limits")
    suspend fun getRateLimit(
        @Query("user_id") userId: String,
        @Query("feature") feature: String
    ): List<Map<String, Any>>

    @PATCH("rest/v1/rate_limits/{id}")
    suspend fun updateRateLimit(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: Map<String, Any>
    ): Map<String, Any>
}
