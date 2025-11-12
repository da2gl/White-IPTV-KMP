package com.simplevideo.whiteiptv.feature.home.mvi

import com.simplevideo.whiteiptv.data.parser.playlist.model.Channel
import com.simplevideo.whiteiptv.domain.model.ContinueWatchingItem

data class HomeState(
    val continueWatchingItems: List<ContinueWatchingItem> = emptyList(),
    val favoriteChannels: List<Channel> = emptyList(),
    val sportsChannels: List<Channel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

sealed interface HomeEvent {
    // User interactions will be added here
}

sealed interface HomeAction {
    // One-time actions like navigation will be added here
}
