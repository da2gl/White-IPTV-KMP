package com.simplevideo.whiteiptv.platform

import com.simplevideo.whiteiptv.platform.avplayer.AVPlayerWrapper
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVPlayerLayer
import platform.AVKit.AVPictureInPictureController
import platform.CoreGraphics.CGRectMake

@OptIn(ExperimentalForeignApi::class)
class IOSPictureInPictureController(
    playerWrapper: AVPlayerWrapper?,
) : PictureInPictureController {

    private var pipController: AVPictureInPictureController? = null

    init {
        playerWrapper?.let { wrapper ->
            val playerLayer = AVPlayerLayer.playerLayerWithPlayer(wrapper.avPlayer)
            playerLayer.frame = CGRectMake(0.0, 0.0, 1.0, 1.0)
            if (AVPictureInPictureController.isPictureInPictureSupported()) {
                pipController = AVPictureInPictureController(playerLayer = playerLayer)
            }
        }
    }

    override fun isPipSupported(): Boolean =
        pipController != null && AVPictureInPictureController.isPictureInPictureSupported()

    override fun enterPipMode() {
        pipController?.startPictureInPicture()
    }
}
