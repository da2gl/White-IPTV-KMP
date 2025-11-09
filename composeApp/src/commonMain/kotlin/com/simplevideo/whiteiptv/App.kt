package com.simplevideo.whiteiptv

import androidx.compose.runtime.*
import com.simplevideo.whiteiptv.designsystem.AppTheme
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import com.simplevideo.whiteiptv.navigation.AppNavGraph
import com.simplevideo.whiteiptv.navigation.Route
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    KoinContext {
        val playlistRepository: PlaylistRepository = koinInject()
        val scope = rememberCoroutineScope()
        var startRoute by remember { mutableStateOf<Route?>(null) }

        LaunchedEffect(Unit) {
            scope.launch {
                startRoute = if (playlistRepository.hasPlaylist()) {
                    Route.Main
                } else {
                    Route.Onboarding
                }
            }
        }

        AppTheme {
            startRoute?.let {
                AppNavGraph(startDestination = it)
            }
        }
    }
}
