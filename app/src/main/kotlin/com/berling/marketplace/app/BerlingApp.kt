package com.berling.marketplace.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.berling.marketplace.data.sync.BackgroundSyncService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BerlingApp : Application(), Configuration.Provider {

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    @Inject
    lateinit var syncService: BackgroundSyncService

    override fun onCreate() {
        super.onCreate()
        
        // Initialize background sync service
        syncService.scheduleSyncWork()
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(hiltWorkerFactory)
            .build()
}
