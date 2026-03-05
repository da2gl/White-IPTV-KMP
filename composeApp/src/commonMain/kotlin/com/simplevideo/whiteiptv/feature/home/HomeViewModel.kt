package com.simplevideo.whiteiptv.feature.home

import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection
import com.simplevideo.whiteiptv.domain.model.PlaylistSource
import com.simplevideo.whiteiptv.domain.repository.CurrentPlaylistRepository
import com.simplevideo.whiteiptv.domain.model.ChannelsFilter
import com.simplevideo.whiteiptv.domain.usecase.DeletePlaylistUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetChannelsUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetContinueWatchingUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetFavoritesUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetHomeCategoriesUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetPlaylistsUseCase
import com.simplevideo.whiteiptv.domain.usecase.ImportPlaylistUseCase
import com.simplevideo.whiteiptv.domain.usecase.RenamePlaylistUseCase
import com.simplevideo.whiteiptv.feature.home.mvi.HomeAction
import com.simplevideo.whiteiptv.feature.home.mvi.HomeEvent
import com.simplevideo.whiteiptv.feature.home.mvi.HomeState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
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
) : BaseViewModel<HomeState, HomeAction, HomeEvent>(initialState = HomeState()) {

    private val searchQuery = MutableStateFlow("")

    init {
        currentPlaylistRepository.selection
            .flatMapLatest { selection ->
                combine(
                    getPlaylists(),
                    getContinueWatching(),
                    getFavorites(selection),
                    getHomeCategories(selection),
                ) { playlists, continueWatching, favorites, categories ->
                    viewState.copy(
                        playlists = playlists,
                        selection = selection,
                        continueWatchingItems = continueWatching,
                        favoriteChannels = favorites,
                        categories = categories,
                        isLoading = false,
                    )
                }
            }.catch { e ->
                emit(viewState.copy(error = e.message, isLoading = false))
            }.onEach { state ->
                viewState = state
            }.launchIn(viewModelScope)

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
            viewState = viewState.copy(searchResults = results)
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
                    searchResults = if (!newIsActive) emptyList() else viewState.searchResults,
                )
            }

            is HomeEvent.OnSearchResultClick -> {
                viewAction = HomeAction.NavigateToPlayer(viewEvent.channelId)
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
        return viewState.playlists.find { it.id == selection.id }
    }
}
