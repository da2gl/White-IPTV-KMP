package com.simplevideo.whiteiptv.platform.exoplayer

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import com.simplevideo.whiteiptv.platform.PlayerConfig
import com.simplevideo.whiteiptv.platform.PlayerListener
import com.simplevideo.whiteiptv.platform.TracksInfo
import com.simplevideo.whiteiptv.platform.VideoPlayer

/**
 * Android implementation of VideoPlayer wrapping ExoPlayer
 * Uses custom Compose controls with gesture support
 */
@OptIn(UnstableApi::class)
class ExoVideoPlayer(
    private val exoPlayer: ExoPlayer,
    private val trackSelector: DefaultTrackSelector,
    private val dataSourceFactory: DataSource.Factory,
    private val config: PlayerConfig,
) : VideoPlayer {

    private val listeners = mutableListOf<PlayerListener>()
    private val tracksInfoMapper = TracksInfoMapper(trackSelector)
    private val videoAspectRatio = mutableFloatStateOf(16f / 9f) // Default 16:9

    init {
        exoPlayer.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    val isPlaying = exoPlayer.isPlaying
                    val isBuffering = playbackState == Player.STATE_BUFFERING
                    listeners.forEach { it.onPlaybackStateChanged(isPlaying, isBuffering) }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    val isBuffering = exoPlayer.playbackState == Player.STATE_BUFFERING
                    listeners.forEach { it.onPlaybackStateChanged(isPlaying, isBuffering) }
                }

                override fun onTracksChanged(tracks: Tracks) {
                    val tracksInfo = tracksInfoMapper.map(tracks)
                    listeners.forEach { it.onTracksChanged(tracksInfo) }
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

                    // User-friendly error messages
                    val message = when (error.errorCode) {
                        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                            "Network connection failed"

                        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                            "Connection timeout"

                        PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> {
                            val cause = error.cause
                            if (cause?.message?.contains("404") == true) {
                                "Channel not available (404)"
                            } else if (cause?.message?.contains("403") == true) {
                                "Access denied (403)"
                            } else {
                                "Server error: ${cause?.message ?: "Unknown"}"
                            }
                        }

                        PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED ->
                            "Invalid stream format"

                        else -> error.message ?: "Playback error"
                    }

                    listeners.forEach { it.onError(error.errorCode, message) }
                }
            },
        )
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
        listeners.clear()
        exoPlayer.release()
    }

    override fun isPlaying(): Boolean = exoPlayer.isPlaying

    override fun getCurrentLiveOffset(): Long = exoPlayer.currentLiveOffset

    override fun seekToLiveEdge() {
        // seekToDefaultPosition() goes to targetLiveOffset, not actual live edge
        // Use duration to seek to the real live edge (0 offset)
        if (exoPlayer.isCurrentMediaItemLive) {
            exoPlayer.seekTo(exoPlayer.duration)
        } else {
            exoPlayer.seekToDefaultPosition()
        }
    }

    override fun addListener(listener: PlayerListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: PlayerListener) {
        listeners.remove(listener)
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

        val mediaSource = if (url.contains(".m3u8") || url.contains("hls")) {
            HlsMediaSource.Factory(dataSourceFactory)
                .setAllowChunklessPreparation(true)
                .setLoadErrorHandlingPolicy(loadErrorPolicy)
                .createMediaSource(mediaItem)
        } else {
            DefaultMediaSourceFactory(dataSourceFactory)
                .createMediaSource(mediaItem)
        }

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
