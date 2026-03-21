package com.simplevideo.whiteiptv.feature.player.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.simplevideo.whiteiptv.common.LogRecomposition
import com.simplevideo.whiteiptv.common.trackRecomposition
import com.simplevideo.whiteiptv.domain.model.EpgProgram
import com.simplevideo.whiteiptv.feature.player.mvi.TrackSelectionType
import com.simplevideo.whiteiptv.platform.AudioTrackInfo
import com.simplevideo.whiteiptv.platform.StreamingButton
import com.simplevideo.whiteiptv.platform.SubtitleTrackInfo
import com.simplevideo.whiteiptv.platform.TracksInfo
import com.simplevideo.whiteiptv.platform.VideoQualityInfo

private const val ANIMATION_DURATION_MS = 350
private const val LIVE_EDGE_THRESHOLD_MS = 5000L
private const val BUFFERING_PULSE_MIN_ALPHA = 0.6f
private const val BUFFERING_PULSE_DURATION_MS = 1000

@Composable
fun PlayerControlsOverlay(
    channelName: String,
    isVisible: Boolean,
    isBuffering: Boolean,
    tracksInfo: TracksInfo,
    currentProgram: EpgProgram?,
    nextProgram: EpgProgram?,
    sleepTimerRemainingMs: Long?,
    isPipSupported: Boolean,
    channelLogoUrl: String?,
    liveOffsetMs: Long,
    onBackClick: () -> Unit,
    onShowAudioTracks: () -> Unit,
    onShowSubtitles: () -> Unit,
    onShowQuality: () -> Unit,
    onShowSleepTimer: () -> Unit,
    onEnterPip: () -> Unit,
    onSeekToLive: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LogRecomposition("PlayerControlsOverlay")
    Box(modifier = modifier.fillMaxSize().trackRecomposition("PlayerControlsOverlay")) {
        // Buffering indicator - always visible when buffering, independent of controls
        if (isBuffering) {
            PulsingBufferingIndicator(
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // Top bar - slides from top
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(ANIMATION_DURATION_MS)) + slideInVertically(
                animationSpec = tween(ANIMATION_DURATION_MS),
                initialOffsetY = { -it },
            ),
            exit = fadeOut(animationSpec = tween(ANIMATION_DURATION_MS)) + slideOutVertically(
                animationSpec = tween(ANIMATION_DURATION_MS),
                targetOffsetY = { -it },
            ),
        ) {
            PlayerTopBar(
                channelName = channelName,
                channelLogoUrl = channelLogoUrl,
                currentProgram = currentProgram,
                nextProgram = nextProgram,
                onBackClick = onBackClick,
            )
        }

        // Bottom bar - slides from bottom
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(ANIMATION_DURATION_MS)) + slideInVertically(
                animationSpec = tween(ANIMATION_DURATION_MS),
                initialOffsetY = { it },
            ),
            exit = fadeOut(animationSpec = tween(ANIMATION_DURATION_MS)) + slideOutVertically(
                animationSpec = tween(ANIMATION_DURATION_MS),
                targetOffsetY = { it },
            ),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            PlayerBottomBar(
                tracksInfo = tracksInfo,
                sleepTimerRemainingMs = sleepTimerRemainingMs,
                isPipSupported = isPipSupported,
                liveOffsetMs = liveOffsetMs,
                onShowAudioTracks = onShowAudioTracks,
                onShowSubtitles = onShowSubtitles,
                onShowQuality = onShowQuality,
                onShowSleepTimer = onShowSleepTimer,
                onEnterPip = onEnterPip,
                onSeekToLive = onSeekToLive,
            )
        }
    }
}

@Composable
private fun PulsingBufferingIndicator(
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "buffering_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = BUFFERING_PULSE_MIN_ALPHA,
        animationSpec = infiniteRepeatable(
            animation = tween(BUFFERING_PULSE_DURATION_MS),
        ),
        label = "buffering_alpha",
    )
    CircularProgressIndicator(
        modifier = modifier
            .size(56.dp)
            .alpha(alpha),
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = 4.dp,
    )
}

@Composable
private fun PlayerTopBar(
    channelName: String,
    channelLogoUrl: String?,
    currentProgram: EpgProgram?,
    nextProgram: EpgProgram?,
    onBackClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.7f),
                        Color.Transparent,
                    ),
                ),
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (channelLogoUrl != null) {
                    AsyncImage(
                        model = channelLogoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = channelName,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (currentProgram != null) {
                EpgProgramInfo(
                    currentProgram = currentProgram,
                    nextProgram = nextProgram,
                    modifier = Modifier.padding(start = 56.dp),
                )
            }
        }
    }
}

@Composable
private fun PlayerBottomBar(
    tracksInfo: TracksInfo,
    sleepTimerRemainingMs: Long?,
    isPipSupported: Boolean,
    liveOffsetMs: Long,
    onShowAudioTracks: () -> Unit,
    onShowSubtitles: () -> Unit,
    onShowQuality: () -> Unit,
    onShowSleepTimer: () -> Unit,
    onEnterPip: () -> Unit,
    onSeekToLive: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.7f),
                    ),
                ),
            )
            .padding(horizontal = 8.dp, vertical = 12.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Live indicator row
            if (liveOffsetMs > 0) {
                LiveIndicator(
                    liveOffsetMs = liveOffsetMs,
                    onSeekToLive = onSeekToLive,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                )
            }

            // Sleep timer remaining
            if (sleepTimerRemainingMs != null) {
                Text(
                    text = formatRemainingTime(sleepTimerRemainingMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.padding(start = 12.dp, bottom = 4.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LabeledControlButton(
                    icon = Icons.Default.Timer,
                    label = "Sleep",
                    onClick = onShowSleepTimer,
                    tint = if (sleepTimerRemainingMs != null) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.White
                    },
                )
                if (tracksInfo.audioTracks.size > 1) {
                    LabeledControlButton(
                        icon = Icons.Default.Audiotrack,
                        label = "Audio",
                        onClick = onShowAudioTracks,
                    )
                }
                if (tracksInfo.subtitleTracks.isNotEmpty()) {
                    LabeledControlButton(
                        icon = Icons.Default.Subtitles,
                        label = "Subs",
                        onClick = onShowSubtitles,
                    )
                }
                if (tracksInfo.videoQualities.size > 1) {
                    LabeledControlButton(
                        icon = Icons.Default.HighQuality,
                        label = "Quality",
                        onClick = onShowQuality,
                    )
                }
                if (isPipSupported) {
                    LabeledControlButton(
                        icon = Icons.Default.PictureInPicture,
                        label = "PiP",
                        onClick = onEnterPip,
                    )
                }
                StreamingButton()
            }
        }
    }
}

@Composable
private fun LabeledControlButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(32.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
        )
    }
}

@Composable
private fun LiveIndicator(
    liveOffsetMs: Long,
    onSeekToLive: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (liveOffsetMs in 1..LIVE_EDGE_THRESHOLD_MS) {
        // At live edge - show red dot + LIVE
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier,
        ) {
            Icon(
                imageVector = Icons.Default.FiberManualRecord,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(8.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "LIVE",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
            )
        }
    } else if (liveOffsetMs > LIVE_EDGE_THRESHOLD_MS) {
        // Behind live edge - show "Go to Live" button
        Text(
            text = "Go to Live",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Red,
            fontWeight = FontWeight.Bold,
            modifier = modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable(onClick = onSeekToLive)
                .padding(4.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackSelectionDialog(
    type: TrackSelectionType,
    tracksInfo: TracksInfo,
    onDismiss: () -> Unit,
    onSelectAudio: (String) -> Unit,
    onSelectSubtitle: (String?) -> Unit,
    onSelectQuality: (String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = when (type) {
                    TrackSelectionType.AUDIO -> "Audio Track"
                    TrackSelectionType.SUBTITLE -> "Subtitles"
                    TrackSelectionType.QUALITY -> "Quality"
                },
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            when (type) {
                TrackSelectionType.AUDIO -> {
                    AudioTrackList(
                        tracks = tracksInfo.audioTracks,
                        onSelect = onSelectAudio,
                    )
                }

                TrackSelectionType.SUBTITLE -> {
                    SubtitleTrackList(
                        tracks = tracksInfo.subtitleTracks,
                        onSelect = onSelectSubtitle,
                    )
                }

                TrackSelectionType.QUALITY -> {
                    QualityList(
                        qualities = tracksInfo.videoQualities,
                        onSelect = onSelectQuality,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AudioTrackList(
    tracks: List<AudioTrackInfo>,
    onSelect: (String) -> Unit,
) {
    LazyColumn {
        items(tracks) { track ->
            TrackItem(
                label = track.label,
                subtitle = track.language?.let { "Language: $it" },
                isSelected = track.isSelected,
                onClick = { onSelect(track.id) },
            )
        }
    }
}

@Composable
private fun SubtitleTrackList(
    tracks: List<SubtitleTrackInfo>,
    onSelect: (String?) -> Unit,
) {
    LazyColumn {
        // Off option
        item {
            TrackItem(
                label = "Off",
                subtitle = null,
                isSelected = tracks.none { it.isSelected },
                onClick = { onSelect(null) },
            )
        }
        items(tracks) { track ->
            TrackItem(
                label = track.label,
                subtitle = track.language?.let { "Language: $it" },
                isSelected = track.isSelected,
                onClick = { onSelect(track.id) },
            )
        }
    }
}

@Composable
private fun QualityList(
    qualities: List<VideoQualityInfo>,
    onSelect: (String?) -> Unit,
) {
    LazyColumn {
        items(qualities) { quality ->
            TrackItem(
                label = quality.label,
                subtitle = if (quality.bitrate > 0) {
                    "${quality.bitrate / 1000} kbps"
                } else {
                    null
                },
                isSelected = quality.isSelected,
                onClick = {
                    onSelect(if (quality.isAuto) null else quality.id)
                },
            )
        }
    }
}

@Composable
private fun TrackItem(
    label: String,
    subtitle: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
