package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun CastButton(modifier: Modifier) {
    // No-op on iOS — Chromecast is Android-only
}
