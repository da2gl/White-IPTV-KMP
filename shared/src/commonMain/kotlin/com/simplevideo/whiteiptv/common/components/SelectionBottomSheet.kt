package com.simplevideo.whiteiptv.common.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Generic reusable bottom sheet for single-option selection from a list.
 * Displays a title, a list of options with radio button indicators, and optional leading content per option.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectionBottomSheet(
    title: String,
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    onDismiss: () -> Unit,
    optionLabel: (T) -> String,
    leadingContent: (@Composable (T) -> Unit)? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyColumn(
            modifier = Modifier.padding(bottom = 24.dp),
        ) {
            items(options) { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clickable {
                            onOptionSelected(option)
                            onDismiss()
                        }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (leadingContent != null) {
                        leadingContent(option)
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        text = optionLabel(option),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    RadioButton(
                        selected = option == selectedOption,
                        onClick = {
                            onOptionSelected(option)
                            onDismiss()
                        },
                    )
                }
            }
        }
    }
}
