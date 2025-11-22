package com.simplevideo.whiteiptv.feature.splash.mvi

import com.simplevideo.whiteiptv.navigation.Route

data class SplashState(
    val isLoading: Boolean = true,
)

sealed interface SplashEvent

sealed interface SplashAction {
    data class Navigate(val route: Route) : SplashAction
}
