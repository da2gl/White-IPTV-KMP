package com.simplevideo.whiteiptv.feature.home.mvi

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.domain.model.ChannelGroup
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection

data class HomeState(
    val playlists: List<PlaylistEntity> = emptyList(),
    val selection: PlaylistSelection = PlaylistSelection.All,
    val continueWatchingItems: List<ContinueWatchingItem> = emptyList(),
    val favoriteChannels: List<ChannelEntity> = emptyList(),
    val categories: List<Pair<ChannelGroup, List<ChannelEntity>>> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

sealed interface HomeEvent {
    data class OnPlaylistSelected(val selection: PlaylistSelection) : HomeEvent
    data object OnFavoritesViewAllClick : HomeEvent
    data class OnGroupViewAllClick(val groupId: String) : HomeEvent
    data class OnChannelClick(val channelId: Long) : HomeEvent
}

sealed interface HomeAction {
    data object NavigateToFavorites : HomeAction
    data class NavigateToChannels(val groupId: String?) : HomeAction
    data class NavigateToPlayer(val channelId: Long) : HomeAction
}
