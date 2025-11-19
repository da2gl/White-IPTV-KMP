package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import kotlinx.coroutines.flow.first

class GetFavoritesUseCase(
    private val channelRepository: ChannelRepository,
) {
    suspend operator fun invoke(): List<ChannelEntity> =
        channelRepository.getFavoriteChannels().first()
}
