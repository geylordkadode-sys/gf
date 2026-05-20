package com.berling.marketplace.di

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.berling.marketplace.data.sync.SyncWorker
import com.berling.marketplace.data.sync.BackgroundSyncService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

class CustomWorkerFactory @Inject constructor(
    private val syncService: BackgroundSyncService
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ) = when (workerClassName) {
        SyncWorker::class.java.name -> SyncWorker(appContext, workerParameters, syncService)
        else -> null
    }
}

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    @Provides
    @Singleton
    fun provideCustomWorkerFactory(syncService: BackgroundSyncService): CustomWorkerFactory {
        return CustomWorkerFactory(syncService)
    }
}
