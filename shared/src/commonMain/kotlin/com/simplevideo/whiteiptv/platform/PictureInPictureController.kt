package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject

/**
 * Platform-specific Picture-in-Picture controller.
 *
 * Android: enters PiP mode via Activity.enterPictureInPictureMode()
 * iOS: no-op stub (real PiP deferred to iOS AVPlayer implementation)
 */
interface PictureInPictureController {
    /** Whether PiP is supported on this device/platform */
    fun isPipSupported(): Boolean

    /** Enter Picture-in-Picture mode */
    fun enterPipMode()
}

/** Provides platform-specific PictureInPictureController via Koin DI. */
@Composable
fun rememberPipController(): PictureInPictureController = koinInject<PictureInPictureController>()
