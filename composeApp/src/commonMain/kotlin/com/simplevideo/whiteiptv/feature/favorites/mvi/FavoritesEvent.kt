package com.simplevideo.whiteiptv.feature.favorites.mvi

sealed interface FavoritesEvent {
    data class OnCategorySelected(val category: String) : FavoritesEvent
    data class OnToggleFavorite(val channelId: Long) : FavoritesEvent
}
