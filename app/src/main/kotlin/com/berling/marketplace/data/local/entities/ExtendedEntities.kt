package com.berling.marketplace.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "messages")
@Serializable
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val isSynced: Boolean = false
)

@Entity(tableName = "conversations")
@Serializable
data class ConversationEntity(
    @PrimaryKey
    val id: String,
    val participantId: String,
    val participantName: String,
    val participantPhotoUrl: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0,
    val productId: String = "",
    val productTitle: String = "",
    val productImageUrl: String = "",
    val isSynced: Boolean = false,
    val createdAt: String = ""
)

@Entity(tableName = "analytics_events")
@Serializable
data class AnalyticsEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val eventName: String,
    val eventData: String = "", // JSON
    val timestamp: String = "",
    val userId: String = "",
    val isSynced: Boolean = false
)

@Entity(tableName = "orders")
@Serializable
data class OrderEntity(
    @PrimaryKey
    val id: String,
    val productId: String,
    val productTitle: String,
    val buyerId: String,
    val sellerId: String,
    val price: Double,
    val quantity: Int,
    val status: String, // pending, paid, shipped, delivered, cancelled
    val paymentMethod: String = "razorpay",
    val trackingNumber: String = "",
    val createdAt: String,
    val deliveryDate: String = "",
    val isSynced: Boolean = false
)
