package com.simplevideo.whiteiptv.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDropdown(
    playlists: List<PlaylistEntity>,
    selection: PlaylistSelection,
    onPlaylistSelect: (PlaylistSelection) -> Unit,
    onAddPlaylistClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var showSheet by remember { mutableStateOf(false) }
    val selectedText = when (selection) {
        is PlaylistSelection.Selected -> playlists.find { it.id == selection.id }?.name ?: "All Playlists"
        PlaylistSelection.All -> "All Playlists"
    }

    Row(
        modifier = modifier
            .clickable { showSheet = true }
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
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = rememberModalBottomSheetState(),
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        ) {
            Text(
                text = "Select Playlist",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyColumn(
                modifier = Modifier.padding(bottom = 24.dp),
            ) {
                item {
                    PlaylistOptionRow(
                        label = "All Playlists",
                        isSelected = selection is PlaylistSelection.All,
                        onClick = {
                            onPlaylistSelect(PlaylistSelection.All)
                            showSheet = false
                        },
                    )
                }
                items(playlists) { playlist ->
                    PlaylistOptionRow(
                        label = playlist.name,
                        isSelected = selection is PlaylistSelection.Selected && selection.id == playlist.id,
                        onClick = {
                            onPlaylistSelect(PlaylistSelection.Selected(playlist.id))
                            showSheet = false
                        },
                    )
                }
                if (onAddPlaylistClick != null) {
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .clickable {
                                    onAddPlaylistClick()
                                    showSheet = false
                                }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Add new playlist",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistOptionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        RadioButton(
            selected = isSelected,
            onClick = onClick,
        )
    }
}
