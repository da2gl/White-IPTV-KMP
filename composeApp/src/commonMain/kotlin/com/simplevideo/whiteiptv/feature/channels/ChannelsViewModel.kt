package com.simplevideo.whiteiptv.feature.channels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.domain.model.ChannelGroup
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import com.simplevideo.whiteiptv.domain.repository.CurrentPlaylistRepository
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsAction
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsEvent
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ChannelsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val channelRepository: ChannelRepository,
    private val playlistRepository: PlaylistRepository,
    private val currentPlaylistRepository: CurrentPlaylistRepository,
) : BaseViewModel<ChannelsState, ChannelsAction, ChannelsEvent>(
    initialState = ChannelsState()
) {
    companion object {
        private const val GROUP_ID_KEY = "groupId"
    }

    private val selectedGroupIdFlow: StateFlow<String?> = savedStateHandle.getStateFlow(
        key = GROUP_ID_KEY,
        initialValue = savedStateHandle[GROUP_ID_KEY],
    )

    init {
        loadData()
    }

    private fun updateSelectedGroupId(groupId: String?) {
        savedStateHandle[GROUP_ID_KEY] = groupId
    }

    private fun loadData() {
        combine(
            playlistRepository.getPlaylists(),
            channelRepository.getAllGroups(),
            currentPlaylistRepository.selection,
            selectedGroupIdFlow,
        ) { playlists, groups, selection, selectedGroupId ->
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

            val selectedGroup = selectedGroupId?.let { id ->
                channelGroups.find { it.id == id }
            }

            DataState(playlists, selection, channelGroups, selectedGroup)
        }.flatMapLatest { data ->
            val channelsFlow = if (data.selectedGroup != null) {
                val groupId = data.selectedGroup.id.toLongOrNull()
                if (groupId != null) {
                    channelRepository.getChannelsByGroupId(groupId)
                } else {
                    channelRepository.getAllChannels()
                }
            } else {
                when (data.selection) {
                    is PlaylistSelection.Selected ->
                        channelRepository.getChannels(data.selection.id)

                    PlaylistSelection.All ->
                        channelRepository.getAllChannels()
                }
            }

            channelsFlow.onEach { channels ->
                viewState = ChannelsState(
                    channels = channels,
                    playlists = data.playlists,
                    selection = data.selection,
                    groups = data.groups,
                    selectedGroup = data.selectedGroup,
                    isLoading = false,
                )
            }
        }.launchIn(viewModelScope)
    }

    private data class DataState(
        val playlists: List<PlaylistEntity>,
        val selection: PlaylistSelection,
        val groups: List<ChannelGroup>,
        val selectedGroup: ChannelGroup?,
    )

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
        updateSelectedGroupId(null)
    }

    private fun selectGroup(group: ChannelGroup?) {
        updateSelectedGroupId(group?.id)
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
