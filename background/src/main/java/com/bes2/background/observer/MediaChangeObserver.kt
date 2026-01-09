package com.bes2.background.observer

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bes2.background.worker.PhotoDiscoveryWorker
import com.bes2.data.dao.ReviewItemDao
import com.bes2.data.dao.TrashItemDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

private const val DEBUG_TAG = "MediaDetectorDebug"

class MediaChangeObserver(
    private val context: Context,
    handler: Handler,
    private val reviewItemDao: ReviewItemDao,
    private val trashItemDao: TrashItemDao, 
    private val workManager: WorkManager,
    private val scope: CoroutineScope
) : ContentObserver(handler) {

    private val recentlyProcessedUris = mutableSetOf<Uri>()
    private val handlerForDebounce = Handler(handler.looper)

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        uri ?: return

        // [CRITICAL FIX] If it's a self-change (caused by our own app deleting/moving), IGNORE IT.
        // This prevents the infinite loop of "Delete -> Observer triggers -> Worker runs -> App crashes".
        if (selfChange) {
            Timber.tag(DEBUG_TAG).d("Ignored self-change for URI: $uri")
            return
        }

        synchronized(this) {
            if (recentlyProcessedUris.contains(uri)) return
            recentlyProcessedUris.add(uri)
        }

        handlerForDebounce.postDelayed({
            synchronized(this) {
                recentlyProcessedUris.remove(uri)
            }
        }, 2000)

        scope.launch {
            try {
                // [OPTIMIZATION] Verify existence quickly before doing heavy DB/WorkManager ops.
                // If the file was deleted, the cursor will be empty. We should just STOP here.
                val cursor = context.contentResolver.query(
                    uri,
                    arrayOf(MediaStore.Images.Media._ID), // Query minimal column
                    null, null, null
                )
                
                cursor?.use {
                    if (it.count == 0 || !it.moveToFirst()) {
                        Timber.tag(DEBUG_TAG).d("File deleted or not found: $uri. Ignoring.")
                        return@launch // EXIT IMMEDIATELY
                    }
                }

                // If file exists, check if we already processed it
                val isProcessed = reviewItemDao.isUriProcessed(uri.toString()) || trashItemDao.isUriProcessed(uri.toString())
                if (isProcessed) {
                    return@launch
                }
                
                Timber.tag(DEBUG_TAG).i("New valid photo detected: $uri. Triggering Worker.")
                
                val inputData = Data.Builder()
                    .putBoolean(PhotoDiscoveryWorker.KEY_IS_INSTANT_MODE, true)
                    .build()
                
                // Increased delay to 5 seconds to group burst shots better and reduce load
                val discoveryWorkRequest = OneTimeWorkRequestBuilder<PhotoDiscoveryWorker>()
                    .setInputData(inputData)
                    .setInitialDelay(5, TimeUnit.SECONDS) 
                    .build()
                
                workManager.enqueueUniqueWork(
                    PhotoDiscoveryWorker.WORK_NAME + "_OnChange",
                    ExistingWorkPolicy.REPLACE,
                    discoveryWorkRequest
                )

            } catch (e: Exception) {
                Timber.tag(DEBUG_TAG).e(e, "Error processing media change for URI: $uri")
            }
        }
    }
}
