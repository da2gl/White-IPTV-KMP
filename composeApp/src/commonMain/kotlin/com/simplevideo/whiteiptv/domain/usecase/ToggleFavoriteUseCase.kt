package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.repository.ChannelRepository

class ToggleFavoriteUseCase(
    private val channelRepository: ChannelRepository,
) {
    suspend operator fun invoke(channelId: Long) {
        channelRepository.toggleFavoriteStatus(channelId)
    }
}
