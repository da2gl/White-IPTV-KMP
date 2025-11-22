package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.domain.model.ChannelsFilter
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving channels with flexible filtering
 * Supports all channels, by playlist, or by group
 */
class GetChannelsUseCase(
    private val channelRepository: ChannelRepository,
) {
    operator fun invoke(filter: ChannelsFilter = ChannelsFilter.All): Flow<List<ChannelEntity>> =
        when (filter) {
            is ChannelsFilter.All -> channelRepository.getAllChannels()
            is ChannelsFilter.ByPlaylist -> channelRepository.getChannelsByPlaylistId(filter.playlistId)
            is ChannelsFilter.ByGroup -> channelRepository.getChannelsByGroupId(filter.groupId)
        }
}
