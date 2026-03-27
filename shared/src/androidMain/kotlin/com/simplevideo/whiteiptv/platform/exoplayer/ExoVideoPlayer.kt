package com.simplevideo.whiteiptv.platform.exoplayer

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import com.simplevideo.whiteiptv.platform.PlaybackState
import com.simplevideo.whiteiptv.platform.PlayerConfig
import com.simplevideo.whiteiptv.platform.TracksInfo
import com.simplevideo.whiteiptv.platform.VideoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val LIVE_OFFSET_POLL_MS = 1000L

/**
 * Android implementation of VideoPlayer wrapping ExoPlayer.
 * Exposes playback state, tracks, and live offset via StateFlow.
 */
@OptIn(UnstableApi::class)
class ExoVideoPlayer(
    private val exoPlayer: ExoPlayer,
    private val trackSelector: DefaultTrackSelector,
    private val dataSourceFactory: DataSource.Factory,
    private val config: PlayerConfig,
) : VideoPlayer {

    private val tracksInfoMapper = TracksInfoMapper(trackSelector)
    private val videoAspectRatio = mutableFloatStateOf(16f / 9f)

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _tracksInfo = MutableStateFlow(TracksInfo())
    override val tracksInfo: StateFlow<TracksInfo> = _tracksInfo.asStateFlow()

    private val _liveOffset = MutableStateFlow(0L)
    override val liveOffset: StateFlow<Long> = _liveOffset.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    init {
        exoPlayer.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    updatePlaybackState()
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updatePlaybackState()
                }

                override fun onTracksChanged(tracks: Tracks) {
                    _tracksInfo.value = tracksInfoMapper.map(tracks)
                }

                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    if (videoSize.width > 0 && videoSize.height > 0) {
                        val ratio = (videoSize.width * videoSize.pixelWidthHeightRatio) / videoSize.height
                        videoAspectRatio.floatValue = ratio
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    // Auto-recover from behind live window error without showing to user
                    if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                        exoPlayer.seekToDefaultPosition()
                        exoPlayer.prepare()
                        return
                    }

                    _playbackState.value = PlaybackState.Error(
                        errorCode = error.errorCode,
                        message = formatErrorMessage(error),
                    )
                }
            },
        )

        // Poll live offset inside the player, not in UI layer
        scope.launch {
            while (true) {
                delay(LIVE_OFFSET_POLL_MS)
                if (exoPlayer.isPlaying) {
                    _liveOffset.value = exoPlayer.currentLiveOffset
                }
            }
        }
    }

    private fun updatePlaybackState() {
        val isBuffering = exoPlayer.playbackState == Player.STATE_BUFFERING
        _playbackState.value = if (isBuffering) {
            PlaybackState.Buffering
        } else {
            PlaybackState.Playing(exoPlayer.isPlaying)
        }
    }

    private fun formatErrorMessage(error: PlaybackException): String = when (error.errorCode) {
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
            "Network connection failed"

        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
            "Connection timeout"

        PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> {
            val cause = error.cause
            when {
                cause?.message?.contains("404") == true -> "Channel not available (404)"
                cause?.message?.contains("403") == true -> "Access denied (403)"
                else -> "Server error: ${cause?.message ?: "Unknown"}"
            }
        }

        PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED ->
            "Invalid stream format"

        else -> error.message ?: "Playback error"
    }

    override fun play() {
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun stop() {
        exoPlayer.stop()
    }

    override fun release() {
        scope.cancel()
        exoPlayer.release()
    }

    override fun seekToLiveEdge() {
        if (exoPlayer.isCurrentMediaItemLive) {
            exoPlayer.seekTo(exoPlayer.duration)
        } else {
            exoPlayer.seekToDefaultPosition()
        }
    }

    override fun getTracksInfo(): TracksInfo {
        return tracksInfoMapper.map(exoPlayer.currentTracks)
    }

    override fun selectAudioTrack(trackId: String) {
        val (groupId, trackIndex) = parseTrackId(trackId) ?: return

        for (trackGroup in exoPlayer.currentTracks.groups) {
            if (trackGroup.mediaTrackGroup.id == groupId) {
                val override = TrackSelectionOverride(trackGroup.mediaTrackGroup, trackIndex)
                trackSelector.setParameters(
                    trackSelector.buildUponParameters()
                        .setOverrideForType(override)
                        .build(),
                )
                return
            }
        }
    }

    override fun selectSubtitleTrack(trackId: String?) {
        if (trackId == null) {
            trackSelector.setParameters(
                trackSelector.buildUponParameters()
                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                    .build(),
            )
            return
        }

        val (groupId, trackIndex) = parseTrackId(trackId) ?: return

        for (trackGroup in exoPlayer.currentTracks.groups) {
            if (trackGroup.mediaTrackGroup.id == groupId) {
                val override = TrackSelectionOverride(trackGroup.mediaTrackGroup, trackIndex)
                trackSelector.setParameters(
                    trackSelector.buildUponParameters()
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                        .setOverrideForType(override)
                        .build(),
                )
                return
            }
        }
    }

    override fun selectVideoQuality(qualityId: String?) {
        if (qualityId == null || qualityId == "auto") {
            trackSelector.setParameters(
                trackSelector.buildUponParameters()
                    .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                    .setForceHighestSupportedBitrate(false)
                    .build(),
            )
            return
        }

        val (groupId, trackIndex) = parseTrackId(qualityId) ?: return

        for (trackGroup in exoPlayer.currentTracks.groups) {
            if (trackGroup.mediaTrackGroup.id == groupId) {
                val override = TrackSelectionOverride(trackGroup.mediaTrackGroup, trackIndex)
                trackSelector.setParameters(
                    trackSelector.buildUponParameters()
                        .setOverrideForType(override)
                        .setForceHighestSupportedBitrate(true)
                        .build(),
                )
                return
            }
        }
    }

    private fun parseTrackId(trackId: String): Pair<String, Int>? {
        val parts = trackId.split(":")
        if (parts.size != 2) return null
        val index = parts[1].toIntOrNull() ?: return null
        return Pair(parts[0], index)
    }

    override fun setMediaSource(
        url: String,
        userAgent: String?,
        referer: String?,
    ) {
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setLiveConfiguration(
                MediaItem.LiveConfiguration.Builder()
                    .setTargetOffsetMs(config.targetLiveOffsetMs)
                    .setMinOffsetMs(config.minLiveOffsetMs)
                    .setMaxOffsetMs(config.maxLiveOffsetMs)
                    .setMinPlaybackSpeed(config.minPlaybackSpeed)
                    .setMaxPlaybackSpeed(config.maxPlaybackSpeed)
                    .build(),
            )
            .build()

        val loadErrorPolicy = object : DefaultLoadErrorHandlingPolicy() {
            override fun getRetryDelayMsFor(
                loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo,
            ): Long {
                val errorCount = loadErrorInfo.errorCount
                return minOf((errorCount * 1000).toLong(), config.maxRetryDelayMs)
            }

            override fun getMinimumLoadableRetryCount(dataType: Int): Int {
                return config.minRetryCount
            }
        }

        val contentType = Util.inferContentType(url)
        val mediaSource = if (contentType == C.CONTENT_TYPE_HLS) {
            HlsMediaSource.Factory(dataSourceFactory)
                .setAllowChunklessPreparation(true)
                .setLoadErrorHandlingPolicy(loadErrorPolicy)
                .createMediaSource(mediaItem)
        } else {
            DefaultMediaSourceFactory(dataSourceFactory)
                .setLoadErrorHandlingPolicy(loadErrorPolicy)
                .createMediaSource(mediaItem)
        }

        // Reset error state when loading new source
        _playbackState.value = PlaybackState.Buffering
        _liveOffset.value = 0L

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
    }

    @Composable
    override fun PlayerView(modifier: Modifier) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            PlayerSurface(
                player = exoPlayer,
                surfaceType = SURFACE_TYPE_SURFACE_VIEW,
                modifier = Modifier.aspectRatio(videoAspectRatio.floatValue),
            )
        }
    }
}
