package com.simplevideo.whiteiptv.feature.player.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.simplevideo.whiteiptv.domain.model.EpgProgram
import com.simplevideo.whiteiptv.feature.player.mvi.TrackSelectionType
import com.simplevideo.whiteiptv.platform.AudioTrackInfo
import com.simplevideo.whiteiptv.platform.SubtitleTrackInfo
import com.simplevideo.whiteiptv.platform.TracksInfo
import com.simplevideo.whiteiptv.platform.VideoQualityInfo

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
    onBackClick: () -> Unit,
    onShowAudioTracks: () -> Unit,
    onShowSubtitles: () -> Unit,
    onShowQuality: () -> Unit,
    onShowSleepTimer: () -> Unit,
    onEnterPip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
            )
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                PlayerTopBar(
                    channelName = channelName,
                    currentProgram = currentProgram,
                    nextProgram = nextProgram,
                    onBackClick = onBackClick,
                )
                PlayerBottomBar(
                    tracksInfo = tracksInfo,
                    sleepTimerRemainingMs = sleepTimerRemainingMs,
                    isPipSupported = isPipSupported,
                    onShowAudioTracks = onShowAudioTracks,
                    onShowSubtitles = onShowSubtitles,
                    onShowQuality = onShowQuality,
                    onShowSleepTimer = onShowSleepTimer,
                    onEnterPip = onEnterPip,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

@Composable
private fun PlayerTopBar(
    channelName: String,
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
            .padding(8.dp),
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
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = channelName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
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
    onShowAudioTracks: () -> Unit,
    onShowSubtitles: () -> Unit,
    onShowQuality: () -> Unit,
    onShowSleepTimer: () -> Unit,
    onEnterPip: () -> Unit,
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
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (sleepTimerRemainingMs != null) {
                Text(
                    text = formatRemainingTime(sleepTimerRemainingMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(onClick = onShowSleepTimer) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Sleep timer",
                        tint = if (sleepTimerRemainingMs != null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.White
                        },
                    )
                }
                if (tracksInfo.audioTracks.size > 1) {
                    IconButton(onClick = onShowAudioTracks) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = "Audio tracks",
                            tint = Color.White,
                        )
                    }
                }
                if (tracksInfo.subtitleTracks.isNotEmpty()) {
                    IconButton(onClick = onShowSubtitles) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = "Subtitles",
                            tint = Color.White,
                        )
                    }
                }
                if (tracksInfo.videoQualities.size > 1) {
                    IconButton(onClick = onShowQuality) {
                        Icon(
                            imageVector = Icons.Default.HighQuality,
                            contentDescription = "Quality",
                            tint = Color.White,
                        )
                    }
                }
                if (isPipSupported) {
                    IconButton(onClick = onEnterPip) {
                        Icon(
                            imageVector = Icons.Default.PictureInPicture,
                            contentDescription = "Picture in Picture",
                            tint = Color.White,
                        )
                    }
                }
            }
        }
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
