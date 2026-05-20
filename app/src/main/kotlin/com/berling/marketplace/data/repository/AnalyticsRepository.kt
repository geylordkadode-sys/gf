package com.berling.marketplace.data.repository

import com.berling.marketplace.data.local.AnalyticsDao
import com.berling.marketplace.data.local.entities.AnalyticsEventEntity
import com.berling.marketplace.data.remote.SupabaseApi
import com.berling.marketplace.data.remote.models.AnalyticsEventRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AnalyticsRepository @Inject constructor(
    private val analyticsDao: AnalyticsDao,
    private val api: SupabaseApi
) {

    fun getUserEvents(userId: String): Flow<List<AnalyticsEventEntity>> {
        return analyticsDao.getUserEvents(userId)
    }

    suspend fun getUnsyncedEvents(): List<AnalyticsEventEntity> {
        return analyticsDao.getUnsyncedEvents()
    }

    suspend fun markEventAsSynced(id: Int) {
        analyticsDao.markEventAsSynced(id)
    }

    suspend fun deleteOldEvents() {
        analyticsDao.deleteOldEvents()
    }

    suspend fun getEventCountForUser(userId: String): Int {
        return analyticsDao.getEventCountForUser(userId)
    }

    fun getEventsByName(eventName: String): Flow<List<AnalyticsEventEntity>> {
        return analyticsDao.getEventsByName(eventName)
    }

    suspend fun logEvent(eventName: String, eventData: String = "", userId: String) {
        val event = AnalyticsEventEntity(
            eventName = eventName,
            eventData = eventData,
            timestamp = System.currentTimeMillis().toString(),
            userId = userId,
            isSynced = false
        )

        // Save to local DB
        analyticsDao.insertEvent(event)

        // Attempt to sync to remote
        try {
            val request = AnalyticsEventRequest(
                eventName = eventName,
                eventData = emptyMap(),
                timestamp = event.timestamp
            )
            api.logEvent("Bearer YOUR_TOKEN", request)
            analyticsDao.markEventAsSynced(event.id)
        } catch (e: Exception) {
            // Event saved locally, will be synced later
        }
    }

    suspend fun syncUnsyncedEvents() {
        val unsyncedEvents = getUnsyncedEvents()
        for (event in unsyncedEvents) {
            try {
                val request = AnalyticsEventRequest(
                    eventName = event.eventName,
                    eventData = emptyMap(),
                    timestamp = event.timestamp
                )
                api.logEvent("Bearer YOUR_TOKEN", request)
                markEventAsSynced(event.id)
            } catch (e: Exception) {
                // Continue with next event
            }
        }
    }
}
