package com.simplevideo.whiteiptv.feature.channels

import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.domain.model.ChannelGroup
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import com.simplevideo.whiteiptv.domain.repository.CurrentPlaylistRepository
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsAction
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsEvent
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChannelsViewModel(
    private val channelRepository: ChannelRepository,
    private val playlistRepository: PlaylistRepository,
    private val currentPlaylistRepository: CurrentPlaylistRepository,
) : BaseViewModel<ChannelsState, ChannelsAction, ChannelsEvent>(
    initialState = ChannelsState()
) {
    private var initialGroupId: String? = null
    private var isInitialized = false

    fun setInitialGroup(groupId: String?) {
        if (!isInitialized) {
            initialGroupId = groupId
            isInitialized = true
            loadData()
        }
    }

    private fun loadData() {
        combine(
            playlistRepository.getPlaylists(),
            channelRepository.getAllGroups(),
            currentPlaylistRepository.selection,
        ) { playlists, groups, selection ->
            val filteredGroups = when (selection) {
                is PlaylistSelection.Selected ->
                    groups.filter { it.playlistId == selection.id }

                PlaylistSelection.All -> groups
            }

            val channelGroups = filteredGroups.map { group ->
                ChannelGroup(
                    id = group.id.toString(),
                    displayName = group.name,
                    icon = group.icon,
                    channelCount = group.channelCount,
                    playlistId = group.playlistId,
                )
            }

            val selectedGroup = initialGroupId?.let { id ->
                channelGroups.find { it.id == id }
            }

            Triple(
                playlists to selection,
                channelGroups,
                selectedGroup,
            )
        }.onEach { (playlistData, groups, selectedGroup) ->
            val (playlists, selection) = playlistData
            viewState = viewState.copy(
                playlists = playlists,
                selection = selection,
                groups = groups,
            )
            selectGroup(selectedGroup)
        }.launchIn(viewModelScope)
    }

    override fun obtainEvent(viewEvent: ChannelsEvent) {
        when (viewEvent) {
            is ChannelsEvent.OnPlaylistSelected -> selectPlaylist(viewEvent.selection)
            is ChannelsEvent.OnGroupSelected -> selectGroup(viewEvent.group)
            is ChannelsEvent.OnToggleFavorite -> toggleFavorite(viewEvent.channelId)
            is ChannelsEvent.OnChannelClick -> {
                viewAction = ChannelsAction.NavigateToPlayer(viewEvent.channelId)
            }
        }
    }

    private fun selectPlaylist(selection: PlaylistSelection) {
        currentPlaylistRepository.select(selection)
        viewState = viewState.copy(
            selectedGroup = null,
            isLoading = true,
        )
    }

    private fun selectGroup(group: ChannelGroup?) {
        viewState = viewState.copy(
            selectedGroup = group,
            isLoading = true,
        )

        val selection = viewState.selection

        val channelsFlow = if (group != null) {
            val groupId = group.id.toLongOrNull()
            if (groupId != null) {
                channelRepository.getChannelsByGroupId(groupId)
            } else {
                channelRepository.getAllChannels()
            }
        } else {
            when (selection) {
                is PlaylistSelection.Selected ->
                    channelRepository.getChannels(selection.id)

                PlaylistSelection.All ->
                    channelRepository.getAllChannels()
            }
        }

        channelsFlow.onEach { channels ->
            viewState = viewState.copy(
                channels = channels,
                selectedGroup = group,
                isLoading = false,
            )
        }.launchIn(viewModelScope)
    }

    private fun toggleFavorite(channelId: Long) {
        viewModelScope.launch {
            try {
                channelRepository.toggleFavoriteStatus(channelId)
            } catch (e: Exception) {
                viewAction = ChannelsAction.ShowError(e.message ?: "Unknown error")
            }
        }
    }
}
