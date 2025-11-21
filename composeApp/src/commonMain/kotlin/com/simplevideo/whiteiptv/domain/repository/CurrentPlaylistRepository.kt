package com.simplevideo.whiteiptv.domain.repository

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
    private val _selectedPlaylistId = MutableStateFlow<Long?>(null)
    val selectedPlaylistId: StateFlow<Long?> = _selectedPlaylistId.asStateFlow()

    fun selectPlaylist(id: Long?) {
        _selectedPlaylistId.value = id
    }

    fun clear() {
        _selectedPlaylistId.value = null
    }
}
