package com.simplevideo.whiteiptv.platform

import co.touchlab.kermit.Logger
import com.simplevideo.whiteiptv.common.AppLogger
import com.simplevideo.whiteiptv.data.scheduler.BackgroundRefreshCoordinator
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTask
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSinceNow

/**
 * iOS [BackgroundScheduler] implementation using BGTaskScheduler.
 *
 * Submits [BGAppRefreshTaskRequest] for periodic playlist refresh.
 * The system determines the actual execution timing based on app usage
 * patterns, battery level, and network availability.
 *
 * Important: The BGTask handler must be registered in iOSApp.swift on launch,
 * and the task identifier must be declared in Info.plist under
 * BGTaskSchedulerPermittedIdentifiers.
 */
class IOSBackgroundScheduler(
    private val coordinator: BackgroundRefreshCoordinator,
) : BackgroundScheduler {

    private val log = Logger.withTag(AppLogger.Tags.AUTO_REFRESH)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var scheduled = false

    override fun schedule(intervalSeconds: Long) {
        submitTaskRequest(intervalSeconds)
        scheduled = true
    }

    override fun cancel() {
        BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(TASK_IDENTIFIER)
        scheduled = false
    }

    override fun isScheduled(): Boolean = scheduled

    /**
     * Called by the BGTask handler registered in iOSApp.swift.
     * Performs the refresh and resubmits the task for the next cycle.
     */
    fun handleBackgroundTask(task: BGTask) {
        scope.launch {
            runCatching {
                coordinator.refreshAllPlaylists()
            }.onFailure { e ->
                log.e(e) { "Background refresh failed" }
            }
            task.setTaskCompletedWithSuccess(true)
        }
        val interval = runBlocking {
            coordinator.calculateIntervalSeconds()
        }
        submitTaskRequest(interval)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun submitTaskRequest(intervalSeconds: Long) {
        val request = BGAppRefreshTaskRequest(identifier = TASK_IDENTIFIER)
        request.earliestBeginDate = NSDate.dateWithTimeIntervalSinceNow(
            intervalSeconds.toDouble(),
        )
        runCatching {
            BGTaskScheduler.sharedScheduler.submitTaskRequest(request, error = null)
        }.onFailure { e ->
            log.e(e) { "Failed to submit background task request" }
        }
    }

    companion object {
        const val TASK_IDENTIFIER = "com.simplevideo.whiteiptv.playlistRefresh"
    }
}
