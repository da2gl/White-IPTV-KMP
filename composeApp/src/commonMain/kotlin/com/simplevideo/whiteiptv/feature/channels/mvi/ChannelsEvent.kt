package com.simplevideo.whiteiptv.feature.channels.mvi

import com.simplevideo.whiteiptv.domain.model.ChannelCategory

sealed interface ChannelsEvent {
    data class OnPlaylistSelected(val playlistId: Long?) : ChannelsEvent
    data class OnCategorySelected(val category: ChannelCategory) : ChannelsEvent
    data class OnToggleFavorite(val channelId: Long) : ChannelsEvent
}
