package com.simplevideo.whiteiptv.platform

import com.simplevideo.whiteiptv.platform.avplayer.AVPlayerWrapper

/**
 * iOS implementation of VideoPlayerFactory.
 * Creates AVPlayer-based video player instances for HLS/IPTV streaming.
 * Tracks the last created player for PiP controller access.
 */
class IOSVideoPlayerFactory : VideoPlayerFactory {

    internal var lastCreatedPlayer: AVPlayerWrapper? = null
        private set

    override fun createPlayer(): VideoPlayer {
        val player = AVPlayerWrapper()
        lastCreatedPlayer = player
        return player
    }
}
