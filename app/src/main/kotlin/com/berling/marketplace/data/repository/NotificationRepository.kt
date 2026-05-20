package com.berling.marketplace.data.repository

import com.berling.marketplace.data.local.MarketplaceDatabase
import com.berling.marketplace.data.models.Notification
import com.berling.marketplace.data.remote.SupabaseApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val api: SupabaseApi,
    private val database: MarketplaceDatabase
) {
    
    private val _notifications = MutableSharedFlow<Notification>()
    val notifications = _notifications.asSharedFlow()

    private val _notificationUpdates = MutableSharedFlow<String>() // For real-time updates
    val notificationUpdates = _notificationUpdates.asSharedFlow()

    suspend fun sendNotification(
        userId: String,
        title: String,
        message: String,
        type: String,
        relatedId: String? = null
    ) {
        val notification = Notification(
            id = System.currentTimeMillis().toString(),
            userId = userId,
            title = title,
            message = message,
            type = type,
            relatedId = relatedId,
            isRead = false,
            createdAt = System.currentTimeMillis().toString()
        )
        
        _notifications.emit(notification)
    }

    suspend fun notifyOrderCreated(orderId: String, buyerId: String, sellerId: String, amount: Double) {
        sendNotification(
            buyerId,
            "Order Confirmed",
            "Your order #$orderId has been confirmed",
            "order",
            orderId
        )
        
        sendNotification(
            sellerId,
            "New Order",
            "You received a new order worth ₹${String.format("%.2f", amount)}",
            "order",
            orderId
        )
    }

    suspend fun notifyOrderShipped(orderId: String, buyerId: String, trackingNumber: String) {
        sendNotification(
            buyerId,
            "Order Shipped",
            "Your order #$orderId has been shipped. Tracking: $trackingNumber",
            "order",
            orderId
        )
    }

    suspend fun notifyPaymentReceived(orderId: String, sellerId: String, amount: Double) {
        sendNotification(
            sellerId,
            "Payment Received",
            "Payment of ₹${String.format("%.2f", amount)} received for order #$orderId",
            "payment",
            orderId
        )
    }

    suspend fun notifyNewMessage(conversationId: String, userId: String, senderName: String) {
        sendNotification(
            userId,
            "New Message",
            "$senderName sent you a message",
            "message",
            conversationId
        )
    }

    suspend fun notifyAchievementUnlocked(userId: String, achievement: String) {
        sendNotification(
            userId,
            "Achievement Unlocked!",
            "You've earned the '$achievement' badge",
            "achievement",
            null
        )
    }
}
