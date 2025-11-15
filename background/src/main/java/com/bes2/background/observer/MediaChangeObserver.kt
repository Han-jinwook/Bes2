package com.bes2.background.observer

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bes2.background.worker.ClusteringWorker
import com.bes2.data.dao.ImageItemDao
import com.bes2.data.model.ImageItemEntity
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

    // --- DEBOUNCE LOGIC ENHANCEMENT ---
    // Use a simple in-memory cache to track recently processed URIs.
    private val recentlyProcessedUris = mutableSetOf<Uri>()
    private val handlerForDebounce = Handler(handler.looper)

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        Timber.tag(DEBUG_TAG).d("--- onChange TRIGGERED by system --- URI: $uri")
        super.onChange(selfChange, uri)
        uri ?: return

        // --- DEBOUNCE LOGIC ENHANCEMENT ---
        // If the URI is already in our recent set, ignore this trigger.
        synchronized(this) {
            if (recentlyProcessedUris.contains(uri)) {
                Timber.tag(DEBUG_TAG).d("Debounced duplicate URI: $uri")
                return
            }
            recentlyProcessedUris.add(uri)
        }

        // Remove the URI from the set after a delay to allow for legitimate new events.
        handlerForDebounce.postDelayed({
            synchronized(this) {
                recentlyProcessedUris.remove(uri)
            }
        }, 2000) // Increased debounce window to 2 seconds for stability.

        Timber.tag(DEBUG_TAG).i("Media change detected for URI: $uri")

        scope.launch {
            try {
                // Check if this exact URI has been fully processed and saved to DB
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
                        
                        val newImage = ImageItemEntity(
                            uri = uri.toString(),
                            filePath = path,
                            timestamp = timestamp * 1000,
                            status = "NEW",
                            pHash = null, nimaScore = null, blurScore = null, exposureScore = null,
                            areEyesClosed = null, smilingProbability = null, faceEmbedding = null
                        )
                        imageItemDao.insertImageItem(newImage)
                        Timber.tag(DEBUG_TAG).i("SUCCESS: Saved new image to DB. URI: ${newImage.uri}")

                        val clusteringWorkRequest = OneTimeWorkRequestBuilder<ClusteringWorker>()
                            .setInitialDelay(1, TimeUnit.MINUTES)
                            .build()
                        
                        workManager.enqueueUniqueWork(
                            ClusteringWorker.WORK_NAME,
                            ExistingWorkPolicy.REPLACE,
                            clusteringWorkRequest
                        )
                        Timber.tag(DEBUG_TAG).i("Enqueued clustering work. Will run in 1 minute.")

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
