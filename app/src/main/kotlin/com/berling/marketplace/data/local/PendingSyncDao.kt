package com.berling.marketplace.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.berling.marketplace.data.local.entities.PendingSyncEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingSyncDao {
    @Insert
    suspend fun insertPendingSync(sync: PendingSyncEntity)

    @Update
    suspend fun updatePendingSync(sync: PendingSyncEntity)

    @Query("SELECT * FROM pending_syncs ORDER BY createdAt ASC")
    suspend fun getAllPendingSyncs(): List<PendingSyncEntity>

    @Query("SELECT * FROM pending_syncs WHERE id = :id")
    suspend fun getPendingSyncById(id: Int): PendingSyncEntity?

    @Query("SELECT * FROM pending_syncs WHERE retryCount < 3 ORDER BY createdAt ASC LIMIT 10")
    suspend fun getPendingSyncsToSync(): List<PendingSyncEntity>

    @Delete
    suspend fun deletePendingSync(sync: PendingSyncEntity)

    @Query("DELETE FROM pending_syncs WHERE id = :id")
    suspend fun deletePendingSyncById(id: Int)

    @Query("SELECT COUNT(*) FROM pending_syncs")
    suspend fun getPendingSyncCount(): Int
}
