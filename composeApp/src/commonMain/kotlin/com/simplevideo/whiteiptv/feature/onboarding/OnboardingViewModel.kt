package com.simplevideo.whiteiptv.feature.onboarding

import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.domain.exception.PlaylistException
import com.simplevideo.whiteiptv.domain.usecase.ImportPlaylistUseCase
import com.simplevideo.whiteiptv.feature.onboarding.mvi.OnboardingAction
import com.simplevideo.whiteiptv.feature.onboarding.mvi.OnboardingEvent
import com.simplevideo.whiteiptv.feature.onboarding.mvi.OnboardingState
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val importPlaylistUseCase: ImportPlaylistUseCase,
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
        val isValid = validatePlaylistUrl(url)
        viewState = viewState.copy(
            playlistUrl = url,
            isValidUrl = isValid,
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
    }

    private fun handleImportPlaylist() {
        if (viewState.playlistUrl.isNotBlank()) {
            importPlaylistFromUrl(viewState.playlistUrl)
        } else {
            viewState = viewState.copy(error = "Please enter a playlist URL or choose a file")
            viewAction = OnboardingAction.ShowError("Please enter a playlist URL or choose a file")
        }
    }

    private fun importPlaylistFromUrl(url: String) {
        viewModelScope.launch {
            viewState = viewState.copy(isLoading = true, error = null)
            try {
                importPlaylistUseCase(url)
                viewState = viewState.copy(isLoading = false)
                viewAction = OnboardingAction.NavigateToMain
            } catch (e: PlaylistException) {
                val errorMessage = e.message ?: "Unknown error occurred"
                viewState = viewState.copy(isLoading = false, error = errorMessage)
                viewAction = OnboardingAction.ShowError(errorMessage)
            } catch (e: Exception) {
                val errorMessage = "Unexpected error: ${e.message ?: "Unknown error"}"
                viewState = viewState.copy(isLoading = false, error = errorMessage)
                viewAction = OnboardingAction.ShowError(errorMessage)
            }
        }
    }

    private fun handleUseDemoPlaylist() {
        viewState = viewState.copy(isLoading = true, error = null)
        // TODO: Implement demo playlist loading
    }

    private fun validatePlaylistUrl(url: String): Boolean {
        if (url.isBlank()) return false
        return url.startsWith("http://") || url.startsWith("https://")
    }
}
