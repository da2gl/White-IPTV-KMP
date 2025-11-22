package com.simplevideo.whiteiptv.feature.favorites.mvi

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection

data class FavoritesState(
    val channels: List<ChannelEntity> = emptyList(),
    val playlists: List<PlaylistEntity> = emptyList(),
    val selection: PlaylistSelection = PlaylistSelection.All,
    val isLoading: Boolean = true,
    val error: String? = null,
)

sealed interface FavoritesEvent {
    data class OnPlaylistSelected(val selection: PlaylistSelection) : FavoritesEvent
    data class OnToggleFavorite(val channelId: Long) : FavoritesEvent
    data class OnChannelClick(val channelId: Long) : FavoritesEvent
}

sealed interface FavoritesAction {
    data class NavigateToPlayer(val channelId: Long) : FavoritesAction
}
