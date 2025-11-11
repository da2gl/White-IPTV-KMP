package com.simplevideo.whiteiptv.feature.splash

import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import com.simplevideo.whiteiptv.feature.splash.mvi.SplashAction
import com.simplevideo.whiteiptv.feature.splash.mvi.SplashEvent
import com.simplevideo.whiteiptv.feature.splash.mvi.SplashState
import com.simplevideo.whiteiptv.navigation.Route
import kotlinx.coroutines.launch

/**
 * ViewModel for Splash screen
 */
class SplashViewModel(
    private val playlistRepository: PlaylistRepository,
) : BaseViewModel<SplashState, SplashAction, SplashEvent>(
    initialState = SplashState(),
) {
    init {
        viewModelScope.launch {
            val targetRoute = if (playlistRepository.hasPlaylist()) Route.Main else Route.Onboarding
            viewState = viewState.copy(isLoading = false)
            viewAction = SplashAction.Navigate(targetRoute)
        }
    }
}
