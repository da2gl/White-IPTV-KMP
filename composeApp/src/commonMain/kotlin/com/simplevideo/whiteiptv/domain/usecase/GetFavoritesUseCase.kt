package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving favorite channels
 * Supports filtering by playlist selection or getting all favorites
 */
class GetFavoritesUseCase(
    private val channelRepository: ChannelRepository,
) {
    operator fun invoke(selection: PlaylistSelection = PlaylistSelection.All): Flow<List<ChannelEntity>> =
        when (selection) {
            is PlaylistSelection.Selected -> channelRepository.getFavoriteChannelsByPlaylist(selection.id)
            is PlaylistSelection.All -> channelRepository.getFavoriteChannels()
        }
}
