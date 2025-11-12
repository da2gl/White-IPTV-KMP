package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.model.Channel
import com.simplevideo.whiteiptv.domain.repository.ChannelsRepository

class GetChannelsUseCase(
    private val channelsRepository: ChannelsRepository
) {
    suspend operator fun invoke(): List<Channel> {
        return channelsRepository.getChannels()
    }
}
