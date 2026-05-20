package com.berling.marketplace.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.berling.marketplace.data.local.entities.AnalyticsEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {
    @Insert
    suspend fun insertEvent(event: AnalyticsEventEntity)

    @Query("SELECT * FROM analytics_events WHERE userId = :userId ORDER BY timestamp DESC LIMIT 100")
    fun getUserEvents(userId: String): Flow<List<AnalyticsEventEntity>>

    @Query("SELECT * FROM analytics_events WHERE isSynced = 0 LIMIT 10")
    suspend fun getUnsyncedEvents(): List<AnalyticsEventEntity>

    @Query("UPDATE analytics_events SET isSynced = 1 WHERE id = :id")
    suspend fun markEventAsSynced(id: Int)

    @Query("DELETE FROM analytics_events WHERE timestamp < datetime('now', '-30 days')")
    suspend fun deleteOldEvents()

    @Query("SELECT COUNT(*) FROM analytics_events WHERE userId = :userId")
    suspend fun getEventCountForUser(userId: String): Int

    @Query("SELECT * FROM analytics_events WHERE eventName = :eventName ORDER BY timestamp DESC")
    fun getEventsByName(eventName: String): Flow<List<AnalyticsEventEntity>>
}
