package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.repository.ChannelsRepository
import com.simplevideo.whiteiptv.domain.repository.FIXMEChannel

class GetChannelsUseCase(
    private val channelsRepository: ChannelsRepository
) {
    suspend operator fun invoke(): List<FIXMEChannel> {
        return channelsRepository.getChannels()
    }
}
