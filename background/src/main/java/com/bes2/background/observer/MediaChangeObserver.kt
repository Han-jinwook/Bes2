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
    private val workManager: WorkManager, // Re-added WorkManager dependency
    private val scope: CoroutineScope
) : ContentObserver(handler) {

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        uri ?: return

        if (uri == lastProcessedUri && (System.currentTimeMillis() - lastProcessedTime) < 1000) {
            Timber.tag(DEBUG_TAG).d("Debounced duplicate URI: $uri")
            return
        }
        lastProcessedUri = uri
        lastProcessedTime = System.currentTimeMillis()

        Timber.tag(DEBUG_TAG).i("Media change detected for URI: $uri")

        scope.launch {
            try {
                context.contentResolver.query(
                    uri,
                    arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED),
                    null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                        val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED))
                        Timber.tag(DEBUG_TAG).d("Queried image details: Path=$path, Timestamp=$timestamp")

                        if (imageItemDao.isUriProcessed(uri.toString())) {
                            Timber.tag(DEBUG_TAG).w("URI has already been processed: $uri")
                            return@launch
                        }

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

                        // --- PIPELINE FIX: START WITH CLUSTERING ---
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

    companion object {
        private var lastProcessedUri: Uri? = null
        private var lastProcessedTime: Long = 0
    }
}
