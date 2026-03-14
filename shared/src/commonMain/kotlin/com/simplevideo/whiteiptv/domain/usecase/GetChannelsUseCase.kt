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
    operator fun invoke(
        filter: ChannelsFilter = ChannelsFilter.All,
        query: String = "",
    ): Flow<List<ChannelEntity>> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return when (filter) {
                is ChannelsFilter.All -> channelRepository.getAllChannels()
                is ChannelsFilter.ByPlaylist -> channelRepository.getChannelsByPlaylistId(filter.playlistId)
                is ChannelsFilter.ByGroup -> channelRepository.getChannelsByGroupId(filter.groupId)
            }
        }
        return when (filter) {
            is ChannelsFilter.All -> channelRepository.searchChannels(trimmedQuery)
            is ChannelsFilter.ByPlaylist -> channelRepository.searchChannelsByPlaylistId(
                trimmedQuery,
                filter.playlistId
            )
            is ChannelsFilter.ByGroup -> channelRepository.searchChannelsByGroupId(trimmedQuery, filter.groupId)
        }
    }
}
