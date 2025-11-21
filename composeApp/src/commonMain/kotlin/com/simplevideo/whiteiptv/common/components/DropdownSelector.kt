package com.simplevideo.whiteiptv.common.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> DropdownSelector(
    label: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T?) -> Unit,
    itemText: (T) -> String,
    modifier: Modifier = Modifier,
    allItemLabel: String = "All",
    showAllOption: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = selectedItem?.let { itemText(it) } ?: allItemLabel

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedCard(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    text = "$label: $selectedText",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterStart),
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select $label",
                    modifier = Modifier.align(Alignment.CenterEnd),
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f),
        ) {
            if (showAllOption) {
                DropdownMenuItem(
                    text = { Text(allItemLabel) },
                    onClick = {
                        onItemSelected(null)
                        expanded = false
                    },
                )
            }
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemText(item)) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                )
            }
        }
    }
}
