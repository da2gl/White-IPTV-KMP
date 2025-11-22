package com.simplevideo.whiteiptv.feature.player.mvi

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.platform.TracksInfo

data class PlayerState(
    val channel: ChannelEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val controlsVisible: Boolean = true,
    val tracksInfo: TracksInfo = TracksInfo(),
    val showTrackSelectionDialog: TrackSelectionType? = null,
)

sealed interface PlayerEvent {
    data object OnBackClick : PlayerEvent
    data object OnScreenTap : PlayerEvent
    data object OnNextChannel : PlayerEvent
    data object OnPreviousChannel : PlayerEvent
    data class OnPlaybackStateChanged(val isPlaying: Boolean, val isBuffering: Boolean) : PlayerEvent
    data class OnPlayerError(val errorCode: Int, val message: String) : PlayerEvent
    data class OnTracksChanged(val tracksInfo: TracksInfo) : PlayerEvent
    data class OnShowTrackSelection(val type: TrackSelectionType) : PlayerEvent
    data object OnDismissTrackSelection : PlayerEvent
    data class OnSelectAudioTrack(val trackId: String) : PlayerEvent
    data class OnSelectSubtitleTrack(val trackId: String?) : PlayerEvent
    data class OnSelectVideoQuality(val qualityId: String?) : PlayerEvent
}

sealed interface PlayerAction {
    data object NavigateBack : PlayerAction
}

enum class TrackSelectionType {
    AUDIO,
    SUBTITLE,
    QUALITY,
}
