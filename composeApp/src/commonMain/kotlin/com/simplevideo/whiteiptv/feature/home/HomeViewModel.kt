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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class HomeViewModel(
    getContinueWatchingUseCase: GetContinueWatchingUseCase,
    channelRepository: ChannelRepository,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    playlistRepository: PlaylistRepository,
    private val currentPlaylistRepository: CurrentPlaylistRepository,
) : BaseViewModel<HomeState, HomeAction, HomeEvent>(initialState = HomeState()) {

    init {
        combine(
            playlistRepository.getPlaylists(),
            currentPlaylistRepository.selectedPlaylistId,
            getContinueWatchingUseCase(),
            channelRepository.getFavoriteChannels(),
        ) { playlists, selectedPlaylistId, continueWatching, favorites ->
            val categories = getCategoriesUseCase(playlistId = selectedPlaylistId)
            HomeState(
                playlists = playlists,
                selectedPlaylistId = selectedPlaylistId,
                continueWatchingItems = continueWatching,
                favoriteChannels = favorites,
                categories = categories,
                isLoading = false,
            )
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
