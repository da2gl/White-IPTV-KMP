package com.simplevideo.whiteiptv.common.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection

@Composable
fun PlaylistDropdown(
    playlists: List<PlaylistEntity>,
    selection: PlaylistSelection,
    onPlaylistSelected: (PlaylistSelection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedPlaylist = when (selection) {
        is PlaylistSelection.Selected -> playlists.find { it.id == selection.id }
        PlaylistSelection.All -> null
    }

    DropdownSelector(
        label = "Playlist",
        items = playlists,
        selectedItem = selectedPlaylist,
        onItemSelected = { playlist ->
            val newSelection = if (playlist != null) {
                PlaylistSelection.Selected(playlist.id)
            } else {
                PlaylistSelection.All
            }
            onPlaylistSelected(newSelection)
        },
        itemText = { it.name },
        modifier = modifier,
    )
}
