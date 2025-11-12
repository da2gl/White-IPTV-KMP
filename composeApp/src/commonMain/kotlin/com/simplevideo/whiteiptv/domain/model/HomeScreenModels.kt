package com.simplevideo.whiteiptv.domain.model

data class ContinueWatchingItem(
    val name: String,
    val imageUrl: String,
    val progress: Float,
    val timeLeft: String
)
