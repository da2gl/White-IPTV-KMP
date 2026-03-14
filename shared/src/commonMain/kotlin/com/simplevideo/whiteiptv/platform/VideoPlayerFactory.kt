package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Callback interface for player events
 */
interface PlayerListener {
    fun onPlaybackStateChanged(isPlaying: Boolean, isBuffering: Boolean)
    fun onError(errorCode: Int, errorMessage: String)
    fun onTracksChanged(tracksInfo: TracksInfo) {}
}

/**
 * Common interface for video player instances
 * Platform implementations wrap native players (ExoPlayer, AVPlayer)
 */
interface VideoPlayer {
    fun play()
    fun pause()
    fun stop()
    fun release()
    fun setMediaSource(
        url: String,
        userAgent: String? = null,
        referer: String? = null,
    )

    /** Check if player is currently playing */
    fun isPlaying(): Boolean

    /** Get current live offset in milliseconds (distance from live edge) */
    fun getCurrentLiveOffset(): Long

    /** Seek to live edge (default position for live streams) */
    fun seekToLiveEdge()

    /** Add listener for player events */
    fun addListener(listener: PlayerListener)

    /** Remove listener */
    fun removeListener(listener: PlayerListener)

    /** Get current available tracks */
    fun getTracksInfo(): TracksInfo

    /** Select audio track by ID */
    fun selectAudioTrack(trackId: String)

    /** Select subtitle track by ID, null to disable */
    fun selectSubtitleTrack(trackId: String?)

    /** Select video quality by ID, null for auto */
    fun selectVideoQuality(qualityId: String?)

    /**
     * Composable that renders the video surface
     * Each platform provides its own implementation
     */
    @Composable
    fun PlayerView(modifier: Modifier)
}

/**
 * Platform-specific video player factory
 */
interface VideoPlayerFactory {
    fun createPlayer(): VideoPlayer
}
