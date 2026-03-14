package com.simplevideo.whiteiptv.platform

import com.simplevideo.whiteiptv.platform.avplayer.AVPlayerWrapper

/**
 * iOS implementation of VideoPlayerFactory
 * Creates AVPlayer-based video player instances for HLS/IPTV streaming
 */
class IOSVideoPlayerFactory : VideoPlayerFactory {

    override fun createPlayer(): VideoPlayer {
        return AVPlayerWrapper()
    }
}
