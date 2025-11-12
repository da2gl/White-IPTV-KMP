package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository

class ToggleFavoriteUseCase(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(channelId: Long) {
        playlistRepository.toggleFavoriteStatus(channelId)
    }
}
