package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving favorite channels
 * Supports filtering by playlist ID or getting all favorites
 */
class GetFavoritesUseCase(
    private val channelRepository: ChannelRepository,
) {
    operator fun invoke(playlistId: Long? = null): Flow<List<ChannelEntity>> =
        if (playlistId != null) {
            channelRepository.getFavoriteChannelsByPlaylist(playlistId)
        } else {
            channelRepository.getFavoriteChannels()
        }
}
