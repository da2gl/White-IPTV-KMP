package com.simplevideo.whiteiptv.domain.repository

import com.simplevideo.whiteiptv.domain.model.Channel
import com.simplevideo.whiteiptv.domain.model.ChannelCategory

interface ChannelsRepository {
    suspend fun getChannels(): List<Channel>
    suspend fun getChannelCategories(): List<ChannelCategory>
    suspend fun toggleFavoriteStatus(channelId: String): Channel
}
