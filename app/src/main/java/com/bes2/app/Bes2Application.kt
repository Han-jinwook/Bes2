package com.bes2.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bes2.background.notification.NotificationHelper
import com.bes2.background.worker.PhotoDiscoveryWorker
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class Bes2Application : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        val TAG = "Bes2Application"
        Timber.tag(TAG).d("onCreate - START")

        Timber.plant(Timber.DebugTree())
        
        NotificationHelper.createNotificationChannels(this)
        Timber.tag(TAG).d("onCreate - NotificationHelper OK")

        // [TEMPORARY FIX] Disable AdMob initialization to check if it's the cause of the crash.
        // MobileAds.initialize(this) {}
        Timber.tag(TAG).d("onCreate - MobileAds initialization SKIPPED")

        applicationScope.launch(Dispatchers.IO) {
            Timber.tag(TAG).d("onCreate - Coroutine for periodic work START")
            setupPeriodicWork()
            Timber.tag(TAG).d("onCreate - Coroutine for periodic work END")
        }
        Timber.tag(TAG).d("onCreate - END")
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
            
    private fun setupPeriodicWork() {
        val TAG = "Bes2Application"
        Timber.tag(TAG).d("setupPeriodicWork - START on background thread")
        
        try {
            val workManager = WorkManager.getInstance(this)
            
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresCharging(true)
                .build()

            val periodicWorkRequest = PeriodicWorkRequestBuilder<PhotoDiscoveryWorker>(
                12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                PhotoDiscoveryWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
            Timber.tag(TAG).d("setupPeriodicWork - Work enqueued successfully")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "setupPeriodicWork - FAILED")
        }
    }
}
