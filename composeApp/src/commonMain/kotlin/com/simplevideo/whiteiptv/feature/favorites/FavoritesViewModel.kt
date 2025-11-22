package com.simplevideo.whiteiptv.feature.favorites

import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.domain.repository.CurrentPlaylistRepository
import com.simplevideo.whiteiptv.domain.usecase.GetFavoritesUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetPlaylistsUseCase
import com.simplevideo.whiteiptv.domain.usecase.ToggleFavoriteUseCase
import com.simplevideo.whiteiptv.feature.favorites.mvi.FavoritesAction
import com.simplevideo.whiteiptv.feature.favorites.mvi.FavoritesEvent
import com.simplevideo.whiteiptv.feature.favorites.mvi.FavoritesState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FavoritesViewModel(
    getPlaylists: GetPlaylistsUseCase,
    getFavorites: GetFavoritesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val currentPlaylistRepository: CurrentPlaylistRepository,
) : BaseViewModel<FavoritesState, FavoritesAction, FavoritesEvent>(
    initialState = FavoritesState(),
) {

    private val searchQuery = MutableStateFlow("")

    init {
        @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
        combine(
            currentPlaylistRepository.selection,
            searchQuery.debounce(300),
        ) { selection, query ->
            selection to query
        }.flatMapLatest { (selection, query) ->
            combine(
                getPlaylists(),
                getFavorites(selection),
            ) { playlists, channels ->
                val filteredChannels = filterChannels(channels, query)
                viewState.copy(
                    playlists = playlists,
                    channels = filteredChannels,
                    selection = selection,
                    searchQuery = query,
                    isLoading = false,
                    error = null,
                )
            }
        }.catch { e ->
            emit(viewState.copy(error = e.message, isLoading = false))
        }.onEach { state ->
            viewState = state
        }.launchIn(viewModelScope)
    }

    private fun filterChannels(channels: List<ChannelEntity>, query: String): List<ChannelEntity> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) return channels
        return channels.filter { channel ->
            channel.name.contains(normalizedQuery, ignoreCase = true)
        }
    }

    override fun obtainEvent(viewEvent: FavoritesEvent) {
        when (viewEvent) {
            is FavoritesEvent.OnPlaylistSelected -> {
                currentPlaylistRepository.select(viewEvent.selection)
            }

            is FavoritesEvent.OnToggleFavorite -> {
                viewModelScope.launch {
                    toggleFavorite(viewEvent.channelId)
                }
            }

            is FavoritesEvent.OnChannelClick -> {
                viewAction = FavoritesAction.NavigateToPlayer(viewEvent.channelId)
            }

            is FavoritesEvent.OnSearchQueryChanged -> {
                searchQuery.value = viewEvent.query
                viewState = viewState.copy(searchQuery = viewEvent.query)
            }

            is FavoritesEvent.OnToggleSearch -> {
                val newIsActive = !viewState.isSearchActive
                if (!newIsActive) {
                    searchQuery.value = ""
                }
                viewState = viewState.copy(
                    isSearchActive = newIsActive,
                    searchQuery = if (!newIsActive) "" else viewState.searchQuery,
                )
            }
        }
    }
}
