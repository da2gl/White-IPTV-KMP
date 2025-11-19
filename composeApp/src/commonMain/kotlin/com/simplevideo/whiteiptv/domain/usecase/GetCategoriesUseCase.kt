package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.ChannelGroupEntity
import com.simplevideo.whiteiptv.domain.model.ChannelCategory
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import kotlinx.coroutines.flow.first

/**
 * Fetches top categories with random channels for home screen display
 *
 * Priority order:
 * 1. Filter out invalid categories (undefined, unknown, blank)
 * 2. Select priority categories (news, sport, music, general) if available
 * 3. Fill remaining slots with top categories by channel count
 */
class GetCategoriesUseCase(
    private val channelRepository: ChannelRepository,
) {
    private val invalidNames = setOf("undefined", "unknown", "other", "")
    private val priorityKeywords = listOf("news", "sport", "music", "general")

    suspend operator fun invoke(
        playlistId: Long? = null,
        categoryLimit: Int = 5,
        channelsPerCategory: Int = 10,
    ): List<Pair<ChannelCategory.Group, List<ChannelEntity>>> {
        val allGroups = if (playlistId != null) {
            channelRepository.getTopGroupsByPlaylist(playlistId, limit = 20).first()
        } else {
            channelRepository.getTopGroups(limit = 20).first()
        }

        val validGroups = allGroups.filter { group ->
            group.name.isNotBlank() && group.name.lowercase() !in invalidNames
        }

        val selectedGroups = selectCategories(validGroups, categoryLimit)

        return selectedGroups.map { group ->
            val category = ChannelCategory.Group(
                id = group.id.toString(),
                displayName = group.name,
                icon = group.icon,
                channelCount = group.channelCount,
                playlistId = group.playlistId,
            )
            val channels = channelRepository.getRandomChannelsByGroupId(
                groupId = group.id,
                limit = channelsPerCategory,
            )
            category to channels
        }
    }

    private fun selectCategories(
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
