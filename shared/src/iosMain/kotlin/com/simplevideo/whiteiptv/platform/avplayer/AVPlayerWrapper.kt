package com.simplevideo.whiteiptv.platform.avplayer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.simplevideo.whiteiptv.platform.PlaybackState
import com.simplevideo.whiteiptv.platform.TracksInfo
import com.simplevideo.whiteiptv.platform.VideoPlayer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import platform.AVFoundation.AVLayerVideoGravityResizeAspect
import platform.AVFoundation.AVMediaCharacteristicAudible
import platform.AVFoundation.AVMediaCharacteristicLegible
import platform.AVFoundation.AVMediaSelectionOption
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemFailedToPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemStatusFailed
import platform.AVFoundation.AVPlayerItemStatusReadyToPlay
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
import platform.AVFoundation.AVURLAsset
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMake
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSDate
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIView

private const val LIVE_OFFSET_POLL_MS = 1000L

/**
 * iOS implementation of VideoPlayer wrapping AVPlayer.
 * Exposes playback state, tracks, and live offset via StateFlow.
 */
@OptIn(ExperimentalForeignApi::class)
class AVPlayerWrapper : VideoPlayer {

    internal val avPlayer = AVPlayer()
    private val tracksMapper = AVPlayerTracksMapper()
    private var currentPlayerItem: AVPlayerItem? = null
    private var isPlayerPlaying = false
    private var timeObserver: Any? = null
    private var endTimeObserver: Any? = null
    private var failedObserver: Any? = null
    private var tracksNotified = false
    private var previousItemStatus: Long = -1

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _tracksInfo = MutableStateFlow(TracksInfo())
    override val tracksInfo: StateFlow<TracksInfo> = _tracksInfo.asStateFlow()

    private val _liveOffset = MutableStateFlow(0L)
    override val liveOffset: StateFlow<Long> = _liveOffset.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        setupPeriodicTimeObserver()

        scope.launch {
            while (true) {
                delay(LIVE_OFFSET_POLL_MS)
                if (isPlayerPlaying) {
                    _liveOffset.value = computeLiveOffset()
                }
            }
        }
    }

    private fun computeLiveOffset(): Long {
        val item = currentPlayerItem ?: return 0L
        val currentSec = CMTimeGetSeconds(item.currentTime())
        val durationSec = CMTimeGetSeconds(item.duration())
        if (currentSec.isNaN() || durationSec.isNaN() || durationSec.isInfinite()) {
            return 0L
        }
        return ((durationSec - currentSec) * 1000).toLong()
    }

    override fun play() {
        avPlayer.play()
    }

    override fun pause() {
        avPlayer.pause()
    }

    override fun stop() {
        avPlayer.pause()
        avPlayer.seekToTime(CMTimeMakeWithSeconds(0.0, 1))
    }

    override fun release() {
        scope.cancel()
        avPlayer.pause()
        removeNotificationObservers()
        timeObserver?.let { avPlayer.removeTimeObserver(it) }
        timeObserver = null
        avPlayer.replaceCurrentItemWithPlayerItem(null)
        currentPlayerItem = null
    }

    override fun seekToLiveEdge() {
        currentPlayerItem?.seekToDate(NSDate.date())
    }

    override fun getTracksInfo(): TracksInfo {
        val item = currentPlayerItem ?: return TracksInfo()
        return tracksMapper.map(item)
    }

    override fun selectAudioTrack(trackId: String) {
        val item = currentPlayerItem ?: return
        val group = item.asset.mediaSelectionGroupForMediaCharacteristic(AVMediaCharacteristicAudible) ?: return

        @Suppress("UNCHECKED_CAST")
        val options = group.options as List<AVMediaSelectionOption>
        val index = trackId.toIntOrNull() ?: return

        if (index in options.indices) {
            item.selectMediaOption(options[index], inMediaSelectionGroup = group)
            _tracksInfo.value = getTracksInfo()
        }
    }

    override fun selectSubtitleTrack(trackId: String?) {
        val item = currentPlayerItem ?: return
        val group = item.asset.mediaSelectionGroupForMediaCharacteristic(AVMediaCharacteristicLegible) ?: return

        if (trackId == null) {
            item.selectMediaOption(null, inMediaSelectionGroup = group)
            _tracksInfo.value = getTracksInfo()
            return
        }

        @Suppress("UNCHECKED_CAST")
        val options = group.options as List<AVMediaSelectionOption>
        val index = trackId.toIntOrNull() ?: return

        if (index in options.indices) {
            item.selectMediaOption(options[index], inMediaSelectionGroup = group)
            _tracksInfo.value = getTracksInfo()
        }
    }

    override fun selectVideoQuality(qualityId: String?) {
        val item = currentPlayerItem ?: return
        if (qualityId == null || qualityId == "auto") {
            item.preferredPeakBitRate = 0.0
        } else {
            val bitrate = qualityId.toDoubleOrNull() ?: return
            item.preferredPeakBitRate = bitrate
        }
    }

    override fun setMediaSource(
        url: String,
        userAgent: String?,
        referer: String?,
    ) {
        removeNotificationObservers()

        val nsUrl = NSURL.URLWithString(url) ?: return

        val headers = mutableMapOf<Any?, Any?>()
        userAgent?.let { headers["User-Agent"] = it }
        referer?.let { headers["Referer"] = it }

        val asset = if (headers.isNotEmpty()) {
            AVURLAsset.URLAssetWithURL(
                URL = nsUrl,
                options = mapOf("AVURLAssetHTTPHeaderFieldsKey" to headers),
            )
        } else {
            AVURLAsset.URLAssetWithURL(URL = nsUrl, options = null)
        }

        val playerItem = AVPlayerItem.playerItemWithAsset(asset)
        currentPlayerItem = playerItem
        tracksNotified = false
        previousItemStatus = -1

        _playbackState.value = PlaybackState.Buffering
        _liveOffset.value = 0L

        addNotificationObservers(playerItem)
        avPlayer.replaceCurrentItemWithPlayerItem(playerItem)
        avPlayer.play()
    }

    @Composable
    override fun PlayerView(modifier: Modifier) {
        val player = avPlayer
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            UIKitView(
                factory = {
                    val containerView = UIView()
                    val playerLayer = AVPlayerLayer.playerLayerWithPlayer(player)
                    playerLayer.videoGravity = AVLayerVideoGravityResizeAspect
                    containerView.layer.addSublayer(playerLayer)
                    containerView
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    val playerLayer = view.layer.sublayers?.firstOrNull() as? AVPlayerLayer
                    CATransaction.begin()
                    CATransaction.setValue(true, kCATransactionDisableActions)
                    playerLayer?.frame = view.bounds
                    CATransaction.commit()
                },
                onResize = { view, rect ->
                    val playerLayer = view.layer.sublayers?.firstOrNull() as? AVPlayerLayer
                    CATransaction.begin()
                    CATransaction.setValue(true, kCATransactionDisableActions)
                    playerLayer?.frame = rect
                    CATransaction.commit()
                },
            )
        }
    }

    // --- Internal: periodic observer replaces KVO ---

    private fun setupPeriodicTimeObserver() {
        val interval = CMTimeMake(value = 1, timescale = 2)
        timeObserver = avPlayer.addPeriodicTimeObserverForInterval(interval, queue = null) { _ ->
            pollPlaybackState()
            pollItemStatus()
        }
    }

    private fun pollPlaybackState() {
        val wasPlaying = isPlayerPlaying
        isPlayerPlaying = avPlayer.timeControlStatus == AVPlayerTimeControlStatusPlaying
        val isBuffering = avPlayer.timeControlStatus == AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate

        if (isPlayerPlaying != wasPlaying || isBuffering) {
            _playbackState.value = if (isBuffering) {
                PlaybackState.Buffering
            } else {
                PlaybackState.Playing(isPlayerPlaying)
            }
        }
    }

    private fun pollItemStatus() {
        val item = currentPlayerItem ?: return
        val status = item.status
        if (status == previousItemStatus) return
        previousItemStatus = status

        when (status) {
            AVPlayerItemStatusReadyToPlay -> {
                if (!tracksNotified) {
                    tracksNotified = true
                    _tracksInfo.value = getTracksInfo()
                }
            }
            AVPlayerItemStatusFailed -> {
                val error = item.error
                val errorCode = error?.code?.toInt() ?: -1
                val errorMessage = error?.localizedDescription ?: "Unknown playback error"
                _playbackState.value = PlaybackState.Error(errorCode, errorMessage)
            }
            else -> {}
        }
    }

    // --- Notification observers for end-of-playback ---

    private fun addNotificationObservers(item: AVPlayerItem) {
        endTimeObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = item,
            queue = null,
        ) { _: NSNotification? ->
            isPlayerPlaying = false
            _playbackState.value = PlaybackState.Playing(isPlaying = false)
        }

        failedObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemFailedToPlayToEndTimeNotification,
            `object` = item,
            queue = null,
        ) { notification: NSNotification? ->
            @Suppress("UNCHECKED_CAST")
            val error = notification?.userInfo?.get("AVPlayerItemFailedToPlayToEndTimeErrorKey")
            val errorMessage = error?.toString() ?: "Failed to play to end"
            _playbackState.value = PlaybackState.Error(-1, errorMessage)
        }
    }

    private fun removeNotificationObservers() {
        endTimeObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        endTimeObserver = null
        failedObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        failedObserver = null
    }
}
