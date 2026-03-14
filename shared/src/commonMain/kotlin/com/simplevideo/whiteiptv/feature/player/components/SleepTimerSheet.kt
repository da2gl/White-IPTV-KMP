package com.simplevideo.whiteiptv.feature.player.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val SLEEP_TIMER_PRESETS = listOf(
    "15 min" to 15L * 60 * 1000,
    "30 min" to 30L * 60 * 1000,
    "45 min" to 45L * 60 * 1000,
    "1 hour" to 60L * 60 * 1000,
    "2 hours" to 120L * 60 * 1000,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerSheet(
    activeTimerRemainingMs: Long?,
    onDismiss: () -> Unit,
    onSetTimer: (durationMs: Long) -> Unit,
    onCancelTimer: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
    ) {
        Column(
            modifier = Modifier.padding(bottom = 24.dp),
        ) {
            Text(
                text = "Sleep Timer",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (activeTimerRemainingMs != null) {
                val remainingText = formatRemainingTime(activeTimerRemainingMs)
                Text(
                    text = "Timer active: $remainingText remaining",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onCancelTimer)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Cancel timer",
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Cancel Timer",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            SLEEP_TIMER_PRESETS.forEach { (label, durationMs) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSetTimer(durationMs) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = label,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

internal fun formatRemainingTime(remainingMs: Long): String {
    val totalSeconds = remainingMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val secStr = if (seconds < 10) "0$seconds" else "$seconds"
    return "$minutes:$secStr"
}
