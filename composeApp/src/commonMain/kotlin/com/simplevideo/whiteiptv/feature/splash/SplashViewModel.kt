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
 *
 * Determines initial navigation route based on app state:
 * - If playlist exists -> navigate to Main
 * - If no playlist -> navigate to Onboarding
 */
class SplashViewModel(
    private val playlistRepository: PlaylistRepository,
) : BaseViewModel<SplashState, SplashAction, SplashEvent>(
    initialState = SplashState(),
) {

    init {
        determineInitialRoute()
    }

    override fun obtainEvent(viewEvent: SplashEvent) {
        // No user events on splash screen
    }

    private fun determineInitialRoute() {
        viewModelScope.launch {
            try {
                val hasPlaylist = playlistRepository.hasPlaylist()
                val targetRoute = if (hasPlaylist) {
                    Route.Main
                } else {
                    Route.Onboarding
                }

                viewState = viewState.copy(isLoading = false)
                viewAction = SplashAction.Navigate(targetRoute)
            } catch (e: Exception) {
                // On error, navigate to onboarding to let user set up playlist
                viewState = viewState.copy(isLoading = false)
                viewAction = SplashAction.Navigate(Route.Onboarding)
            }
        }
    }
}
