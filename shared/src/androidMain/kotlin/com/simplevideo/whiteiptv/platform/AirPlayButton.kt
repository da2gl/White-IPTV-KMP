package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun AirPlayButton(modifier: Modifier) {
    // No-op on Android — AirPlay is iOS-only
}
