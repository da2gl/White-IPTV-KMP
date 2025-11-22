package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.local.PlaylistDao
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity

/**
 * Use case for fetching a single channel by ID
 * Used by PlayerViewModel to get channel details for playback
 */
class GetChannelByIdUseCase(
    private val playlistDao: PlaylistDao,
) {
    suspend operator fun invoke(channelId: Long): ChannelEntity? = playlistDao.getChannelById(channelId)
}
