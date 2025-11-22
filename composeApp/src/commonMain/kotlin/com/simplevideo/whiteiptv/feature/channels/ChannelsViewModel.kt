package com.simplevideo.whiteiptv.feature.channels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.domain.model.ChannelGroup
import com.simplevideo.whiteiptv.domain.model.ChannelsFilter
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection
import com.simplevideo.whiteiptv.domain.repository.CurrentPlaylistRepository
import com.simplevideo.whiteiptv.domain.usecase.GetChannelsUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetGroupsUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetPlaylistsUseCase
import com.simplevideo.whiteiptv.domain.usecase.ToggleFavoriteUseCase
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsAction
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsEvent
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ChannelsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getPlaylists: GetPlaylistsUseCase,
    private val getGroups: GetGroupsUseCase,
    private val getChannels: GetChannelsUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
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
            getPlaylists(),
            currentPlaylistRepository.selection,
            selectedGroupIdFlow,
        ) { playlists, selection, selectedGroupId ->
            Triple(playlists, selection, selectedGroupId)
        }.flatMapLatest { (playlists, selection, selectedGroupId) ->
            getGroups(selection).map { groups ->
                val selectedGroup = selectedGroupId?.let { id ->
                    groups.find { it.id == id }
                }
                DataState(playlists, selection, groups, selectedGroup)
            }
        }.flatMapLatest { data ->
            val filter = when {
                data.selectedGroup != null -> {
                    val groupId = data.selectedGroup.id.toLongOrNull()
                    if (groupId != null) {
                        ChannelsFilter.ByGroup(groupId)
                    } else {
                        ChannelsFilter.All
                    }
                }

                data.selection is PlaylistSelection.Selected -> {
                    ChannelsFilter.ByPlaylist(data.selection.id)
                }

                else -> ChannelsFilter.All
            }
            getChannels(filter).onEach { channels ->
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
        val playlists: List<com.simplevideo.whiteiptv.data.local.model.PlaylistEntity>,
        val selection: PlaylistSelection,
        val groups: List<ChannelGroup>,
        val selectedGroup: ChannelGroup?,
    )

    override fun obtainEvent(viewEvent: ChannelsEvent) {
        when (viewEvent) {
            is ChannelsEvent.OnPlaylistSelected -> selectPlaylist(viewEvent.selection)
            is ChannelsEvent.OnGroupSelected -> selectGroup(viewEvent.group)
            is ChannelsEvent.OnToggleFavorite -> toggleFavoriteChannel(viewEvent.channelId)
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

    private fun toggleFavoriteChannel(channelId: Long) {
        viewModelScope.launch {
            try {
                toggleFavorite(channelId)
            } catch (e: Exception) {
                viewAction = ChannelsAction.ShowError(e.message ?: "Unknown error")
            }
        }
    }
}
