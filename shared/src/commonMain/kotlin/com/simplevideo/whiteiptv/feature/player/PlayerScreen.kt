package com.simplevideo.whiteiptv.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.simplevideo.whiteiptv.feature.player.components.GestureOverlay
import com.simplevideo.whiteiptv.feature.player.components.PlayerControlsOverlay
import com.simplevideo.whiteiptv.feature.player.components.SleepTimerSheet
import com.simplevideo.whiteiptv.feature.player.components.TrackSelectionDialog
import com.simplevideo.whiteiptv.feature.player.mvi.PlayerAction
import com.simplevideo.whiteiptv.feature.player.mvi.PlayerEvent
import com.simplevideo.whiteiptv.feature.player.mvi.PlayerState
import com.simplevideo.whiteiptv.feature.player.mvi.TrackSelectionType
import com.simplevideo.whiteiptv.platform.CastManager
import com.simplevideo.whiteiptv.platform.KeepScreenOn
import com.simplevideo.whiteiptv.platform.PlayerListener
import com.simplevideo.whiteiptv.platform.TracksInfo
import com.simplevideo.whiteiptv.platform.VideoPlayerFactory
import com.simplevideo.whiteiptv.platform.rememberPipController
import com.simplevideo.whiteiptv.platform.rememberSystemControls
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PlayerScreen(
    onNavigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<PlayerViewModel>()
    val state by viewModel.viewStates().collectAsState()
    val action by viewModel.viewActions().collectAsState(initial = null)

    LaunchedEffect(action) {
        when (action) {
            is PlayerAction.NavigateBack -> {
                onNavigateBack()
                viewModel.clearAction()
            }

            is PlayerAction.SleepTimerExpired -> {
                onNavigateBack()
                viewModel.clearAction()
            }

            null -> Unit
        }
    }

    PlayerScreenContent(
        state = state,
        onEvent = { viewModel.obtainEvent(it) },
    )
}

@Composable
private fun PlayerScreenContent(
    state: PlayerState,
    onEvent: (PlayerEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val playerFactory = koinInject<VideoPlayerFactory>()
    val castManager = koinInject<CastManager>()
    val player = remember { playerFactory.createPlayer() }
    val systemControls = rememberSystemControls()
    val pipController = rememberPipController()
    val scope = rememberCoroutineScope()
    var hideJob: Job? = remember { null }

    // Local state for volume and brightness to ensure proper updates
    var currentVolume by remember { mutableFloatStateOf(systemControls.getVolume()) }
    var currentBrightness by remember { mutableFloatStateOf(systemControls.getBrightness()) }

    // Auto-hide controls after 3 seconds
    fun scheduleHideControls() {
        hideJob?.cancel()
        hideJob = scope.launch {
            delay(3000)
            onEvent(PlayerEvent.OnScreenTap) // Toggle off
        }
    }

    // Show controls and schedule auto-hide
    fun showControlsWithAutoHide() {
        if (!state.controlsVisible) {
            onEvent(PlayerEvent.OnScreenTap)
        }
        scheduleHideControls()
    }

    KeepScreenOn()

    DisposableEffect(player) {
        val listener = object : PlayerListener {
            override fun onPlaybackStateChanged(isPlaying: Boolean, isBuffering: Boolean) {
                onEvent(PlayerEvent.OnPlaybackStateChanged(isPlaying, isBuffering))
            }

            override fun onError(errorCode: Int, errorMessage: String) {
                onEvent(PlayerEvent.OnPlayerError(errorCode, errorMessage))
            }

            override fun onTracksChanged(tracksInfo: TracksInfo) {
                onEvent(PlayerEvent.OnTracksChanged(tracksInfo))
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
            systemControls.restoreBrightness()
        }
    }

    LaunchedEffect(state.channel) {
        state.channel?.let {
            player.setMediaSource(
                url = it.url,
                userAgent = it.userAgent,
                referer = it.referer,
            )
            player.play()
            scheduleHideControls()
        }
    }

    // When casting starts, pause local player and send stream to Cast device
    LaunchedEffect(state.isCasting, state.channel) {
        if (state.isCasting && state.channel != null) {
            player.pause()
            castManager.startCasting(
                url = state.channel.url,
                title = state.channel.name,
                logoUrl = state.channel.logoUrl,
            )
        } else if (!state.isCasting && state.channel != null && !state.isLoading) {
            player.play()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // Video surface (always present when channel loaded)
        if (state.channel != null) {
            player.PlayerView(
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Gesture overlay - always available for channel switching
        GestureOverlay(
            currentVolume = currentVolume,
            currentBrightness = currentBrightness,
            onBrightnessChange = {
                currentBrightness = it
                systemControls.setBrightness(it)
                showControlsWithAutoHide()
            },
            onVolumeChange = {
                currentVolume = it
                systemControls.setVolume(it)
                showControlsWithAutoHide()
            },
            onChannelChange = { direction ->
                if (direction > 0) {
                    onEvent(PlayerEvent.OnNextChannel)
                } else {
                    onEvent(PlayerEvent.OnPreviousChannel)
                }
                showControlsWithAutoHide()
            },
            onTap = {
                // Toggle controls visibility
                val wasVisible = state.controlsVisible
                onEvent(PlayerEvent.OnScreenTap)
                if (wasVisible) {
                    hideJob?.cancel()
                } else {
                    scheduleHideControls()
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Loading indicator
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
            )
        }

        // Error message
        if (state.error != null) {
            Text(
                text = state.error,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // Casting overlay
        if (state.isCasting) {
            CastingOverlay(
                channelName = state.channel?.name.orEmpty(),
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // Controls overlay
        if (state.channel != null) {
            PlayerControlsOverlay(
                channelName = state.channel.name,
                isVisible = state.controlsVisible,
                isBuffering = state.isBuffering,
                tracksInfo = state.tracksInfo,
                currentProgram = state.currentProgram,
                nextProgram = state.nextProgram,
                sleepTimerRemainingMs = state.sleepTimerRemainingMs,
                isPipSupported = pipController.isPipSupported(),
                onBackClick = { onEvent(PlayerEvent.OnBackClick) },
                onShowAudioTracks = {
                    onEvent(PlayerEvent.OnShowTrackSelection(TrackSelectionType.AUDIO))
                },
                onShowSubtitles = {
                    onEvent(PlayerEvent.OnShowTrackSelection(TrackSelectionType.SUBTITLE))
                },
                onShowQuality = {
                    onEvent(PlayerEvent.OnShowTrackSelection(TrackSelectionType.QUALITY))
                },
                onShowSleepTimer = { onEvent(PlayerEvent.OnShowSleepTimer) },
                onEnterPip = { pipController.enterPipMode() },
            )
        }

        // Track selection dialog
        state.showTrackSelectionDialog?.let { type ->
            TrackSelectionDialog(
                type = type,
                tracksInfo = state.tracksInfo,
                onDismiss = { onEvent(PlayerEvent.OnDismissTrackSelection) },
                onSelectAudio = { trackId ->
                    player.selectAudioTrack(trackId)
                    onEvent(PlayerEvent.OnSelectAudioTrack(trackId))
                },
                onSelectSubtitle = { trackId ->
                    player.selectSubtitleTrack(trackId)
                    onEvent(PlayerEvent.OnSelectSubtitleTrack(trackId))
                },
                onSelectQuality = { qualityId ->
                    player.selectVideoQuality(qualityId)
                    onEvent(PlayerEvent.OnSelectVideoQuality(qualityId))
                },
            )
        }

        // Sleep timer bottom sheet
        if (state.showSleepTimerSheet) {
            SleepTimerSheet(
                activeTimerRemainingMs = state.sleepTimerRemainingMs,
                onDismiss = { onEvent(PlayerEvent.OnDismissSleepTimer) },
                onSetTimer = { durationMs -> onEvent(PlayerEvent.OnSetSleepTimer(durationMs)) },
                onCancelTimer = { onEvent(PlayerEvent.OnCancelSleepTimer) },
            )
        }
    }
}

@Composable
private fun CastingOverlay(
    channelName: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Cast,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(64.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Casting",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
        )
        if (channelName.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = channelName,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}
