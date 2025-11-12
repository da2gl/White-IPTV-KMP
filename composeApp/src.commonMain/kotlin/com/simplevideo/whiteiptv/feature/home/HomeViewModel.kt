package com.simplevideo.whiteiptv.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.domain.usecase.GetContinueWatchingUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetFavoritesUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetSportsUseCase
import com.simplevideo.whiteiptv.feature.home.mvi.HomeAction
import com.simplevideo.whiteiptv.feature.home.mvi.HomeEvent
import com.simplevideo.whiteiptv.feature.home.mvi.HomeState
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getContinueWatchingUseCase: GetContinueWatchingUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val getSportsUseCase: GetSportsUseCase
) : BaseViewModel<HomeState, HomeAction, HomeEvent>(initialState = HomeState()) {

    init {
        loadContent()
    }

    private fun loadContent() {
        viewModelScope.launch {
            viewState = viewState.copy(isLoading = true)
            try {
                val continueWatchingItems = getContinueWatchingUseCase()
                val favoriteChannels = getFavoritesUseCase()
                val sportsChannels = getSportsUseCase()
                viewState = viewState.copy(
                    continueWatchingItems = continueWatchingItems,
                    favoriteChannels = favoriteChannels,
                    sportsChannels = sportsChannels,
                    isLoading = false
                )
            } catch (e: Exception) {
                viewState = viewState.copy(error = e.message, isLoading = false)
            }
        }
    }
}
