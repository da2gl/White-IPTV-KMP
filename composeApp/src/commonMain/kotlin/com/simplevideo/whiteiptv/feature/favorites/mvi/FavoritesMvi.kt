package com.simplevideo.whiteiptv.feature.favorites.mvi

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity

data class FavoritesState(
    val channels: List<ChannelEntity> = emptyList(),
    val playlists: List<PlaylistEntity> = emptyList(),
    val selectedPlaylistId: Long? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

sealed interface FavoritesEvent {
    data class OnPlaylistSelected(val playlistId: Long?) : FavoritesEvent
    data class OnToggleFavorite(val channelId: Long) : FavoritesEvent
    data class OnChannelClick(val channelId: Long) : FavoritesEvent
}

sealed interface FavoritesAction {
    data class NavigateToPlayer(val channelId: Long) : FavoritesAction
}
