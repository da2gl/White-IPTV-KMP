package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.exception.PlaylistException
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository

/**
 * Rename playlist display name
 *
 * @throws PlaylistException.NotFound if playlist doesn't exist
 * @throws IllegalArgumentException if name is blank
 */
class RenamePlaylistUseCase(
    private val playlistRepository: PlaylistRepository,
) {
    suspend operator fun invoke(playlistId: Long, newName: String) {
        require(newName.isNotBlank()) { "Playlist name cannot be blank" }
        val playlist = playlistRepository.getPlaylistById(playlistId)
            ?: throw PlaylistException.NotFound(playlistId)
        playlistRepository.updatePlaylist(playlist.copy(name = newName.trim()))
    }
}
