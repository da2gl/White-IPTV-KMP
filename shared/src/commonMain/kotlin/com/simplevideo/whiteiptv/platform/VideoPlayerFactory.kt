package com.simplevideo.whiteiptv.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow

/**
 * Sealed hierarchy for player playback state
 */
sealed interface PlaybackState {
    data object Idle : PlaybackState
    data object Buffering : PlaybackState
    data class Playing(val isPlaying: Boolean) : PlaybackState
    data class Error(val errorCode: Int, val message: String) : PlaybackState
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

    /** Reactive playback state */
    val playbackState: StateFlow<PlaybackState>

    /** Reactive tracks info, updated when media tracks change */
    val tracksInfo: StateFlow<TracksInfo>

    /** Reactive live offset in milliseconds (distance from live edge), updated every second */
    val liveOffset: StateFlow<Long>

    /** Seek to live edge (default position for live streams) */
    fun seekToLiveEdge()

    /** Get current available tracks (snapshot) */
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
