package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.repository.ChannelsRepository
import com.simplevideo.whiteiptv.domain.repository.FIXMEChannelCategory

class GetChannelCategoriesUseCase(
    private val channelsRepository: ChannelsRepository
) {
    suspend operator fun invoke(): List<FIXMEChannelCategory> {
        return channelsRepository.getChannelCategories()
    }
}
