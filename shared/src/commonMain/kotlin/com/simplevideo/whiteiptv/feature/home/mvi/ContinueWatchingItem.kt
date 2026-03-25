package com.simplevideo.whiteiptv.feature.home.mvi

import androidx.compose.runtime.Immutable
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity

@Immutable
data class ContinueWatchingItem(
    val channel: ChannelEntity,
    val progress: Float,
    val timeLeft: String,
)
