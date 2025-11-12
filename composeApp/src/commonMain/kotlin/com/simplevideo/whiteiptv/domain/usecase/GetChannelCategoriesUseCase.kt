package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.model.ChannelCategory
import com.simplevideo.whiteiptv.domain.repository.ChannelsRepository

class GetChannelCategoriesUseCase(
    private val channelsRepository: ChannelsRepository
) {
    suspend operator fun invoke(): List<ChannelCategory> {
        return channelsRepository.getChannelCategories()
    }
}
