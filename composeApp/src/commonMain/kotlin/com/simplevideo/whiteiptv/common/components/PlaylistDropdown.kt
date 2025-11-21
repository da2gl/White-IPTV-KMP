package com.simplevideo.whiteiptv.common.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity

@Composable
fun PlaylistDropdown(
    playlists: List<PlaylistEntity>,
    selectedPlaylistId: Long?,
    onPlaylistSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedPlaylist = playlists.find { it.id == selectedPlaylistId }

    DropdownSelector(
        label = "Playlist",
        items = playlists,
        selectedItem = selectedPlaylist,
        onItemSelected = { playlist -> onPlaylistSelected(playlist?.id) },
        itemText = { it.name },
        modifier = modifier,
    )
}
