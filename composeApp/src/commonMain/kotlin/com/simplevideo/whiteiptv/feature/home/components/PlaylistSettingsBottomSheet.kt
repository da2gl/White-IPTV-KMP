package com.simplevideo.whiteiptv.feature.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistSettingsBottomSheet(
    playlist: PlaylistEntity,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onUpdate: () -> Unit,
    onDelete: () -> Unit,
    onViewUrl: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
    ) {
        Column(
            modifier = Modifier.padding(bottom = 24.dp),
        ) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            ActionRow(icon = Icons.Default.Edit, label = "Rename", onClick = onRename)

            if (!playlist.url.startsWith("file://")) {
                ActionRow(icon = Icons.Default.Refresh, label = "Update Playlist", onClick = onUpdate)
            }

            ActionRow(
                icon = Icons.Default.Delete,
                label = "Delete Playlist",
                onClick = onDelete,
                tint = MaterialTheme.colorScheme.error,
            )

            ActionRow(icon = Icons.Default.Link, label = "View URL", onClick = onViewUrl)
        }
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = tint)
    }
}
