package com.simplevideo.whiteiptv.data.scheduler

import co.touchlab.kermit.Logger
import com.simplevideo.whiteiptv.common.AppLogger
import com.simplevideo.whiteiptv.data.local.SettingsPreferences
import com.simplevideo.whiteiptv.domain.model.PlaylistSource
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Schedules periodic background refresh for URL-based playlists.
 *
 * Observes the auto-update toggle from [SettingsPreferences] and starts/stops
 * a refresh loop accordingly. Each cycle re-downloads all URL-based playlists
 * via [refreshPlaylist]. Local file playlists are skipped.
 *
 * Runs in foreground only — the coroutine scope is cancelled when the app
 * leaves composition.
 */
class PlaylistAutoRefreshScheduler(
    private val settingsPreferences: SettingsPreferences,
    private val playlistRepository: PlaylistRepository,
    private val refreshPlaylist: suspend (PlaylistSource) -> Unit,
    coroutineContext: CoroutineContext = Dispatchers.Default,
) {
    private val log = Logger.withTag(AppLogger.Tags.AUTO_REFRESH)
    private val scope = CoroutineScope(SupervisorJob() + coroutineContext)
    private var refreshJob: Job? = null

    fun start() {
        scope.launch {
            settingsPreferences.autoUpdateEnabledFlow.collect { enabled ->
                if (enabled) {
                    startRefreshLoop()
                } else {
                    stopRefreshLoop()
                }
            }
        }
    }

    private fun startRefreshLoop() {
        stopRefreshLoop()
        refreshJob = scope.launch {
            log.i { "Auto-refresh enabled, starting refresh loop" }
            while (isActive) {
                refreshAllPlaylists()
                val delayMs = calculateNextDelayMs()
                log.d { "Next refresh in ${delayMs / 1000}s" }
                delay(delayMs)
            }
        }
    }

    private fun stopRefreshLoop() {
        if (refreshJob != null) {
            log.i { "Auto-refresh disabled, stopping refresh loop" }
        }
        refreshJob?.cancel()
        refreshJob = null
    }

    private suspend fun refreshAllPlaylists() {
        val playlists = playlistRepository.getPlaylistsList()
        val urlPlaylists = playlists.filter {
            it.url.startsWith("http://") || it.url.startsWith("https://")
        }

        if (urlPlaylists.isEmpty()) {
            log.d { "No URL-based playlists to refresh" }
            return
        }

        log.i { "Refreshing ${urlPlaylists.size} playlist(s)" }
        urlPlaylists.forEach { playlist ->
            runCatching {
                refreshPlaylist(PlaylistSource.Url(playlist.url))
            }.onFailure { e ->
                log.e(e) { "Failed to refresh playlist: ${playlist.name} (id=${playlist.id})" }
            }.onSuccess {
                log.d { "Refreshed playlist: ${playlist.name} (id=${playlist.id})" }
            }
        }
    }

    private suspend fun calculateNextDelayMs(): Long {
        val playlists = playlistRepository.getPlaylistsList()
            .filter { it.url.startsWith("http://") || it.url.startsWith("https://") }
        val minInterval = playlists
            .mapNotNull { it.refreshInterval }
            .minOrNull()
            ?.coerceAtLeast(MIN_REFRESH_INTERVAL_SECONDS)
            ?: DEFAULT_REFRESH_INTERVAL_SECONDS
        return minInterval.toLong() * 1000L
    }

    fun stop() {
        stopRefreshLoop()
    }

    companion object {
        const val DEFAULT_REFRESH_INTERVAL_SECONDS = 21600 // 6 hours
        const val MIN_REFRESH_INTERVAL_SECONDS = 900 // 15 minutes
    }
}
