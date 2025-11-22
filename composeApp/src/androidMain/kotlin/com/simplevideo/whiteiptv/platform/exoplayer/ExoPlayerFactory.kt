package com.simplevideo.whiteiptv.platform.exoplayer

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.simplevideo.whiteiptv.platform.PlayerConfig
import com.simplevideo.whiteiptv.platform.VideoPlayer
import com.simplevideo.whiteiptv.platform.VideoPlayerFactory

/**
 * Android implementation of VideoPlayerFactory
 * Creates optimized ExoPlayer instances for IPTV live streaming
 */
@OptIn(UnstableApi::class)
class ExoPlayerFactory(
    private val context: Context,
    private val config: PlayerConfig = PlayerConfig.Default,
) : VideoPlayerFactory {

    private val componentFactory = ExoPlayerComponentFactory(context, config)
    private val dataSourceProvider = DataSourceFactoryProvider(context)

    override fun createPlayer(): VideoPlayer {
        val loadControl = componentFactory.createLoadControl()
        val trackSelector = componentFactory.createTrackSelector()
        val audioAttributes = componentFactory.createAudioAttributes()
        val livePlaybackSpeedControl = componentFactory.createLivePlaybackSpeedControl()
        val dataSourceFactory = dataSourceProvider.create(config)

        val exoPlayer = ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .setTrackSelector(trackSelector)
            .setLivePlaybackSpeedControl(livePlaybackSpeedControl)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()

        return ExoVideoPlayer(exoPlayer, trackSelector, dataSourceFactory, config)
    }
}
