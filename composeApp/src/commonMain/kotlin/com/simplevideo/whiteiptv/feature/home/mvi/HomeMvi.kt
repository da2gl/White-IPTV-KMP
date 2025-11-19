package com.simplevideo.whiteiptv.feature.home.mvi

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.domain.model.ChannelCategory

data class HomeState(
    val playlists: List<PlaylistEntity> = emptyList(),
    val selectedPlaylistId: Long? = null,
    val continueWatchingItems: List<ContinueWatchingItem> = emptyList(),
    val favoriteChannels: List<ChannelEntity> = emptyList(),
    val categories: List<Pair<ChannelCategory.Group, List<ChannelEntity>>> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

sealed interface HomeEvent {
    data class OnPlaylistSelected(val playlistId: Long?) : HomeEvent
}

sealed interface HomeAction {
    // One-time actions like navigation will be added here
}
