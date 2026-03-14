package com.simplevideo.whiteiptv

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.simplevideo.whiteiptv.data.local.SettingsPreferences
import com.simplevideo.whiteiptv.data.scheduler.BackgroundRefreshCoordinator
import com.simplevideo.whiteiptv.platform.BackgroundScheduler
import com.simplevideo.whiteiptv.designsystem.AppTheme
import com.simplevideo.whiteiptv.domain.model.AccentColor
import com.simplevideo.whiteiptv.domain.model.ThemeMode
import com.simplevideo.whiteiptv.domain.repository.ThemeRepository
import com.simplevideo.whiteiptv.navigation.AppNavGraph
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    val themeRepository: ThemeRepository = koinInject()
    val backgroundScheduler: BackgroundScheduler = koinInject()
    val settingsPreferences: SettingsPreferences = koinInject()
    val refreshCoordinator: BackgroundRefreshCoordinator = koinInject()
    val themeMode by themeRepository.themeMode.collectAsState()

    LaunchedEffect(Unit) {
        if (settingsPreferences.getAutoUpdateEnabled() && !backgroundScheduler.isScheduled()) {
            val interval = refreshCoordinator.calculateIntervalSeconds()
            backgroundScheduler.schedule(interval)
        }
    }

    val accentColor by settingsPreferences.accentColorFlow
        .collectAsState(initial = AccentColor.Teal)

    val darkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    AppTheme(darkTheme = darkTheme, accentColor = accentColor) {
        AppNavGraph()
    }
}
