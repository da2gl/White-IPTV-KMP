package com.simplevideo.whiteiptv.feature.favorites.mvi

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity

data class FavoritesState(
    val channels: List<ChannelEntity> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val isLoading: Boolean = true
)
