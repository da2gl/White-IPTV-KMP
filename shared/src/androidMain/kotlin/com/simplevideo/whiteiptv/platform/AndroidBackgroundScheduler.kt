package com.simplevideo.whiteiptv.platform

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Android [BackgroundScheduler] implementation using WorkManager.
 *
 * Enqueues a [PlaylistRefreshWorker] as a unique periodic work request.
 * WorkManager handles Doze mode, battery optimization, and persistence
 * across device reboots.
 */
class AndroidBackgroundScheduler(
    private val context: Context,
) : BackgroundScheduler {

    override fun schedule(intervalSeconds: Long) {
        val intervalMinutes = (intervalSeconds / 60).coerceAtLeast(15)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<PlaylistRefreshWorker>(
            intervalMinutes,
            TimeUnit.MINUTES,
        ).setConstraints(constraints).build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
    }

    override fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    override fun isScheduled(): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(WORK_NAME)
            .get()
        return workInfos.any { !it.state.isFinished }
    }

    companion object {
        const val WORK_NAME = "playlist_auto_refresh"
    }
}
