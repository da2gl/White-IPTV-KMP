package com.simplevideo.whiteiptv.feature.onboarding

import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.domain.exception.PlaylistException
import com.simplevideo.whiteiptv.domain.model.PlaylistSource
import com.simplevideo.whiteiptv.domain.usecase.ImportPlaylistUseCase
import com.simplevideo.whiteiptv.feature.onboarding.mvi.OnboardingAction
import com.simplevideo.whiteiptv.feature.onboarding.mvi.OnboardingEvent
import com.simplevideo.whiteiptv.feature.onboarding.mvi.OnboardingState
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val importPlaylist: ImportPlaylistUseCase,
) : BaseViewModel<OnboardingState, OnboardingAction, OnboardingEvent>(
    initialState = OnboardingState(),
) {

    override fun obtainEvent(viewEvent: OnboardingEvent) {
        when (viewEvent) {
            is OnboardingEvent.EnterPlaylistUrl -> handleEnterPlaylistUrl(viewEvent.url)
            is OnboardingEvent.ChooseFile -> handleChooseFile()
            is OnboardingEvent.FileSelected -> handleFileSelected(viewEvent.fileName, viewEvent.fileUri)
            is OnboardingEvent.ImportPlaylist -> handleImportPlaylist()
            is OnboardingEvent.UseDemoPlaylist -> handleUseDemoPlaylist()
        }
    }

    private fun handleEnterPlaylistUrl(url: String) {
        viewState = viewState.copy(
            playlistUrl = url,
            isValidUrl = validatePlaylistUrl(url),
            error = null,
        )
    }

    private fun handleChooseFile() {
        viewAction = OnboardingAction.ShowFilePicker
    }

    private fun handleFileSelected(fileName: String, fileUri: String) {
        viewState = viewState.copy(
            playlistFileName = fileName,
            playlistUrl = "",
            error = null,
        )
        importPlaylistFromSource(PlaylistSource.LocalFile(uri = fileUri, fileName = fileName))
    }

    private fun handleImportPlaylist() {
        importPlaylistFromSource(PlaylistSource.Url(url = viewState.playlistUrl))
    }

    private fun handleUseDemoPlaylist() {
        importPlaylistFromSource(PlaylistSource.Url(url = "https://iptv-org.github.io/iptv/index.m3u"))
    }

    private fun importPlaylistFromSource(source: PlaylistSource) {
        viewModelScope.launch {
            viewState = viewState.copy(isLoading = true, error = null)
            runCatching {
                importPlaylist(source)
            }.onSuccess {
                viewState = viewState.copy(isLoading = false)
                viewAction = OnboardingAction.NavigateToMain
            }.onFailure { e ->
                val errorMessage = when (e) {
                    is PlaylistException -> e.message ?: "Unknown error occurred"
                    else -> "Unexpected error: ${e.message ?: "Unknown error"}"
                }
                viewState = viewState.copy(isLoading = false, error = errorMessage)
            }
        }
    }

    private fun validatePlaylistUrl(url: String): Boolean {
        return url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))
    }
}
