package com.simplevideo.whiteiptv.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.simplevideo.whiteiptv.domain.model.ChannelGroup

@Composable
fun GroupFilterChips(
    groups: List<ChannelGroup>,
    selectedGroup: ChannelGroup?,
    onGroupSelect: (ChannelGroup?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        item {
            FilterChip(
                selected = selectedGroup == null,
                onClick = { onGroupSelect(null) },
                label = { Text("All") },
                leadingIcon = if (selectedGroup == null) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                } else {
                    null
                },
            )
        }
        items(groups) { group ->
            FilterChip(
                selected = group == selectedGroup,
                onClick = { onGroupSelect(group) },
                label = {
                    Text(
                        text = group.displayName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                leadingIcon = if (group == selectedGroup) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}
