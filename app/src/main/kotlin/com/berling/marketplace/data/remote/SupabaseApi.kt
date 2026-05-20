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
}
