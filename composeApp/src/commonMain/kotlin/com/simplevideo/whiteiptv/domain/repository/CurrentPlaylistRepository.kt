package com.simplevideo.whiteiptv.domain.repository

import com.simplevideo.whiteiptv.domain.model.PlaylistSelection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared state for current playlist selection across screens
 *
 * Provides single source of truth for which playlist is currently selected.
 * Used by HomeScreen and ChannelsScreen to sync playlist filter.
 */
class CurrentPlaylistRepository {
    private val _selection = MutableStateFlow<PlaylistSelection>(PlaylistSelection.All)
    val selection: StateFlow<PlaylistSelection> = _selection.asStateFlow()

    fun select(selection: PlaylistSelection) {
        _selection.value = selection
    }
}
