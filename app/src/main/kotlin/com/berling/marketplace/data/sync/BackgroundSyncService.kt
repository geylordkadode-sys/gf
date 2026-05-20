package com.berling.marketplace.data.sync

import android.content.Context
import androidx.work.*
import androidx.hilt.work.HiltWorker
import com.berling.marketplace.data.local.PendingSyncDao
import com.berling.marketplace.data.local.ProductDao
import com.berling.marketplace.data.local.entities.PendingSyncEntity
import com.berling.marketplace.data.local.entities.ProductEntity
import com.berling.marketplace.data.remote.SupabaseApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import javax.inject.Inject
import java.util.concurrent.TimeUnit

class BackgroundSyncService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val productDao: ProductDao,
    private val pendingSyncDao: PendingSyncDao,
    private val api: SupabaseApi
) {

    fun scheduleSyncWork() {
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "product_sync_work",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    fun triggerImmediateSync() {
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "product_sync_immediate",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    suspend fun syncPendingProducts(token: String) = coroutineScope {
        try {
            val pendingSyncs = pendingSyncDao.getAllPendingSyncs()

            for (sync in pendingSyncs) {
                when (sync.operation) {
                    "create" -> syncCreateProduct(sync, token)
                    "update" -> syncUpdateProduct(sync, token)
                    "delete" -> syncDeleteProduct(sync, token)
                }
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    private suspend fun syncCreateProduct(syncEntity: PendingSyncEntity, token: String) {
        try {
            val product = productDao.getProductById(syncEntity.entityId)
            if (product != null) {
                val isSuccess = uploadProductToSupabase(product, token)
                if (isSuccess) {
                    val synced = product.copy(isSynced = true, uploadStatus = "completed")
                    productDao.updateProduct(synced)
                    pendingSyncDao.deletePendingSync(syncEntity)
                } else {
                    incrementRetryCount(syncEntity)
                }
            }
        } catch (e: Exception) {
            incrementRetryCount(syncEntity)
        }
    }

    private suspend fun syncUpdateProduct(syncEntity: PendingSyncEntity, token: String) {
        try {
            val product = productDao.getProductById(syncEntity.entityId)
            if (product != null) {
                val isSuccess = updateProductOnSupabase(product, token)
                if (isSuccess) {
                    val synced = product.copy(isSynced = true)
                    productDao.updateProduct(synced)
                    pendingSyncDao.deletePendingSync(syncEntity)
                } else {
                    incrementRetryCount(syncEntity)
                }
            }
        } catch (e: Exception) {
            incrementRetryCount(syncEntity)
        }
    }

    private suspend fun syncDeleteProduct(syncEntity: PendingSyncEntity, token: String) {
        try {
            val isSuccess = deleteProductFromSupabase(syncEntity.entityId, token)
            if (isSuccess) {
                productDao.deleteProductById(syncEntity.entityId)
                pendingSyncDao.deletePendingSync(syncEntity)
            } else {
                incrementRetryCount(syncEntity)
            }
        } catch (e: Exception) {
            incrementRetryCount(syncEntity)
        }
    }

    private suspend fun uploadProductToSupabase(product: ProductEntity, token: String): Boolean {
        return try {
            // In real app, would use api.createProduct()
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun updateProductOnSupabase(product: ProductEntity, token: String): Boolean {
        return try {
            // In real app, would use api.updateProduct()
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun deleteProductFromSupabase(productId: String, token: String): Boolean {
        return try {
            // In real app, would use api.deleteProduct()
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun incrementRetryCount(syncEntity: PendingSyncEntity) {
        val updated = syncEntity.copy(retryCount = syncEntity.retryCount + 1)
        if (updated.retryCount < 5) {
            pendingSyncDao.updatePendingSync(updated)
        } else {
            pendingSyncDao.deletePendingSync(syncEntity)
        }
    }
}

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncService: BackgroundSyncService
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // In real app, get token from preferences or auth service
            val token = "" // Get token from preferences
            syncService.syncPendingProducts(token)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val TAG = "SyncWorker"
    }
}
