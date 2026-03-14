package com.simplevideo.whiteiptv.feature.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.domain.usecase.GetAdjacentChannelUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetChannelByIdUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetCurrentProgramUseCase
import com.simplevideo.whiteiptv.domain.usecase.LoadEpgUseCase
import com.simplevideo.whiteiptv.domain.usecase.RecordWatchEventUseCase
import com.simplevideo.whiteiptv.feature.player.mvi.PlayerAction
import com.simplevideo.whiteiptv.feature.player.mvi.PlayerEvent
import com.simplevideo.whiteiptv.feature.player.mvi.PlayerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Suppress("TooManyFunctions")
class PlayerViewModel(
    savedStateHandle: SavedStateHandle,
    private val getChannelById: GetChannelByIdUseCase,
    private val getAdjacentChannel: GetAdjacentChannelUseCase,
    private val recordWatchEvent: RecordWatchEventUseCase,
    private val loadEpg: LoadEpgUseCase,
    private val getCurrentProgram: GetCurrentProgramUseCase,
) : BaseViewModel<PlayerState, PlayerAction, PlayerEvent>(
    initialState = PlayerState(),
) {
    private val initialChannelId: Long = checkNotNull(savedStateHandle["channelId"]) {
        "channelId is required for PlayerViewModel"
    }

    private val channelIdFlow = MutableStateFlow(initialChannelId)
    private var watchStartTime: Long = 0L
    private var watchTimerJob: Job? = null
    private var epgRefreshJob: Job? = null
    private var sleepTimerJob: Job? = null

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
                    recordInitialWatchEvent(channel.id, channel.playlistId)
                    loadEpgAndFetchProgram(channel.playlistId, channel.tvgId)
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

    private fun recordInitialWatchEvent(channelId: Long, playlistId: Long) {
        watchTimerJob?.cancel()
        watchStartTime = System.now().toEpochMilliseconds()
        viewModelScope.launch {
            recordWatchEvent(channelId, playlistId)
        }
    }

    private fun handlePlaybackStateForWatchTracking(isPlaying: Boolean) {
        if (isPlaying) {
            startWatchTimer()
        } else {
            stopWatchTimer()
        }
    }

    private fun startWatchTimer() {
        watchTimerJob?.cancel()
        watchTimerJob = viewModelScope.launch {
            while (true) {
                delay(WATCH_UPDATE_INTERVAL_MS)
                val channel = viewState.channel ?: continue
                val elapsed = System.now().toEpochMilliseconds() - watchStartTime
                recordWatchEvent(channel.id, channel.playlistId, elapsed)
            }
        }
    }

    private fun stopWatchTimer() {
        watchTimerJob?.cancel()
        watchTimerJob = null
        viewModelScope.launch {
            val channel = viewState.channel ?: return@launch
            val elapsed = System.now().toEpochMilliseconds() - watchStartTime
            recordWatchEvent(channel.id, channel.playlistId, elapsed)
        }
    }

    private fun loadEpgAndFetchProgram(playlistId: Long, tvgId: String?) {
        viewModelScope.launch {
            loadEpg(playlistId)
            fetchCurrentProgram(tvgId)
            startEpgRefreshTimer(tvgId)
        }
    }

    private suspend fun fetchCurrentProgram(tvgId: String?) {
        val (current, next) = getCurrentProgram(tvgId)
        viewState = viewState.copy(
            currentProgram = current,
            nextProgram = next,
        )
    }

    private fun startEpgRefreshTimer(tvgId: String?) {
        epgRefreshJob?.cancel()
        if (tvgId.isNullOrBlank()) return
        epgRefreshJob = viewModelScope.launch {
            while (true) {
                delay(EPG_REFRESH_INTERVAL_MS)
                fetchCurrentProgram(tvgId)
            }
        }
    }

    private fun startSleepTimer(durationMs: Long) {
        sleepTimerJob?.cancel()
        sleepTimerJob = viewModelScope.launch {
            var remaining = durationMs
            viewState = viewState.copy(
                sleepTimerRemainingMs = remaining,
                showSleepTimerSheet = false,
            )
            while (remaining > 0) {
                delay(SLEEP_TIMER_TICK_MS)
                remaining -= SLEEP_TIMER_TICK_MS
                viewState = viewState.copy(sleepTimerRemainingMs = remaining.coerceAtLeast(0))
            }
            viewState = viewState.copy(sleepTimerRemainingMs = null)
            viewAction = PlayerAction.SleepTimerExpired
        }
    }

    private fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        viewState = viewState.copy(
            sleepTimerRemainingMs = null,
            showSleepTimerSheet = false,
        )
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
                handlePlaybackStateForWatchTracking(viewEvent.isPlaying)
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

            is PlayerEvent.OnShowSleepTimer -> {
                viewState = viewState.copy(showSleepTimerSheet = true)
            }

            is PlayerEvent.OnDismissSleepTimer -> {
                viewState = viewState.copy(showSleepTimerSheet = false)
            }

            is PlayerEvent.OnSetSleepTimer -> {
                startSleepTimer(viewEvent.durationMs)
            }

            is PlayerEvent.OnCancelSleepTimer -> {
                cancelSleepTimer()
            }

            is PlayerEvent.OnEnterPip -> {
                // PiP is triggered from the screen via the PiP controller
            }
        }
    }

    private fun navigateToChannel(next: Boolean) {
        val currentChannel = viewState.channel ?: return
        stopWatchTimer()
        epgRefreshJob?.cancel()
        viewState = viewState.copy(currentProgram = null, nextProgram = null)

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

    override fun onCleared() {
        super.onCleared()
        watchTimerJob?.cancel()
        epgRefreshJob?.cancel()
        sleepTimerJob?.cancel()
    }

    companion object {
        private const val WATCH_UPDATE_INTERVAL_MS = 30_000L
        private const val EPG_REFRESH_INTERVAL_MS = 60_000L
        private const val SLEEP_TIMER_TICK_MS = 1_000L
    }
}
