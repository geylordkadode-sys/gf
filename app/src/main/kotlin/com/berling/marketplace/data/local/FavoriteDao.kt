package com.berling.marketplace.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.berling.marketplace.data.local.entities.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("SELECT * FROM favorites WHERE productId = :productId AND userId = :userId")
    suspend fun getFavorite(productId: String, userId: String): FavoriteEntity?

    @Query("SELECT * FROM favorites WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserFavorites(userId: String): Flow<List<FavoriteEntity>>

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE productId = :productId AND userId = :userId")
    suspend fun deleteFavoriteByProductAndUser(productId: String, userId: String)

    @Query("SELECT COUNT(*) FROM favorites WHERE productId = :productId AND userId = :userId")
    suspend fun isFavorite(productId: String, userId: String): Int
}
