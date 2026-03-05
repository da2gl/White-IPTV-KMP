package com.simplevideo.whiteiptv.feature.home.mvi

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity

data class ContinueWatchingItem(
    val channel: ChannelEntity,
    val progress: Float,
    val timeLeft: String,
)
