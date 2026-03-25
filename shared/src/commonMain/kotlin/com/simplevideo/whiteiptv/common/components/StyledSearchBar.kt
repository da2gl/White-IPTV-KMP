package com.simplevideo.whiteiptv.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SearchBarShape = RoundedCornerShape(16.dp)

@Composable
fun StyledSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search channels...",
    modifier: Modifier = Modifier,
) {
    val hintColor = MaterialTheme.colorScheme.onSurfaceVariant
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(text = placeholder, color = hintColor)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = hintColor,
                modifier = Modifier.fillMaxWidth(0f),
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = hintColor,
                    )
                }
            }
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(SearchBarShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}
