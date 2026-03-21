package com.simplevideo.whiteiptv.feature.player.mvi

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.domain.model.EpgProgram
import com.simplevideo.whiteiptv.platform.CastConnectionState
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
    val currentProgram: EpgProgram? = null,
    val nextProgram: EpgProgram? = null,
    val sleepTimerRemainingMs: Long? = null,
    val showSleepTimerSheet: Boolean = false,
    val isInPipMode: Boolean = false,
    val isCasting: Boolean = false,
    val nextChannelName: String? = null,
    val previousChannelName: String? = null,
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
    data object OnShowSleepTimer : PlayerEvent
    data object OnDismissSleepTimer : PlayerEvent
    data class OnSetSleepTimer(val durationMs: Long) : PlayerEvent
    data object OnCancelSleepTimer : PlayerEvent
    data object OnEnterPip : PlayerEvent
    data class OnCastStateChanged(val state: CastConnectionState) : PlayerEvent
}

sealed interface PlayerAction {
    data object NavigateBack : PlayerAction
    data object SleepTimerExpired : PlayerAction
}

enum class TrackSelectionType {
    AUDIO,
    SUBTITLE,
    QUALITY,
}
