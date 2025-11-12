package com.simplevideo.whiteiptv.feature.home.mvi

import com.simplevideo.whiteiptv.domain.model.ContinueWatchingItem
import com.simplevideo.whiteiptv.domain.repository.FIXMEChannel

data class HomeState(
    val continueWatchingItems: List<ContinueWatchingItem> = emptyList(),
    val favoriteFIXMEChannels: List<FIXMEChannel> = emptyList(),
    val sportsFIXMEChannels: List<FIXMEChannel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

sealed interface HomeEvent {
    // User interactions will be added here
}

sealed interface HomeAction {
    // One-time actions like navigation will be added here
}
