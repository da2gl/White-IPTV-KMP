package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * iOS PiP stub — no-op until iOS AVPlayer implementation supports PiP.
 */
class IOSPictureInPictureController : PictureInPictureController {

    override fun isPipSupported(): Boolean = false

    override fun enterPipMode() {
        // No-op: iOS PiP requires AVPictureInPictureController with AVPlayerLayer
    }
}

@Composable
actual fun rememberPipController(): PictureInPictureController {
    return remember { IOSPictureInPictureController() }
}
