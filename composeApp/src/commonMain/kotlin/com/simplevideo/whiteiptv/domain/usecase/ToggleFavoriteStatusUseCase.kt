package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.repository.ChannelsRepository
import com.simplevideo.whiteiptv.domain.repository.FIXMEChannel

class ToggleFavoriteStatusUseCase(
    private val channelsRepository: ChannelsRepository
) {
    suspend operator fun invoke(channelId: String): FIXMEChannel {
        return channelsRepository.toggleFavoriteStatus(channelId)
    }
}
