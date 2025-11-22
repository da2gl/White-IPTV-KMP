package com.simplevideo.whiteiptv.feature.player.mvi

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.platform.TracksInfo

data class PlayerState(
    val channel: ChannelEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    // Playback state
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    // UI state
    val controlsVisible: Boolean = true,
    val liveOffsetMs: Long = 0,
    // Track selection
    val tracksInfo: TracksInfo = TracksInfo(),
    val showTrackSelectionDialog: TrackSelectionType? = null,
)

enum class TrackSelectionType {
    AUDIO,
    SUBTITLE,
    QUALITY,
}

sealed interface PlayerEvent {
    data object OnBackClick : PlayerEvent
    data object OnScreenTap : PlayerEvent
    data object OnSeekToLive : PlayerEvent

    // Track selection
    data class OnShowTrackSelection(val type: TrackSelectionType) : PlayerEvent
    data object OnDismissTrackSelection : PlayerEvent
    data class OnSelectAudioTrack(val trackId: String) : PlayerEvent
    data class OnSelectSubtitleTrack(val trackId: String?) : PlayerEvent
    data class OnSelectVideoQuality(val qualityId: String?) : PlayerEvent

    // Playback state updates from player
    data class OnPlaybackStateChanged(val isPlaying: Boolean, val isBuffering: Boolean) : PlayerEvent
    data class OnTracksChanged(val tracksInfo: TracksInfo) : PlayerEvent
    data class OnPlayerError(val errorCode: Int, val message: String) : PlayerEvent
}

sealed interface PlayerAction {
    data object NavigateBack : PlayerAction
}
