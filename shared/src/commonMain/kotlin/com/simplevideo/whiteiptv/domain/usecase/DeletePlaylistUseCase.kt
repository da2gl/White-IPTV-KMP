package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository

/**
 * Delete playlist and all associated data (channels, groups, cross-refs cascade via FK)
 *
 * @return true if this was the last playlist (caller should navigate to Onboarding)
 */
class DeletePlaylistUseCase(
    private val playlistRepository: PlaylistRepository,
) {
    suspend operator fun invoke(playlistId: Long): Boolean {
        playlistRepository.deletePlaylist(playlistId)
        return !playlistRepository.hasPlaylist()
    }
}
