package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository

class HasPlaylistUseCase(
    private val playlistRepository: PlaylistRepository,
) {
    suspend operator fun invoke(): Boolean =
        playlistRepository.hasPlaylist()
}
