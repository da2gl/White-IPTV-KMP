package com.simplevideo.whiteiptv.feature.channels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.data.local.SettingsPreferences
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.domain.model.ChannelGroup
import com.simplevideo.whiteiptv.domain.model.ChannelsFilter
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection
import com.simplevideo.whiteiptv.domain.repository.CurrentPlaylistRepository
import com.simplevideo.whiteiptv.domain.usecase.DeleteChannelUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetGroupsUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetPagedChannelsUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetPlaylistsUseCase
import com.simplevideo.whiteiptv.domain.usecase.RenameChannelUseCase
import com.simplevideo.whiteiptv.domain.usecase.ToggleFavoriteUseCase
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsAction
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsEvent
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ChannelsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getPlaylists: GetPlaylistsUseCase,
    private val getGroups: GetGroupsUseCase,
    private val getPagedChannels: GetPagedChannelsUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val deleteChannel: DeleteChannelUseCase,
    private val renameChannel: RenameChannelUseCase,
    private val currentPlaylistRepository: CurrentPlaylistRepository,
    private val settingsPreferences: SettingsPreferences,
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

    private val searchQuery = MutableStateFlow("")

    val pagedChannels: Flow<PagingData<ChannelEntity>> = combine(
        currentPlaylistRepository.selection,
        selectedGroupIdFlow,
        searchQuery.debounce(300),
    ) { selection, selectedGroupId, query ->
        Triple(selection, selectedGroupId, query)
    }.flatMapLatest { (selection, selectedGroupId, query) ->
        val filter = resolveFilter(selection, selectedGroupId)
        getPagedChannels(filter, query)
    }.cachedIn(viewModelScope)
        .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    init {
        loadData()
        observeViewMode()
    }

    private fun observeViewMode() {
        settingsPreferences.channelViewModeFlow
            .onEach { mode ->
                viewState = viewState.copy(channelViewMode = mode)
            }
            .launchIn(viewModelScope)
    }

    private fun updateSelectedGroupId(groupId: String?) {
        savedStateHandle[GROUP_ID_KEY] = groupId
    }

    private fun resolveFilter(selection: PlaylistSelection, selectedGroupId: String?): ChannelsFilter {
        if (selectedGroupId != null) {
            val groupId = selectedGroupId.toLongOrNull()
            if (groupId != null) {
                return ChannelsFilter.ByGroup(groupId)
            }
        }
        if (selection is PlaylistSelection.Selected) {
            return ChannelsFilter.ByPlaylist(selection.id)
        }
        return ChannelsFilter.All
    }

    private fun loadData() {
        combine(
            getPlaylists().distinctUntilChanged(),
            currentPlaylistRepository.selection,
            selectedGroupIdFlow,
            searchQuery.debounce(300),
        ) { playlists, selection, selectedGroupId, query ->
            LoadParams(playlists, selection, selectedGroupId, query)
        }.flatMapLatest { params ->
            getGroups(params.selection).map { groups ->
                val selectedGroup = params.selectedGroupId?.let { id ->
                    groups.find { it.id == id }
                }
                DataState(params.playlists, params.selection, groups, selectedGroup, params.query)
            }
        }.onEach { data ->
            viewState = viewState.copy(
                playlists = data.playlists,
                selection = data.selection,
                groups = data.groups,
                selectedGroup = data.selectedGroup,
                searchQuery = data.query,
                isLoading = false,
            )
        }.launchIn(viewModelScope)
    }

    private data class LoadParams(
        val playlists: List<com.simplevideo.whiteiptv.data.local.model.PlaylistEntity>,
        val selection: PlaylistSelection,
        val selectedGroupId: String?,
        val query: String,
    )

    private data class DataState(
        val playlists: List<com.simplevideo.whiteiptv.data.local.model.PlaylistEntity>,
        val selection: PlaylistSelection,
        val groups: List<ChannelGroup>,
        val selectedGroup: ChannelGroup?,
        val query: String,
    )

    override fun obtainEvent(viewEvent: ChannelsEvent) {
        when (viewEvent) {
            is ChannelsEvent.OnPlaylistSelected -> selectPlaylist(viewEvent.selection)
            is ChannelsEvent.OnGroupSelected -> selectGroup(viewEvent.group)
            is ChannelsEvent.OnToggleFavorite -> toggleFavoriteChannel(viewEvent.channelId)
            is ChannelsEvent.OnChannelClick -> {
                viewAction = ChannelsAction.NavigateToPlayer(viewEvent.channelId)
            }
            is ChannelsEvent.OnSearchQueryChanged -> {
                searchQuery.value = viewEvent.query
                viewState = viewState.copy(searchQuery = viewEvent.query)
            }
            is ChannelsEvent.OnDeleteChannel -> deleteChannelById(viewEvent.channelId)
            is ChannelsEvent.OnRenameChannel -> renameChannelById(viewEvent.channelId, viewEvent.newName)
            is ChannelsEvent.OnToggleSearch -> {
                val newIsActive = !viewState.isSearchActive
                if (!newIsActive) searchQuery.value = ""
                viewState = viewState.copy(
                    isSearchActive = newIsActive,
                    searchQuery = if (!newIsActive) "" else viewState.searchQuery,
                )
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

    private fun renameChannelById(channelId: Long, newName: String) {
        viewModelScope.launch {
            try {
                renameChannel(channelId, newName)
            } catch (e: Exception) {
                viewAction = ChannelsAction.ShowError(e.message ?: "Failed to rename channel")
            }
        }
    }

    private fun deleteChannelById(channelId: Long) {
        viewModelScope.launch {
            try {
                deleteChannel(channelId)
            } catch (e: Exception) {
                viewAction = ChannelsAction.ShowError(e.message ?: "Failed to delete channel")
            }
        }
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
