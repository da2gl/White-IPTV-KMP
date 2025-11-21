package com.simplevideo.whiteiptv.feature.home

import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import com.simplevideo.whiteiptv.domain.repository.CurrentPlaylistRepository
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import com.simplevideo.whiteiptv.domain.usecase.GetCategoriesUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetContinueWatchingUseCase
import com.simplevideo.whiteiptv.feature.home.mvi.HomeAction
import com.simplevideo.whiteiptv.feature.home.mvi.HomeEvent
import com.simplevideo.whiteiptv.feature.home.mvi.HomeState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    channelRepository: ChannelRepository,
    playlistRepository: PlaylistRepository,
    getContinueWatching: GetContinueWatchingUseCase,
    getCategories: GetCategoriesUseCase,
    private val currentPlaylistRepository: CurrentPlaylistRepository,
) : BaseViewModel<HomeState, HomeAction, HomeEvent>(initialState = HomeState()) {

    init {
        currentPlaylistRepository.selectedPlaylistId
            .flatMapLatest { selectedId ->
                combine(
                    playlistRepository.getPlaylists(),
                    getContinueWatching(),
                    channelRepository.getFavoriteChannels(),
                    getCategories(playlistId = selectedId),
                ) { playlists, continueWatching, favorites, categories ->
                    HomeState(
                        playlists = playlists,
                        selectedPlaylistId = selectedId,
                        continueWatchingItems = continueWatching,
                        favoriteChannels = favorites,
                        categories = categories,
                        isLoading = false,
                    )
                }
            }.catch { e ->
                emit(HomeState(error = e.message, isLoading = false))
            }.onEach { state ->
                viewState = state
            }.launchIn(viewModelScope)
    }

    override fun obtainEvent(viewEvent: HomeEvent) {
        when (viewEvent) {
            is HomeEvent.OnPlaylistSelected -> selectPlaylist(viewEvent.playlistId)
        }
    }

    private fun selectPlaylist(playlistId: Long?) {
        currentPlaylistRepository.selectPlaylist(playlistId)
    }
}
