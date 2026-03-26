package com.simplevideo.whiteiptv.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.simplevideo.whiteiptv.designsystem.CyanGradientEnd
import com.simplevideo.whiteiptv.designsystem.CyanGradientStart
import com.simplevideo.whiteiptv.domain.model.ChannelGroup

private val ChipShape = RoundedCornerShape(12.dp)

@Composable
fun GroupFilterChips(
    groups: List<ChannelGroup>,
    selectedGroup: ChannelGroup?,
    onGroupSelect: (ChannelGroup?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedGroup, groups) {
        if (selectedGroup != null) {
            val index = groups.indexOf(selectedGroup)
            if (index >= 0) {
                listState.animateScrollToItem(index + 1) // +1 for "All" chip
            }
        } else {
            listState.animateScrollToItem(0)
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        item {
            FilterPillChip(
                text = "All",
                isSelected = selectedGroup == null,
                onClick = { onGroupSelect(null) },
            )
        }
        items(groups, key = { it.id }) { group ->
            FilterPillChip(
                text = group.displayName,
                isSelected = group == selectedGroup,
                onClick = { onGroupSelect(group) },
            )
        }
    }
}

@Composable
private fun FilterPillChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(ChipShape)
            .then(
                if (isSelected) {
                    Modifier.background(Brush.horizontalGradient(listOf(CyanGradientStart, CyanGradientEnd)))
                } else {
                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
