package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.mapper.ChannelGroupMapper
import com.simplevideo.whiteiptv.domain.model.ChannelGroup
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetGroupsUseCase(
    private val channelRepository: ChannelRepository,
    private val channelGroupMapper: ChannelGroupMapper,
) {
    operator fun invoke(selection: PlaylistSelection = PlaylistSelection.All): Flow<List<ChannelGroup>> =
        channelRepository.getAllGroups().map { groups ->
            val filteredGroups = when (selection) {
                is PlaylistSelection.Selected ->
                    groups.filter { it.playlistId == selection.id }

                PlaylistSelection.All -> groups
            }

            channelGroupMapper.toDomainList(filteredGroups)
        }
}
