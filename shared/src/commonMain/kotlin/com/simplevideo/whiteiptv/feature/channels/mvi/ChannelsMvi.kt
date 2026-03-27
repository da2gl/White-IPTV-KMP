package com.simplevideo.whiteiptv.feature.channels.mvi

import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.domain.model.ChannelGroup
import com.simplevideo.whiteiptv.domain.model.ChannelViewMode
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection

data class ChannelsState(
    val playlists: List<PlaylistEntity> = emptyList(),
    val selection: PlaylistSelection = PlaylistSelection.All,
    val groups: List<ChannelGroup> = emptyList(),
    val selectedGroup: ChannelGroup? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val channelViewMode: ChannelViewMode = ChannelViewMode.List,
)

sealed interface ChannelsEvent {
    data class OnPlaylistSelected(val selection: PlaylistSelection) : ChannelsEvent
    data class OnGroupSelected(val group: ChannelGroup?) : ChannelsEvent
    data class OnToggleFavorite(val channelId: Long) : ChannelsEvent
    data class OnChannelClick(val channelId: Long) : ChannelsEvent
    data class OnSearchQueryChanged(val query: String) : ChannelsEvent
    data object OnToggleSearch : ChannelsEvent
    data class OnDeleteChannel(val channelId: Long) : ChannelsEvent
    data class OnRenameChannel(val channelId: Long, val newName: String) : ChannelsEvent
}

sealed interface ChannelsAction {
    data class ShowError(val message: String) : ChannelsAction
    data class NavigateToPlayer(val channelId: Long) : ChannelsAction
}
