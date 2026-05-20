package com.berling.marketplace.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.berling.marketplace.data.local.entities.UserEntity
import com.berling.marketplace.data.local.entities.ProductEntity
import com.berling.marketplace.data.local.entities.FavoriteEntity
import com.berling.marketplace.data.local.entities.PendingSyncEntity
import com.berling.marketplace.data.local.entities.MessageEntity
import com.berling.marketplace.data.local.entities.ConversationEntity
import com.berling.marketplace.data.local.entities.AnalyticsEventEntity
import com.berling.marketplace.data.local.entities.OrderEntity

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        FavoriteEntity::class,
        PendingSyncEntity::class,
        MessageEntity::class,
        ConversationEntity::class,
        AnalyticsEventEntity::class,
        OrderEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MarketplaceDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun pendingSyncDao(): PendingSyncDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun analyticsDao(): AnalyticsDao
    abstract fun orderDao(): OrderDao
}
