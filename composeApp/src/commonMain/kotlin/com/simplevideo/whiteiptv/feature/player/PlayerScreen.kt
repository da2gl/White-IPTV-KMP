package com.simplevideo.whiteiptv.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.simplevideo.whiteiptv.feature.player.components.PlayerControlsOverlay
import com.simplevideo.whiteiptv.feature.player.components.TrackSelectionDialog
import com.simplevideo.whiteiptv.feature.player.mvi.PlayerAction
import com.simplevideo.whiteiptv.feature.player.mvi.PlayerEvent
import com.simplevideo.whiteiptv.feature.player.mvi.PlayerState
import com.simplevideo.whiteiptv.feature.player.mvi.TrackSelectionType
import com.simplevideo.whiteiptv.platform.KeepScreenOn
import com.simplevideo.whiteiptv.platform.PlayerListener
import com.simplevideo.whiteiptv.platform.TracksInfo
import com.simplevideo.whiteiptv.platform.VideoPlayerFactory
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PlayerScreen(
    channelId: Long,
    onNavigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<PlayerViewModel> { parametersOf(channelId) }
    val state by viewModel.viewStates().collectAsState()
    val action by viewModel.viewActions().collectAsState(initial = null)

    LaunchedEffect(action) {
        when (action) {
            is PlayerAction.NavigateBack -> {
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
    val player = remember { playerFactory.createPlayer() }

    // Keep screen on during playback
    KeepScreenOn()

    // Set up player listener
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
        }
    }

    // Load media when channel changes
    LaunchedEffect(state.channel) {
        state.channel?.let {
            player.setMediaSource(
                url = it.url,
                userAgent = it.userAgent,
                referer = it.referer,
            )
            player.play()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) {
                onEvent(PlayerEvent.OnScreenTap)
            },
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                )
            }

            state.error != null -> {
                Text(
                    text = state.error,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            state.channel != null -> {
                // Video surface
                player.PlayerView(
                    modifier = Modifier.fillMaxSize(),
                )

                // Controls overlay
                PlayerControlsOverlay(
                    channelName = state.channel.name,
                    isVisible = state.controlsVisible,
                    isBuffering = state.isBuffering,
                    liveOffsetMs = player.getCurrentLiveOffset(),
                    tracksInfo = state.tracksInfo,
                    onBackClick = { onEvent(PlayerEvent.OnBackClick) },
                    onSeekToLive = {
                        player.seekToLiveEdge()
                        onEvent(PlayerEvent.OnSeekToLive)
                    },
                    onShowAudioTracks = {
                        onEvent(PlayerEvent.OnShowTrackSelection(TrackSelectionType.AUDIO))
                    },
                    onShowSubtitles = {
                        onEvent(PlayerEvent.OnShowTrackSelection(TrackSelectionType.SUBTITLE))
                    },
                    onShowQuality = {
                        onEvent(PlayerEvent.OnShowTrackSelection(TrackSelectionType.QUALITY))
                    },
                )
            }
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
    }
}
