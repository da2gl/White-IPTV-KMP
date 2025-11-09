package com.simplevideo.whiteiptv.feature.onboarding

import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val playlistRepository: PlaylistRepository
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
            is OnboardingEvent.DismissError -> handleDismissError()
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
        // TODO: Validate file format
        // TODO: Store file URI for import
        viewState = viewState.copy(
            playlistFileName = fileName,
            playlistUrl = "", // Clear URL when file is selected
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
                playlistRepository.importPlaylistFromUrl(url)
                viewAction = OnboardingAction.NavigateToMain
            } catch (e: Exception) {
                viewState = viewState.copy(isLoading = false, error = "Failed to import playlist")
            }
        }
    }

    private fun handleUseDemoPlaylist() {
        // TODO: Load demo playlist
        // TODO: Save demo playlist to database
        // TODO: Navigate to main screen

        viewState = viewState.copy(isLoading = true, error = null)

        // TODO: Actual demo playlist loading
        // viewAction = OnboardingAction.NavigateToMain
    }

    private fun handleDismissError() {
        viewState = viewState.copy(error = null)
    }

    private fun validatePlaylistUrl(url: String): Boolean {
        if (url.isBlank()) return false
        return url.startsWith("http://") || url.startsWith("https://")
    }
}
