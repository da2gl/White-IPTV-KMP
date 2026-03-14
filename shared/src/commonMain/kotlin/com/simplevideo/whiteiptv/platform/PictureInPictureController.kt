package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable

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

/**
 * Creates platform-specific PictureInPictureController instance.
 * Must be called from Composable context on Android (needs Activity reference).
 */
@Composable
expect fun rememberPipController(): PictureInPictureController
