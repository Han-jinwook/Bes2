package com.bes2.background.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.work.WorkManager
import com.bes2.background.observer.MediaChangeObserver
import com.bes2.data.dao.ImageItemDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import timber.log.Timber
import javax.inject.Inject

private const val DEBUG_TAG = "MediaDetectorDebug"

@AndroidEntryPoint
class MediaDetectionService : Service() {

    @Inject
    lateinit var imageDao: ImageItemDao

    @Inject
    lateinit var workManager: WorkManager // Injected to be passed to the observer

    private var mediaObserver: MediaChangeObserver? = null
    private var handlerThread: HandlerThread? = null

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "MediaDetectionChannel"
        const val NOTIFICATION_ID = 101
    }

    override fun onCreate() {
        super.onCreate()
        Timber.tag(DEBUG_TAG).d("MediaDetectionService onCreate: Initializing...")

        handlerThread = HandlerThread("MediaObserverThread").apply {
            start()
            val handler = Handler(looper)
            
            // Observer is now created with WorkManager to schedule analysis tasks.
            mediaObserver = MediaChangeObserver(applicationContext, handler, imageDao, workManager, serviceScope)
            
            try {
                contentResolver.registerContentObserver(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    true,
                    mediaObserver!!
                )
                Timber.tag(DEBUG_TAG).i("MediaChangeObserver registered successfully.")
            } catch (e: SecurityException) {
                Timber.tag(DEBUG_TAG).e(e, "Failed to register MediaChangeObserver. Check permissions.")
                stopSelf()
                return
            }
        }
        Timber.tag(DEBUG_TAG).d("MediaDetectionService onCreate completed.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.tag(DEBUG_TAG).d("onStartCommand received.")
        createNotificationChannel()
        val notification = createNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            Timber.tag(DEBUG_TAG).i("MediaDetectionService started in foreground.")
        } catch (e: Exception) {
            Timber.tag(DEBUG_TAG).e(e, "Error starting foreground service: ${e.message}")
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Bes2 Media Detection",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("사진 감지 서비스")
            .setContentText("새로운 사진을 감지하고 있습니다.")
            .setSmallIcon(android.R.drawable.stat_notify_sync_noanim)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaObserver?.let { contentResolver.unregisterContentObserver(it) }
        handlerThread?.quitSafely()
        serviceScope.cancel()
        Timber.tag(DEBUG_TAG).w("MediaDetectionService onDestroy: Cleaned up resources.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
