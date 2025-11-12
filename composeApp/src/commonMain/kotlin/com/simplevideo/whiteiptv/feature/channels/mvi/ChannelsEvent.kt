package com.simplevideo.whiteiptv.feature.channels.mvi

import com.simplevideo.whiteiptv.domain.repository.ChannelCategory

sealed interface ChannelsEvent {
    data class OnCategorySelected(val category: ChannelCategory) : ChannelsEvent
    data class OnToggleFavorite(val channelId: String) : ChannelsEvent
}
