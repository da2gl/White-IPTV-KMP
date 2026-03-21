package com.simplevideo.whiteiptv.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection

@Composable
fun PlaylistDropdown(
    playlists: List<PlaylistEntity>,
    selection: PlaylistSelection,
    onPlaylistSelect: (PlaylistSelection) -> Unit,
    onAddPlaylistClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = when (selection) {
        is PlaylistSelection.Selected -> playlists.find { it.id == selection.id }?.name ?: "All Playlists"
        PlaylistSelection.All -> "All Playlists"
    }

    Row(
        modifier = modifier
            .clickable { expanded = true }
            .widthIn(max = 200.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = selectedText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Select Playlist",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("All Playlists") },
                onClick = {
                    onPlaylistSelect(PlaylistSelection.All)
                    expanded = false
                },
                trailingIcon = if (selection is PlaylistSelection.All) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                } else {
                    null
                },
            )

            playlists.forEach { playlist ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = playlist.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        onPlaylistSelect(PlaylistSelection.Selected(playlist.id))
                        expanded = false
                    },
                    trailingIcon = if (selection is PlaylistSelection.Selected && selection.id == playlist.id) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    } else {
                        null
                    },
                )
            }

            if (onAddPlaylistClick != null) {
                HorizontalDivider()
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Add new playlist",
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    onClick = {
                        onAddPlaylistClick()
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                )
            }
        }
    }
}
