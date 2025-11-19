package com.simplevideo.whiteiptv.feature.favorites

import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.domain.usecase.GetFavoritesUseCase
import com.simplevideo.whiteiptv.domain.usecase.ToggleFavoriteUseCase
import com.simplevideo.whiteiptv.feature.favorites.mvi.FavoritesAction
import com.simplevideo.whiteiptv.feature.favorites.mvi.FavoritesEvent
import com.simplevideo.whiteiptv.feature.favorites.mvi.FavoritesState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : BaseViewModel<FavoritesState, FavoritesAction, FavoritesEvent>(
    initialState = FavoritesState(),
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        observeFavorites()
    }

    override fun obtainEvent(viewEvent: FavoritesEvent) {
        when (viewEvent) {
            is FavoritesEvent.OnCategorySelected -> {
                viewState =
                    viewState.copy(
                        selectedCategory = if (viewState.selectedCategory == viewEvent.category) null else viewEvent.category,
                    )
            }

            is FavoritesEvent.OnToggleFavorite -> {
                viewModelScope.launch {
                    toggleFavoriteUseCase(viewEvent.channelId)
                }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            // TODO: Update to use Flow from repository
            val channels = getFavoritesUseCase()
            viewState = viewState.copy(
                channels = channels,
                isLoading = false,
            )
        }
    }
}
