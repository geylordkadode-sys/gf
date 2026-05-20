package com.berling.marketplace.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "users")
@Serializable
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val password: String? = null,
    val name: String = "",
    val country: String = "",
    val location: String = "",
    val profilePhotoUrl: String = "",
    val website: String = "",
    val instagramHandle: String = "",
    val facebookHandle: String = "",
    val twitterHandle: String = "",
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val totalListings: Int = 0,
    val followers: Int = 0,
    val following: Int = 0,
    val joinedDate: String = "",
    val isVerified: Boolean = false,
    val isSynced: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Entity(tableName = "products")
@Serializable
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val sellerId: String,
    val sellerName: String,
    val sellerPhotoUrl: String,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val imageUrls: String = "", // JSON array stored as string
    val category: String,
    val brand: String = "",
    val condition: String = "", // "new", "like_new", "good", "fair", "poor"
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val country: String = "", // New field for country
    val city: String = "", // New field for city
    val deliveryOptions: String = "", // JSON array stored as string
    val returnPolicy: String = "",
    val tags: String = "", // JSON array stored as string
    val productAttributes: String = "", // JSON object stored as string
    val isNew: Boolean = false,
    val boostListing: Boolean = false,
    val discountPrice: Double = 0.0,
    val uploadProgress: Int = 0,
    val uploadStatus: String = "pending", // "pending", "uploading", "completed", "failed"
    val uploadedImageUrls: String = "",
    val isActive: Boolean = true,
    val views: Int = 0,
    val likes: Int = 0,
    val isSynced: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Entity(tableName = "favorites")
@Serializable
data class FavoriteEntity(
    @PrimaryKey
    val id: String,
    val productId: String,
    val userId: String,
    val isSynced: Boolean = false,
    val createdAt: String = ""
)

@Entity(tableName = "pending_syncs")
@Serializable
data class PendingSyncEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val entityType: String, // "user", "product", "favorite"
    val entityId: String,
    val operation: String, // "create", "update", "delete"
    val data: String = "", // JSON data
    val createdAt: String = "",
    val retryCount: Int = 0
)
