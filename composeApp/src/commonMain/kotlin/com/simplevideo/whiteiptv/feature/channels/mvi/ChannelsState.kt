package com.simplevideo.whiteiptv.feature.channels.mvi

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.domain.model.ChannelCategory

data class ChannelsState(
    val channels: List<ChannelEntity> = emptyList(),
    val playlists: List<PlaylistEntity> = emptyList(),
    val selectedPlaylistId: Long? = null,
    val categories: List<ChannelCategory> = emptyList(),
    val selectedCategory: ChannelCategory = ChannelCategory.All,
    val isLoading: Boolean = true,
    val error: String? = null,
)
