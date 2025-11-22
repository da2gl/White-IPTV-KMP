package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupEntity
import com.simplevideo.whiteiptv.domain.model.ChannelGroup
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Fetches top groups with random channels for home screen display
 *
 * Priority order:
 * 1. Filter out invalid groups (undefined, unknown, blank)
 * 2. Select priority groups (news, sport, music, general) if available
 * 3. Fill remaining slots with top groups by channel count
 */
class GetCategoriesUseCase(
    private val channelRepository: ChannelRepository,
) {
    private val invalidNames = setOf("undefined", "unknown", "other", "")
    private val priorityKeywords = listOf("news", "sport", "music", "general")

    operator fun invoke(
        selection: PlaylistSelection = PlaylistSelection.All,
        groupLimit: Int = 5,
        channelsPerGroup: Int = 10,
    ): Flow<List<Pair<ChannelGroup, List<ChannelEntity>>>> = flow {
        val allGroups = when (selection) {
            is PlaylistSelection.Selected ->
                channelRepository.getTopGroupsByPlaylist(selection.id, limit = 20).first()

            PlaylistSelection.All ->
                channelRepository.getTopGroups(limit = 20).first()
        }

        val validGroups = allGroups.filter { group ->
            group.name.isNotBlank() && group.name.lowercase() !in invalidNames
        }

        val selectedGroups = selectGroups(validGroups, groupLimit)

        val result = coroutineScope {
            selectedGroups.map { group ->
                async {
                    val channelGroup = ChannelGroup(
                        id = group.id.toString(),
                        displayName = group.name,
                        icon = group.icon,
                        channelCount = group.channelCount,
                        playlistId = group.playlistId,
                    )
                    val channels = channelRepository.getRandomChannelsByGroupId(
                        groupId = group.id,
                        limit = channelsPerGroup,
                    )
                    channelGroup to channels
                }
            }.awaitAll()
        }
        emit(result)
    }

    private fun selectGroups(
        validGroups: List<ChannelGroupEntity>,
        limit: Int,
    ): List<ChannelGroupEntity> {
        val priorityGroups = priorityKeywords.mapNotNull { keyword ->
            validGroups.find { it.name.lowercase().contains(keyword) }
        }

        val remainingGroups = validGroups
            .filter { it !in priorityGroups }
            .take(limit - priorityGroups.size)

        return (priorityGroups + remainingGroups).take(limit)
    }
}
