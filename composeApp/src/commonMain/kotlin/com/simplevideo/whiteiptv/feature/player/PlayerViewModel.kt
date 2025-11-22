package com.simplevideo.whiteiptv.feature.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.domain.usecase.GetAdjacentChannelUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetChannelByIdUseCase
import com.simplevideo.whiteiptv.feature.player.mvi.PlayerAction
import com.simplevideo.whiteiptv.feature.player.mvi.PlayerEvent
import com.simplevideo.whiteiptv.feature.player.mvi.PlayerState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlayerViewModel(
    savedStateHandle: SavedStateHandle,
    private val getChannelById: GetChannelByIdUseCase,
    private val getAdjacentChannel: GetAdjacentChannelUseCase,
) : BaseViewModel<PlayerState, PlayerAction, PlayerEvent>(
    initialState = PlayerState(),
) {
    private val initialChannelId: Long = checkNotNull(savedStateHandle["channelId"]) {
        "channelId is required for PlayerViewModel"
    }

    private val channelIdFlow = MutableStateFlow(initialChannelId)

    init {
        observeChannel()
    }

    private fun observeChannel() {
        channelIdFlow
            .map { channelId ->
                getChannelById(channelId)
            }
            .onEach { channel ->
                viewState = if (channel != null) {
                    viewState.copy(
                        channel = channel,
                        isLoading = false,
                        error = null,
                    )
                } else {
                    viewState.copy(
                        error = "Channel not found",
                        isLoading = false,
                    )
                }
            }
            .catch { e ->
                viewState = viewState.copy(
                    error = e.message ?: "Unknown error",
                    isLoading = false,
                )
            }
            .launchIn(viewModelScope)
    }

    override fun obtainEvent(viewEvent: PlayerEvent) {
        when (viewEvent) {
            is PlayerEvent.OnBackClick -> {
                viewAction = PlayerAction.NavigateBack
            }

            is PlayerEvent.OnScreenTap -> {
                viewState = viewState.copy(controlsVisible = !viewState.controlsVisible)
            }

            is PlayerEvent.OnNextChannel -> navigateToChannel(next = true)

            is PlayerEvent.OnPreviousChannel -> navigateToChannel(next = false)

            is PlayerEvent.OnPlaybackStateChanged -> {
                viewState = viewState.copy(
                    isPlaying = viewEvent.isPlaying,
                    isBuffering = viewEvent.isBuffering,
                )
            }

            is PlayerEvent.OnPlayerError -> {
                viewState = viewState.copy(error = viewEvent.message)
            }

            is PlayerEvent.OnTracksChanged -> {
                viewState = viewState.copy(tracksInfo = viewEvent.tracksInfo)
            }

            is PlayerEvent.OnShowTrackSelection -> {
                viewState = viewState.copy(showTrackSelectionDialog = viewEvent.type)
            }

            is PlayerEvent.OnDismissTrackSelection -> {
                viewState = viewState.copy(showTrackSelectionDialog = null)
            }

            is PlayerEvent.OnSelectAudioTrack -> {
                viewState = viewState.copy(showTrackSelectionDialog = null)
            }

            is PlayerEvent.OnSelectSubtitleTrack -> {
                viewState = viewState.copy(showTrackSelectionDialog = null)
            }

            is PlayerEvent.OnSelectVideoQuality -> {
                viewState = viewState.copy(showTrackSelectionDialog = null)
            }
        }
    }

    private fun navigateToChannel(next: Boolean) {
        val currentChannel = viewState.channel ?: return

        viewModelScope.launch {
            val adjacentChannel = if (next) {
                getAdjacentChannel.getNext(currentChannel.playlistId, currentChannel.id)
            } else {
                getAdjacentChannel.getPrevious(currentChannel.playlistId, currentChannel.id)
            }

            adjacentChannel?.let {
                viewState = viewState.copy(isLoading = true)
                channelIdFlow.value = it.id
            }
        }
    }
}
