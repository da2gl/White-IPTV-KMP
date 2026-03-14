package com.simplevideo.whiteiptv.platform

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simplevideo.whiteiptv.data.scheduler.BackgroundRefreshCoordinator
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * WorkManager worker that refreshes all URL-based playlists in the background.
 *
 * Resolves [BackgroundRefreshCoordinator] from Koin to perform the refresh.
 * Network connectivity is guaranteed by the work constraints.
 */
class PlaylistRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {

    private val coordinator: BackgroundRefreshCoordinator by inject()

    override suspend fun doWork(): Result {
        return runCatching {
            coordinator.refreshAllPlaylists()
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }
}
