package com.simplevideo.whiteiptv.common.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.simplevideo.whiteiptv.domain.model.ChannelGroup

@Composable
fun GroupDropdown(
    groups: List<ChannelGroup>,
    selectedGroup: ChannelGroup?,
    onGroupSelect: (ChannelGroup?) -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownSelector(
        label = "Group",
        items = groups,
        selectedItem = selectedGroup,
        onItemSelect = { group -> onGroupSelect(group) },
        itemText = { it.displayName },
        modifier = modifier,
        allItemLabel = "All",
        showAllOption = true,
    )
}
