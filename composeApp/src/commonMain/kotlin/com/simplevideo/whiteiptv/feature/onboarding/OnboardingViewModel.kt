package com.simplevideo.whiteiptv.feature.onboarding

import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.domain.exception.PlaylistException
import com.simplevideo.whiteiptv.domain.usecase.ImportPlaylistFromFileUseCase
import com.simplevideo.whiteiptv.domain.usecase.ImportPlaylistUseCase
import com.simplevideo.whiteiptv.feature.onboarding.mvi.OnboardingAction
import com.simplevideo.whiteiptv.feature.onboarding.mvi.OnboardingEvent
import com.simplevideo.whiteiptv.feature.onboarding.mvi.OnboardingState
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val importPlaylist: ImportPlaylistUseCase,
    private val importPlaylistFromFile: ImportPlaylistFromFileUseCase,
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
        importPlaylistFromFile(fileUri, fileName)
    }

    private fun importPlaylistFromFile(fileUri: String, fileName: String) {
        viewModelScope.launch {
            viewState = viewState.copy(isLoading = true, error = null)
            runCatching {
                importPlaylistFromFile.invoke(fileUri, fileName)
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

    private fun handleImportPlaylist() {
        if (viewState.playlistUrl.isNotBlank()) {
            importPlaylistFromUrl(viewState.playlistUrl)
        } else {
            viewState = viewState.copy(error = "Please enter a playlist URL or choose a file")
        }
    }

    private fun importPlaylistFromUrl(url: String) {
        viewModelScope.launch {
            viewState = viewState.copy(isLoading = true, error = null)
            runCatching {
                importPlaylist(url)
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

    private fun handleUseDemoPlaylist() {
        viewState = viewState.copy(isLoading = true, error = null)
        // TODO: Implement demo playlist loading
    }

    private fun validatePlaylistUrl(url: String): Boolean {
        return url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))
    }
}
