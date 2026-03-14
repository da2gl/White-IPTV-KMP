package com.simplevideo.whiteiptv.platform.avplayer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.simplevideo.whiteiptv.platform.PlayerListener
import com.simplevideo.whiteiptv.platform.TracksInfo
import com.simplevideo.whiteiptv.platform.VideoPlayer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.*
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMake
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.*
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIView

/**
 * iOS implementation of VideoPlayer wrapping AVPlayer.
 * Uses periodic time observer for state polling instead of KVO
 * (KVO observeValueForKeyPath is not overridable in Kotlin/Native).
 */
@OptIn(ExperimentalForeignApi::class)
class AVPlayerWrapper : VideoPlayer {

    private val avPlayer = AVPlayer()
    private val listeners = mutableListOf<PlayerListener>()
    private val tracksMapper = AVPlayerTracksMapper()
    private var currentPlayerItem: AVPlayerItem? = null
    private var isPlayerPlaying = false
    private var timeObserver: Any? = null
    private var endTimeObserver: Any? = null
    private var failedObserver: Any? = null
    private var tracksNotified = false
    private var previousItemStatus: Long = -1

    init {
        setupPeriodicTimeObserver()
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
        avPlayer.pause()
        removeNotificationObservers()
        timeObserver?.let { avPlayer.removeTimeObserver(it) }
        timeObserver = null
        avPlayer.replaceCurrentItemWithPlayerItem(null)
        currentPlayerItem = null
        listeners.clear()
    }

    override fun isPlaying(): Boolean = isPlayerPlaying

    override fun getCurrentLiveOffset(): Long {
        val item = currentPlayerItem ?: return 0L
        val currentSec = CMTimeGetSeconds(item.currentTime())
        val durationSec = CMTimeGetSeconds(item.duration())

        if (currentSec.isNaN() || durationSec.isNaN() || durationSec.isInfinite()) {
            return 0L
        }
        return ((durationSec - currentSec) * 1000).toLong()
    }

    override fun seekToLiveEdge() {
        currentPlayerItem?.seekToDate(NSDate.date())
    }

    override fun addListener(listener: PlayerListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: PlayerListener) {
        listeners.remove(listener)
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
            notifyTracksChanged()
        }
    }

    override fun selectSubtitleTrack(trackId: String?) {
        val item = currentPlayerItem ?: return
        val group = item.asset.mediaSelectionGroupForMediaCharacteristic(AVMediaCharacteristicLegible) ?: return

        if (trackId == null) {
            item.selectMediaOption(null, inMediaSelectionGroup = group)
            notifyTracksChanged()
            return
        }

        @Suppress("UNCHECKED_CAST")
        val options = group.options as List<AVMediaSelectionOption>
        val index = trackId.toIntOrNull() ?: return

        if (index in options.indices) {
            item.selectMediaOption(options[index], inMediaSelectionGroup = group)
            notifyTracksChanged()
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
            listeners.forEach { it.onPlaybackStateChanged(isPlayerPlaying, isBuffering) }
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
                    notifyTracksChanged()
                }
            }
            AVPlayerItemStatusFailed -> {
                val error = item.error
                val errorCode = error?.code?.toInt() ?: -1
                val errorMessage = error?.localizedDescription ?: "Unknown playback error"
                listeners.forEach { it.onError(errorCode, errorMessage) }
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
            listeners.forEach { it.onPlaybackStateChanged(isPlaying = false, isBuffering = false) }
        }

        failedObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemFailedToPlayToEndTimeNotification,
            `object` = item,
            queue = null,
        ) { notification: NSNotification? ->
            @Suppress("UNCHECKED_CAST")
            val error = notification?.userInfo?.get("AVPlayerItemFailedToPlayToEndTimeErrorKey")
            val errorMessage = error?.toString() ?: "Failed to play to end"
            listeners.forEach { it.onError(-1, errorMessage) }
        }
    }

    private fun removeNotificationObservers() {
        endTimeObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        endTimeObserver = null
        failedObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        failedObserver = null
    }

    private fun notifyTracksChanged() {
        val tracksInfo = getTracksInfo()
        listeners.forEach { it.onTracksChanged(tracksInfo) }
    }
}
