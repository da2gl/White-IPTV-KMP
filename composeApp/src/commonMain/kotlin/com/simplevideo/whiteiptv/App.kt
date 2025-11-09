package com.simplevideo.whiteiptv

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.simplevideo.whiteiptv.designsystem.AppTheme
import com.simplevideo.whiteiptv.di.initKoin
import com.simplevideo.whiteiptv.navigation.AppNavGraph
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Main App composable for WhiteIPTV
 *
 * Initializes Koin DI and sets up the app theme and navigation
 *
 * TODO: Handle app lifecycle events
 * TODO: Add global error handling
 * TODO: Add analytics initialization
 */
@Composable
@Preview
fun App() {
    // Initialize Koin DI once
    LaunchedEffect(Unit) {
        initKoin()
    }

    // Apply app theme
    AppTheme {
        // Setup navigation
        AppNavGraph()
    }
}
