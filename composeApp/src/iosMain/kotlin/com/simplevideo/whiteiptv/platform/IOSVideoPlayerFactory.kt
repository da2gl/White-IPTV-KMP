package com.simplevideo.whiteiptv.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * iOS placeholder implementation of VideoPlayer
 * TODO: Implement with AVPlayer
 */
class IOSVideoPlayer : VideoPlayer {

    private val listeners = mutableListOf<PlayerListener>()

    override fun play() {
        // TODO: AVPlayer play
    }

    override fun pause() {
        // TODO: AVPlayer pause
    }

    override fun stop() {
        // TODO: AVPlayer stop
    }

    override fun release() {
        listeners.clear()
        // TODO: Release AVPlayer
    }

    override fun isPlaying(): Boolean = false

    override fun getCurrentLiveOffset(): Long = 0L

    override fun seekToLiveEdge() {
        // TODO: AVPlayer seek to live edge
    }

    override fun addListener(listener: PlayerListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: PlayerListener) {
        listeners.remove(listener)
    }

    override fun setMediaSource(
        url: String,
        userAgent: String?,
        referer: String?,
    ) {
        // TODO: Set AVPlayerItem with custom headers
    }

    @Composable
    override fun PlayerView(modifier: Modifier) {
        // TODO: Implement with AVPlayer UIKitView
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "iOS Player - Coming Soon",
                color = Color.White,
            )
        }
    }
}

/**
 * iOS placeholder implementation of VideoPlayerFactory
 * TODO: Implement with AVPlayer
 */
class IOSVideoPlayerFactory : VideoPlayerFactory {

    override fun createPlayer(): VideoPlayer {
        return IOSVideoPlayer()
    }
}
