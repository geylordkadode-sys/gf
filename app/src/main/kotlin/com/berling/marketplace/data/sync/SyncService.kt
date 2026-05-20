package com.berling.marketplace.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.berling.marketplace.data.local.MarketplaceDatabase
import com.berling.marketplace.data.local.PendingSyncDao
import com.berling.marketplace.data.local.entities.PendingSyncEntity
import com.berling.marketplace.data.remote.SupabaseApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncService @Inject constructor(
    private val database: MarketplaceDatabase,
    private val api: SupabaseApi,
    @ApplicationContext private val context: Context
) {
    private val pendingSyncDao: PendingSyncDao = database.pendingSyncDao()
    private val scope = CoroutineScope(Dispatchers.Default)
    private val json = Json

    init {
        startPeriodicSync()
    }

    private fun startPeriodicSync() {
        scope.launch {
            while (true) {
                delay(300000) // Sync every 5 minutes
                if (isNetworkAvailable()) {
                    syncPendingChanges()
                }
            }
        }
    }

    suspend fun syncPendingChanges() {
        try {
            val pendingSyncs = pendingSyncDao.getPendingSyncsToSync()
            if (pendingSyncs.isEmpty()) return

            // Group syncs by entity type
            val groupedSyncs = pendingSyncs.groupBy { it.entityType }

            for ((entityType, syncs) in groupedSyncs) {
                try {
                    // Send to Supabase edge function for syncing
                    when (entityType) {
                        "product" -> syncProducts(syncs)
                        "user" -> syncUsers(syncs)
                        "favorite" -> syncFavorites(syncs)
                        else -> {}
                    }
                    
                    // Mark as synced
                    syncs.forEach { sync ->
                        pendingSyncDao.deletePendingSyncById(sync.id)
                    }
                } catch (e: Exception) {
                    // Increment retry count
                    syncs.forEach { sync ->
                        if (sync.retryCount < 3) {
                            val updated = sync.copy(retryCount = sync.retryCount + 1)
                            pendingSyncDao.updatePendingSync(updated)
                        } else {
                            // Remove after max retries
                            pendingSyncDao.deletePendingSyncById(sync.id)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun syncProducts(syncs: List<PendingSyncEntity>) {
        syncs.forEach { sync ->
            try {
                // TODO: Implement actual API call to sync products
                // val data = json.decodeFromString<ProductData>(sync.data)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private suspend fun syncUsers(syncs: List<PendingSyncEntity>) {
        syncs.forEach { sync ->
            try {
                // TODO: Implement actual API call to sync users
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private suspend fun syncFavorites(syncs: List<PendingSyncEntity>) {
        syncs.forEach { sync ->
            try {
                // TODO: Implement actual API call to sync favorites
            } catch (e: Exception) {
                throw e
            }
        }
    }

    fun isOfflineMode(): Boolean = !isNetworkAvailable()

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

