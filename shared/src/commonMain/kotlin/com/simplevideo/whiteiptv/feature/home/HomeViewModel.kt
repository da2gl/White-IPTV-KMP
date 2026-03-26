package com.simplevideo.whiteiptv.feature.home

import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.domain.model.ChannelsFilter
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection
import com.simplevideo.whiteiptv.domain.model.PlaylistSource
import com.simplevideo.whiteiptv.domain.repository.CurrentPlaylistRepository
import com.simplevideo.whiteiptv.domain.usecase.DeletePlaylistUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetChannelsUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetContinueWatchingUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetFavoritesUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetHomeCategoriesUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetPlaylistsUseCase
import com.simplevideo.whiteiptv.domain.usecase.ImportPlaylistUseCase
import com.simplevideo.whiteiptv.domain.usecase.RenamePlaylistUseCase
import com.simplevideo.whiteiptv.domain.usecase.ToggleFavoriteUseCase
import com.simplevideo.whiteiptv.feature.home.mvi.CategoryItem
import com.simplevideo.whiteiptv.feature.home.mvi.ContinueWatchingItem
import com.simplevideo.whiteiptv.feature.home.mvi.HomeAction
import com.simplevideo.whiteiptv.feature.home.mvi.HomeEvent
import com.simplevideo.whiteiptv.feature.home.mvi.HomeState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class HomeViewModel(
    getPlaylists: GetPlaylistsUseCase,
    getContinueWatching: GetContinueWatchingUseCase,
    getFavorites: GetFavoritesUseCase,
    getHomeCategories: GetHomeCategoriesUseCase,
    private val getChannels: GetChannelsUseCase,
    private val currentPlaylistRepository: CurrentPlaylistRepository,
    private val renamePlaylistUseCase: RenamePlaylistUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase,
    private val importPlaylistUseCase: ImportPlaylistUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : BaseViewModel<HomeState, HomeAction, HomeEvent>(initialState = HomeState()) {

    private val searchQuery = MutableStateFlow("")

    val playlists: StateFlow<ImmutableList<PlaylistEntity>> =
        getPlaylists()
            .map { it.toImmutableList() }
            .catch { emit(persistentListOf()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), persistentListOf())

    val continueWatchingItems: StateFlow<ImmutableList<ContinueWatchingItem>> =
        getContinueWatching()
            .map { it.toImmutableList() }
            .catch { emit(persistentListOf()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), persistentListOf())

    val favoriteChannels: StateFlow<ImmutableList<ChannelEntity>> =
        currentPlaylistRepository.selection
            .flatMapLatest { selection -> getFavorites(selection) }
            .map { it.toImmutableList() }
            .catch { emit(persistentListOf()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), persistentListOf())

    val categories: StateFlow<ImmutableList<CategoryItem>> =
        currentPlaylistRepository.selection
            .flatMapLatest { selection -> getHomeCategories(selection) }
            .map { pairs ->
                pairs
                    .filter { (_, channels) -> channels.isNotEmpty() }
                    .map { (group, channels) -> CategoryItem(group, channels.toImmutableList()) }
                    .toImmutableList()
            }
            .catch { emit(persistentListOf()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), persistentListOf())

    init {
        currentPlaylistRepository.selection
            .onEach { selection ->
                viewState = viewState.copy(selection = selection, isLoading = false)
            }
            .launchIn(viewModelScope)

        combine(
            searchQuery.debounce(300),
            currentPlaylistRepository.selection,
        ) { query, selection ->
            query to selection
        }.flatMapLatest { (query, selection) ->
            val trimmedQuery = query.trim()
            if (trimmedQuery.isEmpty()) {
                flowOf(emptyList())
            } else {
                val filter = when (selection) {
                    is PlaylistSelection.Selected -> ChannelsFilter.ByPlaylist(selection.id)
                    is PlaylistSelection.All -> ChannelsFilter.All
                }
                getChannels(filter, trimmedQuery)
            }
        }.onEach { results ->
            viewState = viewState.copy(searchResults = results.toImmutableList())
        }.launchIn(viewModelScope)
    }

    override fun obtainEvent(viewEvent: HomeEvent) {
        when (viewEvent) {
            is HomeEvent.OnPlaylistSelected -> {
                currentPlaylistRepository.select(viewEvent.selection)
            }

            is HomeEvent.OnFavoritesViewAllClick -> {
                viewAction = HomeAction.NavigateToFavorites
            }

            is HomeEvent.OnGroupViewAllClick -> {
                viewAction = HomeAction.NavigateToChannels(viewEvent.groupId)
            }

            is HomeEvent.OnChannelClick -> {
                viewAction = HomeAction.NavigateToPlayer(viewEvent.channelId)
            }

            is HomeEvent.OnPlaylistSettingsClick -> {
                viewState = viewState.copy(showPlaylistSettings = true)
            }

            is HomeEvent.OnPlaylistSettingsDismiss -> {
                viewState = viewState.copy(showPlaylistSettings = false)
            }

            is HomeEvent.OnRenameClick -> {
                viewState = viewState.copy(showPlaylistSettings = false, showRenameDialog = true)
            }

            is HomeEvent.OnRenameDialogDismiss -> {
                viewState = viewState.copy(showRenameDialog = false)
            }

            is HomeEvent.OnRenameConfirm -> handleRename(viewEvent.newName)

            is HomeEvent.OnUpdatePlaylistClick -> handleUpdatePlaylist()

            is HomeEvent.OnDeleteClick -> {
                viewState = viewState.copy(showPlaylistSettings = false, showDeleteConfirmation = true)
            }

            is HomeEvent.OnDeleteDialogDismiss -> {
                viewState = viewState.copy(showDeleteConfirmation = false)
            }

            is HomeEvent.OnDeleteConfirm -> handleDelete()

            is HomeEvent.OnViewUrlClick -> {
                viewState = viewState.copy(showPlaylistSettings = false, showViewUrlDialog = true)
            }

            is HomeEvent.OnViewUrlDialogDismiss -> {
                viewState = viewState.copy(showViewUrlDialog = false)
            }

            is HomeEvent.OnPlaylistManagementErrorDismiss -> {
                viewState = viewState.copy(playlistManagementError = null)
            }

            is HomeEvent.OnSearchQueryChanged -> {
                searchQuery.value = viewEvent.query
                viewState = viewState.copy(searchQuery = viewEvent.query)
            }

            is HomeEvent.OnToggleSearch -> {
                val newIsActive = !viewState.isSearchActive
                if (!newIsActive) searchQuery.value = ""
                viewState = viewState.copy(
                    isSearchActive = newIsActive,
                    searchQuery = if (!newIsActive) "" else viewState.searchQuery,
                    searchResults = if (!newIsActive) persistentListOf() else viewState.searchResults,
                )
            }

            is HomeEvent.OnSearchResultClick -> {
                viewAction = HomeAction.NavigateToPlayer(viewEvent.channelId)
            }

            is HomeEvent.OnAddPlaylistClick -> {
                viewAction = HomeAction.NavigateToOnboarding
            }

            is HomeEvent.OnToggleFavorite -> {
                viewModelScope.launch { toggleFavorite(viewEvent.channelId) }
            }
        }
    }

    private fun handleRename(newName: String) {
        val playlist = getSelectedPlaylist() ?: return
        viewModelScope.launch {
            runCatching {
                renamePlaylistUseCase(playlist.id, newName)
            }.onSuccess {
                viewState = viewState.copy(showRenameDialog = false)
            }.onFailure { e ->
                viewState = viewState.copy(
                    showRenameDialog = false,
                    playlistManagementError = e.message ?: "Failed to rename playlist",
                )
            }
        }
    }

    private fun handleUpdatePlaylist() {
        val playlist = getSelectedPlaylist() ?: return
        viewState = viewState.copy(showPlaylistSettings = false, isUpdatingPlaylist = true)
        viewModelScope.launch {
            runCatching {
                importPlaylistUseCase(PlaylistSource.Url(playlist.url))
            }.onSuccess {
                viewState = viewState.copy(isUpdatingPlaylist = false)
            }.onFailure { e ->
                viewState = viewState.copy(
                    isUpdatingPlaylist = false,
                    playlistManagementError = e.message ?: "Failed to update playlist",
                )
            }
        }
    }

    private fun handleDelete() {
        val playlist = getSelectedPlaylist() ?: return
        viewState = viewState.copy(showDeleteConfirmation = false)
        viewModelScope.launch {
            runCatching {
                deletePlaylistUseCase(playlist.id)
            }.onSuccess { wasLastPlaylist ->
                if (wasLastPlaylist) {
                    viewAction = HomeAction.NavigateToOnboarding
                } else {
                    currentPlaylistRepository.select(PlaylistSelection.All)
                }
            }.onFailure { e ->
                viewState = viewState.copy(
                    playlistManagementError = e.message ?: "Failed to delete playlist",
                )
            }
        }
    }

    private fun getSelectedPlaylist(): PlaylistEntity? {
        val selection = viewState.selection
        if (selection !is PlaylistSelection.Selected) return null
        return playlists.value.find { it.id == selection.id }
    }
}
