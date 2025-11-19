package com.simplevideo.whiteiptv.common.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.simplevideo.whiteiptv.domain.model.ChannelCategory

@Composable
fun CategoryDropdown(
    categories: List<ChannelCategory>,
    selectedCategory: ChannelCategory,
    onCategorySelected: (ChannelCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownSelector(
        label = "Category",
        items = categories,
        selectedItem = selectedCategory,
        onItemSelected = { category -> category?.let { onCategorySelected(it) } },
        itemText = { it.displayName },
        modifier = modifier,
        showAllOption = false,
    )
}
