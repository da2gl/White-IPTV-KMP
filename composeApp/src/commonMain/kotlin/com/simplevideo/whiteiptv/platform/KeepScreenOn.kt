package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable

/**
 * Platform-specific composable to keep screen on during video playback
 * Android: Uses FLAG_KEEP_SCREEN_ON
 * iOS: Uses UIApplication.shared.isIdleTimerDisabled
 */
@Composable
expect fun KeepScreenOn()
