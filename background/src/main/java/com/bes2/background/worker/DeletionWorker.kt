package com.bes2.background.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bes2.data.dao.ReviewItemDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class DeletionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val reviewItemDao: ReviewItemDao
) : CoroutineWorker(appContext, workerParams) {
    // ...
    override suspend fun doWork(): Result {
        Timber.d("Deletion worker started.")
        // TODO: Re-implement deletion with ReviewItemDao
        return Result.success()
    }
}
