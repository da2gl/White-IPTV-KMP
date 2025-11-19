package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import kotlinx.coroutines.flow.Flow

class GetFavoritesUseCase(
    private val channelRepository: ChannelRepository,
) {
    operator fun invoke(): Flow<List<ChannelEntity>> {
        return channelRepository.getFavoriteChannels()
    }
}
