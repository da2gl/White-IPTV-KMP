package com.simplevideo.whiteiptv.data.scheduler

import co.touchlab.kermit.Logger
import com.simplevideo.whiteiptv.common.AppLogger
import com.simplevideo.whiteiptv.domain.model.PlaylistSource
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository

/**
 * Coordinates playlist refresh operations for background execution.
 *
 * Contains the shared business logic for refreshing URL-based playlists.
 * Called by platform-specific background workers (WorkManager on Android,
 * BGTaskScheduler on iOS).
 */
class BackgroundRefreshCoordinator(
    private val playlistRepository: PlaylistRepository,
    private val refreshPlaylist: suspend (PlaylistSource) -> Unit,
) {
    private val log = Logger.withTag(AppLogger.Tags.AUTO_REFRESH)

    /**
     * Refresh all URL-based playlists. Local file playlists are skipped.
     * Errors for individual playlists are logged but do not halt the process.
     */
    suspend fun refreshAllPlaylists() {
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

    /**
     * Calculate the optimal refresh interval based on playlist configurations.
     * Returns the minimum refreshInterval across all URL playlists, clamped
     * to at least [MIN_REFRESH_INTERVAL_SECONDS], defaulting to [DEFAULT_REFRESH_INTERVAL_SECONDS].
     */
    suspend fun calculateIntervalSeconds(): Long {
        val playlists = playlistRepository.getPlaylistsList()
            .filter { it.url.startsWith("http://") || it.url.startsWith("https://") }
        return playlists
            .mapNotNull { it.refreshInterval }
            .minOrNull()
            ?.toLong()
            ?.coerceAtLeast(MIN_REFRESH_INTERVAL_SECONDS)
            ?: DEFAULT_REFRESH_INTERVAL_SECONDS
    }

    companion object {
        const val DEFAULT_REFRESH_INTERVAL_SECONDS = 21600L // 6 hours
        const val MIN_REFRESH_INTERVAL_SECONDS = 900L // 15 minutes
    }
}
