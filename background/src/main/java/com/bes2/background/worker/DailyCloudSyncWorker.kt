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
class DailyCloudSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val reviewItemDao: ReviewItemDao
) : CoroutineWorker(appContext, workerParams) {
    
    override suspend fun doWork(): Result {
        Timber.d("Sync worker started.")
        // TODO: Re-implement sync with ReviewItemDao
        return Result.success()
    }
    
    companion object {
        const val WORK_NAME = "DailyCloudSyncWorker"
        const val KEY_IS_ONE_TIME_SYNC = "is_one_time_sync"
        const val KEY_SYNCED_COUNT = "key_synced_count"
    }
}
