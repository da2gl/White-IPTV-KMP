package com.simplevideo.whiteiptv.domain.repository

interface ChannelsRepository {
    suspend fun getChannels(): List<FIXMEChannel>
    suspend fun getChannelCategories(): List<FIXMEChannelCategory>
    suspend fun toggleFavoriteStatus(channelId: String): FIXMEChannel
}

class FIXMEChannel
class FIXMEChannelCategory
