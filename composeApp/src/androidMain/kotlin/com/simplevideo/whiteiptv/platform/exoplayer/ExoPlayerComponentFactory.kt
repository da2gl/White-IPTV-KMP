package com.simplevideo.whiteiptv.platform.exoplayer

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLivePlaybackSpeedControl
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultAllocator
import com.simplevideo.whiteiptv.platform.PlayerConfig

/**
 * Factory for creating ExoPlayer components with IPTV-optimized settings
 */
@OptIn(UnstableApi::class)
class ExoPlayerComponentFactory(
    private val context: Context,
    private val config: PlayerConfig,
) {

    fun createLoadControl(): DefaultLoadControl {
        return DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE))
            .setBufferDurationsMs(
                config.minBufferMs,
                config.maxBufferMs,
                config.bufferForPlaybackMs,
                config.bufferForPlaybackAfterRebufferMs,
            )
            .setTargetBufferBytes(C.LENGTH_UNSET)
            .setPrioritizeTimeOverSizeThresholds(true)
            .setBackBuffer(config.backBufferMs, true)
            .build()
    }

    fun createTrackSelector(): DefaultTrackSelector {
        return DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters()
                    .setExceedVideoConstraintsIfNecessary(true)
                    .setExceedRendererCapabilitiesIfNecessary(true),
            )
        }
    }

    fun createAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
    }

    fun createLivePlaybackSpeedControl(): DefaultLivePlaybackSpeedControl {
        return DefaultLivePlaybackSpeedControl.Builder()
            .setFallbackMinPlaybackSpeed(config.minPlaybackSpeed)
            .setFallbackMaxPlaybackSpeed(config.maxPlaybackSpeed)
            .setProportionalControlFactor(0.1f)
            .setTargetLiveOffsetIncrementOnRebufferMs(500)
            .build()
    }
}
