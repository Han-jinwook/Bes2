package com.bes2.background.observer

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bes2.background.worker.PhotoDiscoveryWorker
import com.bes2.data.dao.ImageItemDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

private const val DEBUG_TAG = "MediaDetectorDebug"

class MediaChangeObserver(
    private val context: Context,
    handler: Handler,
    private val imageItemDao: ImageItemDao,
    private val workManager: WorkManager,
    private val scope: CoroutineScope
) : ContentObserver(handler) {

    private val recentlyProcessedUris = mutableSetOf<Uri>()
    private val handlerForDebounce = Handler(handler.looper)

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        Timber.tag(DEBUG_TAG).d("--- onChange TRIGGERED by system --- URI: $uri")
        super.onChange(selfChange, uri)
        uri ?: return

        synchronized(this) {
            if (recentlyProcessedUris.contains(uri)) {
                Timber.tag(DEBUG_TAG).d("Debounced duplicate URI: $uri")
                return
            }
            recentlyProcessedUris.add(uri)
        }

        handlerForDebounce.postDelayed({
            synchronized(this) {
                recentlyProcessedUris.remove(uri)
            }
        }, 2000)

        Timber.tag(DEBUG_TAG).i("Media change detected for URI: $uri")

        scope.launch {
            try {
                if (imageItemDao.isUriProcessed(uri.toString())) {
                    Timber.tag(DEBUG_TAG).w("URI has already been saved to DB: $uri")
                    return@launch
                }
                
                context.contentResolver.query(
                    uri,
                    arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED),
                    null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                        val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED))
                        Timber.tag(DEBUG_TAG).d("Queried image details: Path=$path, Timestamp=$timestamp")
                        
                        Timber.tag(DEBUG_TAG).i("Triggering PhotoDiscoveryWorker due to media change.")
                        
                        val discoveryWorkRequest = OneTimeWorkRequestBuilder<PhotoDiscoveryWorker>()
                            .setInitialDelay(20, TimeUnit.SECONDS) // Delay to batch potential multiple new photos
                            .build()
                        
                        workManager.enqueueUniqueWork(
                            PhotoDiscoveryWorker.WORK_NAME + "_OnChange", // Use a unique name to not conflict with periodic work
                            ExistingWorkPolicy.REPLACE,
                            discoveryWorkRequest
                        )
                        Timber.tag(DEBUG_TAG).i("Enqueued PhotoDiscoveryWorker. Will run in 20 seconds.")

                    } else {
                        Timber.tag(DEBUG_TAG).w("Could not move cursor to first. URI: $uri may no longer exist.")
                    }
                }
            } catch (e: Exception) {
                Timber.tag(DEBUG_TAG).e(e, "Error processing media change for URI: $uri")
            }
        }
    }
}
