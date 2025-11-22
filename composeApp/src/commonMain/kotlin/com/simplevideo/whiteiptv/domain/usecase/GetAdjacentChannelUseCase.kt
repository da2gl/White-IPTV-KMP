package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.local.PlaylistDao
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity

/**
 * Use case for getting adjacent (next/previous) channel within the same playlist
 *
 * Used for channel navigation in the player screen via swipe gestures
 */
class GetAdjacentChannelUseCase(
    private val playlistDao: PlaylistDao,
) {
    suspend fun getNext(playlistId: Long, currentChannelId: Long): ChannelEntity? =
        playlistDao.getNextChannel(playlistId, currentChannelId)

    suspend fun getPrevious(playlistId: Long, currentChannelId: Long): ChannelEntity? =
        playlistDao.getPreviousChannel(playlistId, currentChannelId)
}
