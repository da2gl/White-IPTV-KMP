package com.simplevideo.whiteiptv.platform

/**
 * Platform-specific background task scheduler.
 *
 * Android: WorkManager PeriodicWorkRequest
 * iOS: BGTaskScheduler with BGAppRefreshTaskRequest
 */
interface BackgroundScheduler {
    /**
     * Schedule periodic playlist refresh at the given interval.
     * Replaces any existing schedule. Requires network connectivity.
     *
     * @param intervalSeconds refresh interval in seconds (minimum 900 = 15 min)
     */
    fun schedule(intervalSeconds: Long)

    /** Cancel any scheduled periodic refresh. */
    fun cancel()

    /** Returns true if a periodic refresh is currently scheduled. */
    fun isScheduled(): Boolean
}
