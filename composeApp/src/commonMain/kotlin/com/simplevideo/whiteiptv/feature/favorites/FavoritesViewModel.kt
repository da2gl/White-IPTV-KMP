package com.simplevideo.whiteiptv.feature.favorites

import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.domain.repository.CurrentPlaylistRepository
import com.simplevideo.whiteiptv.domain.usecase.GetFavoritesUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetPlaylistsUseCase
import com.simplevideo.whiteiptv.domain.usecase.ToggleFavoriteUseCase
import com.simplevideo.whiteiptv.feature.favorites.mvi.FavoritesAction
import com.simplevideo.whiteiptv.feature.favorites.mvi.FavoritesEvent
import com.simplevideo.whiteiptv.feature.favorites.mvi.FavoritesState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModel(
    getPlaylists: GetPlaylistsUseCase,
    private val getFavorites: GetFavoritesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val currentPlaylistRepository: CurrentPlaylistRepository,
) : BaseViewModel<FavoritesState, FavoritesAction, FavoritesEvent>(
    initialState = FavoritesState(),
) {

    init {
        currentPlaylistRepository.selectedPlaylistId
            .flatMapLatest { selectedId ->
                combine(
                    getPlaylists(),
                    getFavorites(selectedId),
                ) { playlists, channels ->
                    viewState.copy(
                        playlists = playlists,
                        channels = channels,
                        selectedPlaylistId = selectedId,
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

    override fun obtainEvent(viewEvent: FavoritesEvent) {
        when (viewEvent) {
            is FavoritesEvent.OnPlaylistSelected -> {
                currentPlaylistRepository.selectPlaylist(viewEvent.playlistId)
            }

            is FavoritesEvent.OnToggleFavorite -> {
                viewModelScope.launch {
                    toggleFavorite(viewEvent.channelId)
                }
            }

            is FavoritesEvent.OnChannelClick -> {
                viewAction = FavoritesAction.NavigateToPlayer(viewEvent.channelId)
            }
        }
    }
}
