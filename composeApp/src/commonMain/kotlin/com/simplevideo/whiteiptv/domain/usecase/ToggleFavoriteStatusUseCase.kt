package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.model.Channel
import com.simplevideo.whiteiptv.domain.repository.ChannelsRepository

class ToggleFavoriteStatusUseCase(
    private val channelsRepository: ChannelsRepository
) {
    suspend operator fun invoke(channelId: String): Channel {
        return channelsRepository.toggleFavoriteStatus(channelId)
    }
}
