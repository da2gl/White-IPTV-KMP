package com.simplevideo.whiteiptv.feature.home

import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.domain.repository.CurrentPlaylistRepository
import com.simplevideo.whiteiptv.domain.usecase.GetContinueWatchingUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetFavoritesUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetHomeCategoriesUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetPlaylistsUseCase
import com.simplevideo.whiteiptv.feature.home.mvi.HomeAction
import com.simplevideo.whiteiptv.feature.home.mvi.HomeEvent
import com.simplevideo.whiteiptv.feature.home.mvi.HomeState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    getPlaylists: GetPlaylistsUseCase,
    getContinueWatching: GetContinueWatchingUseCase,
    getFavorites: GetFavoritesUseCase,
    getHomeCategories: GetHomeCategoriesUseCase,
    private val currentPlaylistRepository: CurrentPlaylistRepository,
) : BaseViewModel<HomeState, HomeAction, HomeEvent>(initialState = HomeState()) {

    init {
        currentPlaylistRepository.selection
            .flatMapLatest { selection ->
                combine(
                    getPlaylists(),
                    getContinueWatching(),
                    getFavorites(selection),
                    getHomeCategories(selection),
                ) { playlists, continueWatching, favorites, categories ->
                    HomeState(
                        playlists = playlists,
                        selection = selection,
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
        }
    }
}
