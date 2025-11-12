package com.simplevideo.whiteiptv.feature.channels.mvi

import com.simplevideo.whiteiptv.domain.repository.FIXMEChannelCategory

sealed interface ChannelsEvent {
    data class OnCategorySelected(val category: FIXMEChannelCategory) : ChannelsEvent
    data class OnToggleFavorite(val channelId: String) : ChannelsEvent
}
