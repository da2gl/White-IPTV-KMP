package com.simplevideo.whiteiptv.feature.channels.mvi

import com.simplevideo.whiteiptv.domain.model.Channel
import com.simplevideo.whiteiptv.domain.model.ChannelCategory

data class ChannelsState(
    val channels: List<Channel> = emptyList(),
    val categories: List<ChannelCategory> = emptyList(),
    val selectedCategory: ChannelCategory? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
