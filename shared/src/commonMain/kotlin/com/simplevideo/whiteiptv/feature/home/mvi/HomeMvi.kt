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
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val searchResults: List<ChannelEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showPlaylistSettings: Boolean = false,
    val showRenameDialog: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val showViewUrlDialog: Boolean = false,
    val isUpdatingPlaylist: Boolean = false,
    val playlistManagementError: String? = null,
)

sealed interface HomeEvent {
    data class OnPlaylistSelected(val selection: PlaylistSelection) : HomeEvent
    data object OnFavoritesViewAllClick : HomeEvent
    data class OnGroupViewAllClick(val groupId: String) : HomeEvent
    data class OnChannelClick(val channelId: Long) : HomeEvent
    data object OnPlaylistSettingsClick : HomeEvent
    data object OnPlaylistSettingsDismiss : HomeEvent
    data object OnRenameClick : HomeEvent
    data object OnRenameDialogDismiss : HomeEvent
    data class OnRenameConfirm(val newName: String) : HomeEvent
    data object OnUpdatePlaylistClick : HomeEvent
    data object OnDeleteClick : HomeEvent
    data object OnDeleteDialogDismiss : HomeEvent
    data object OnDeleteConfirm : HomeEvent
    data object OnViewUrlClick : HomeEvent
    data object OnViewUrlDialogDismiss : HomeEvent
    data object OnPlaylistManagementErrorDismiss : HomeEvent
    data class OnSearchQueryChanged(val query: String) : HomeEvent
    data object OnToggleSearch : HomeEvent
    data class OnSearchResultClick(val channelId: Long) : HomeEvent
    data object OnAddPlaylistClick : HomeEvent
    data class OnToggleFavorite(val channelId: Long) : HomeEvent
}

sealed interface HomeAction {
    data object NavigateToFavorites : HomeAction
    data class NavigateToChannels(val groupId: String?) : HomeAction
    data class NavigateToPlayer(val channelId: Long) : HomeAction
    data object NavigateToOnboarding : HomeAction
}
