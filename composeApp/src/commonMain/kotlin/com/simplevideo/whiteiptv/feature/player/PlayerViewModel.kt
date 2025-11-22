package com.simplevideo.whiteiptv.feature.player

import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.domain.usecase.GetChannelByIdUseCase
import com.simplevideo.whiteiptv.feature.player.mvi.PlayerAction
import com.simplevideo.whiteiptv.feature.player.mvi.PlayerEvent
import com.simplevideo.whiteiptv.feature.player.mvi.PlayerState
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val channelId: Long,
    private val getChannelById: GetChannelByIdUseCase,
) : BaseViewModel<PlayerState, PlayerAction, PlayerEvent>(
    initialState = PlayerState(),
) {

    init {
        loadChannel()
    }

    private fun loadChannel() {
        viewModelScope.launch {
            try {
                val channel = getChannelById(channelId)
                viewState = if (channel != null) {
                    viewState.copy(
                        channel = channel,
                        isLoading = false,
                    )
                } else {
                    viewState.copy(
                        error = "Channel not found",
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                viewState = viewState.copy(
                    error = e.message ?: "Unknown error",
                    isLoading = false,
                )
            }
        }
    }

    override fun obtainEvent(viewEvent: PlayerEvent) {
        when (viewEvent) {
            is PlayerEvent.OnBackClick -> {
                viewAction = PlayerAction.NavigateBack
            }
            is PlayerEvent.OnScreenTap -> {
                viewState = viewState.copy(controlsVisible = !viewState.controlsVisible)
            }

            is PlayerEvent.OnSeekToLive -> {
                // Handled in PlayerScreen
            }

            is PlayerEvent.OnShowTrackSelection -> {
                viewState = viewState.copy(showTrackSelectionDialog = viewEvent.type)
            }

            is PlayerEvent.OnDismissTrackSelection -> {
                viewState = viewState.copy(showTrackSelectionDialog = null)
            }

            is PlayerEvent.OnSelectAudioTrack -> {
                // Handled in PlayerScreen
                viewState = viewState.copy(showTrackSelectionDialog = null)
            }

            is PlayerEvent.OnSelectSubtitleTrack -> {
                // Handled in PlayerScreen
                viewState = viewState.copy(showTrackSelectionDialog = null)
            }

            is PlayerEvent.OnSelectVideoQuality -> {
                // Handled in PlayerScreen
                viewState = viewState.copy(showTrackSelectionDialog = null)
            }

            is PlayerEvent.OnPlaybackStateChanged -> {
                viewState = viewState.copy(
                    isPlaying = viewEvent.isPlaying,
                    isBuffering = viewEvent.isBuffering,
                )
            }

            is PlayerEvent.OnTracksChanged -> {
                viewState = viewState.copy(tracksInfo = viewEvent.tracksInfo)
            }

            is PlayerEvent.OnPlayerError -> {
                viewState = viewState.copy(error = viewEvent.message)
            }
        }
    }
}
