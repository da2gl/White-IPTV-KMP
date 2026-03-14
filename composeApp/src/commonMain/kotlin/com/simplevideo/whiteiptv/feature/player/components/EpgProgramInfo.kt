package com.simplevideo.whiteiptv.feature.player.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.simplevideo.whiteiptv.domain.model.EpgProgram
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@Composable
fun EpgProgramInfo(
    currentProgram: EpgProgram,
    nextProgram: EpgProgram?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = currentProgram.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatTimeRange(currentProgram.startTimeMs, currentProgram.endTimeMs),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
        if (nextProgram != null) {
            Text(
                text = "Next: ${nextProgram.title} · ${formatTime(nextProgram.startTimeMs)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun formatTimeRange(startMs: Long, endMs: Long): String {
    return "${formatTime(startMs)} – ${formatTime(endMs)}"
}

@OptIn(ExperimentalTime::class)
private fun formatTime(epochMs: Long): String {
    val instant = kotlin.time.Instant.fromEpochMilliseconds(epochMs)
    val localTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localTime.hour.toString().padStart(2, '0')}:${localTime.minute.toString().padStart(2, '0')}"
}
