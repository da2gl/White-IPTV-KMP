package com.simplevideo.whiteiptv.feature.channels.mvi

import com.simplevideo.whiteiptv.domain.repository.FIXMEChannel
import com.simplevideo.whiteiptv.domain.repository.FIXMEChannelCategory

data class ChannelsState(
    val FIXMEChannels: List<FIXMEChannel> = emptyList(),
    val categories: List<FIXMEChannelCategory> = emptyList(),
    val selectedCategory: FIXMEChannelCategory? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
