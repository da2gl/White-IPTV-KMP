package com.simplevideo.whiteiptv

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import com.simplevideo.whiteiptv.designsystem.AppTheme
import com.simplevideo.whiteiptv.di.initKoin
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import com.simplevideo.whiteiptv.navigation.AppNavGraph
import com.simplevideo.whiteiptv.navigation.Route
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.inject

@Composable
@Preview
fun App() {
    val isKoinInitialized by produceState(initialValue = false) {
        initKoin()
        value = true
    }

    if (isKoinInitialized) {
        KoinContext {
            val playlistRepository: PlaylistRepository = inject()
            val startRoute by produceState<Route?>(initialValue = null, producer = {
                value = if (playlistRepository.hasPlaylist()) {
                    Route.Main
                } else {
                    Route.Onboarding
                }
            })

            AppTheme {
                startRoute?.let {
                    AppNavGraph(startDestination = it)
                }
            }
        }
    }
}
