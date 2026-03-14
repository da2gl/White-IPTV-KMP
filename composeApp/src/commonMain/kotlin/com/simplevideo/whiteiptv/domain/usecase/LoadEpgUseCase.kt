package com.simplevideo.whiteiptv.domain.usecase

import co.touchlab.kermit.Logger
import com.simplevideo.whiteiptv.domain.repository.EpgRepository
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository

/**
 * Loads EPG data for a playlist by downloading its XMLTV source and storing programs in the database.
 * Silently does nothing if the playlist has no urlTvg. Errors are logged but not thrown (EPG is non-critical).
 */
class LoadEpgUseCase(
    private val playlistRepository: PlaylistRepository,
    private val epgRepository: EpgRepository,
) {
    suspend operator fun invoke(playlistId: Long) {
        val playlist = playlistRepository.getPlaylistById(playlistId) ?: return
        val xmltvUrl = playlist.urlTvg ?: return

        runCatching {
            epgRepository.loadEpg(
                playlistId = playlistId,
                xmltvUrl = xmltvUrl,
                tvgShiftHours = playlist.tvgShift ?: 0,
            )
        }.onFailure { e ->
            Logger.withTag(TAG).w { "Failed to load EPG for playlist $playlistId: ${e.message}" }
        }
    }

    companion object {
        private const val TAG = "LoadEpgUseCase"
    }
}
