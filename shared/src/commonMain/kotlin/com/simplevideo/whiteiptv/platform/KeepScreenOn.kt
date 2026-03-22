package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject

/**
 * Platform-specific controller to keep screen on during video playback.
 * Android: Uses FLAG_KEEP_SCREEN_ON
 * iOS: Uses UIApplication.shared.isIdleTimerDisabled
 */
interface KeepScreenOnController {
    @Composable
    fun Effect()
}

@Composable
fun KeepScreenOn() {
    val controller = koinInject<KeepScreenOnController>()
    controller.Effect()
}
