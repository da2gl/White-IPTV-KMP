package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving all playlists as a Flow
 * Used in screens that need to filter content by playlist
 */
class GetPlaylistsUseCase(
    private val playlistRepository: PlaylistRepository,
) {
    operator fun invoke(): Flow<List<PlaylistEntity>> =
        playlistRepository.getPlaylists()
}
