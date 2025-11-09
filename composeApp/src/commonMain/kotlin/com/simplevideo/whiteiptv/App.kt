package com.simplevideo.whiteiptv

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.simplevideo.whiteiptv.designsystem.AppTheme
import com.simplevideo.whiteiptv.di.initKoin
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import com.simplevideo.whiteiptv.navigation.AppNavGraph
import com.simplevideo.whiteiptv.navigation.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.inject

@Composable
@Preview
fun App() {
    KoinContext {
        val playlistRepository: PlaylistRepository = inject()
        var startRoute by remember { mutableStateOf<Route?>(null) }

        LaunchedEffect(Unit) {
            initKoin()
            CoroutineScope(Dispatchers.Default).launch {
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
