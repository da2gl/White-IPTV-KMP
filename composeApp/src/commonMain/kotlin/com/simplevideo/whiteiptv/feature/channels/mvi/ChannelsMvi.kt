package com.simplevideo.whiteiptv.feature.channels.mvi

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.domain.model.ChannelGroup
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection

data class ChannelsState(
    val channels: List<ChannelEntity> = emptyList(),
    val playlists: List<PlaylistEntity> = emptyList(),
    val selection: PlaylistSelection = PlaylistSelection.All,
    val groups: List<ChannelGroup> = emptyList(),
    val selectedGroup: ChannelGroup? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

sealed interface ChannelsEvent {
    data class OnPlaylistSelected(val selection: PlaylistSelection) : ChannelsEvent
    data class OnGroupSelected(val group: ChannelGroup?) : ChannelsEvent
    data class OnToggleFavorite(val channelId: Long) : ChannelsEvent
    data class OnChannelClick(val channelId: Long) : ChannelsEvent
}

sealed interface ChannelsAction {
    data class ShowError(val message: String) : ChannelsAction
    data class NavigateToPlayer(val channelId: Long) : ChannelsAction
}
