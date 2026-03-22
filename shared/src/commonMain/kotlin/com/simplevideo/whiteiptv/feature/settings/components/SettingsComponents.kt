package com.simplevideo.whiteiptv.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplevideo.whiteiptv.common.components.isDarkTheme
import com.simplevideo.whiteiptv.platform.FullscreenSheetEffect

private val ItemCardShape = RoundedCornerShape(16.dp)
private val SectionBadgeShape = RoundedCornerShape(10.dp)

private val LightSectionLabel = Color(0xFF6A7282)
private val LightTitleText = Color(0xFF101828)
private val LightSubtitleText = Color(0xFF6A7282)
private val LightCardBorder = Color(0xFFE5E7EB)

@Composable
fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    gradientColors: List<Color> = emptyList(),
) {
    val isDark = isDarkTheme()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null && gradientColors.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(SectionBadgeShape)
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(
            text = title.uppercase(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = if (isDark) Color.White.copy(alpha = 0.5f) else LightSectionLabel,
        )
    }
}

@Composable
fun SettingsItemCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val isDark = isDarkTheme()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(ItemCardShape)
            .then(
                if (isDark) {
                    Modifier
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), ItemCardShape)
                } else {
                    Modifier
                        .background(Color.White)
                        .border(1.dp, LightCardBorder, ItemCardShape)
                },
            ),
    ) {
        content()
    }
}

/**
 * Settings row that opens a rich selection bottom sheet.
 * Displays icon, title, cyan value text, and chevron right.
 */
@Composable
fun <T> SettingsDropdownRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    optionLabel: (T) -> String,
    modifier: Modifier = Modifier,
    sheetSubtitle: String = "Choose your ${title.lowercase()}",
    optionDescription: ((T) -> String)? = null,
    optionIcon: (@Composable (T) -> Unit)? = null,
) {
    var showSheet by remember { mutableStateOf(false) }
    val isDark = isDarkTheme()

    SettingsItemCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable { showSheet = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDark) Color.White.copy(alpha = 0.5f) else LightSubtitleText,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color.White else LightTitleText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = if (isDark) Color.White.copy(alpha = 0.3f) else LightSubtitleText,
                modifier = Modifier.size(16.dp),
            )
        }
    }

    if (showSheet) {
        SettingsSelectionBottomSheet(
            title = title,
            subtitle = sheetSubtitle,
            options = options,
            selectedOption = selectedOption,
            onOptionSelected = {
                onOptionSelected(it)
                showSheet = false
            },
            onDismiss = { showSheet = false },
            optionLabel = optionLabel,
            optionDescription = optionDescription,
            optionIcon = optionIcon,
        )
    }
}

/**
 * Rich bottom sheet for settings selection with cards, icons, descriptions, and cyan checkmark.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SettingsSelectionBottomSheet(
    title: String,
    subtitle: String,
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    onDismiss: () -> Unit,
    optionLabel: (T) -> String,
    optionDescription: ((T) -> String)? = null,
    optionIcon: (@Composable (T) -> Unit)? = null,
) {
    val isDark = isDarkTheme()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = if (isDark) Color(0xFF0F1419) else Color.White,
        dragHandle = {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFD1D5DC),
                        ),
                )
            }
        },
    ) {
        FullscreenSheetEffect()

        // Header: Title + subtitle on left, close button on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else LightTitleText,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.5f) else LightSubtitleText,
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF3F4F6),
                    )
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = if (isDark) Color.White.copy(alpha = 0.5f) else LightSubtitleText,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Option cards
        Column(
            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                SettingsOptionCard(
                    label = optionLabel(option),
                    description = optionDescription?.invoke(option),
                    isSelected = isSelected,
                    isDark = isDark,
                    onClick = { onOptionSelected(option) },
                    icon = if (optionIcon != null) {
                        { optionIcon(option) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingsOptionCard(
    label: String,
    description: String?,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    icon: (@Composable () -> Unit)? = null,
) {
    val primary = MaterialTheme.colorScheme.primary
    val selectedBorderBrush = Brush.linearGradient(
        listOf(primary.copy(alpha = 0.5f), primary.copy(alpha = 0.5f)),
    )
    val selectedBgBrush = Brush.horizontalGradient(
        listOf(primary.copy(alpha = 0.2f), primary.copy(alpha = 0.15f)),
    )

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(
                if (isSelected) {
                    Modifier
                        .background(selectedBgBrush, shape)
                        .border(1.dp, selectedBorderBrush, shape)
                } else if (isDark) {
                    Modifier
                        .background(Color.White.copy(alpha = 0.05f), shape)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), shape)
                } else {
                    Modifier
                        .background(Color(0xFFF9FAFB), shape)
                        .border(1.dp, LightCardBorder, shape)
                },
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else LightTitleText,
                )
                if (description != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color.White.copy(alpha = 0.5f) else LightSubtitleText,
                    )
                }
            }
            if (isSelected) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

/**
 * Action row with icon, title, and chevron right (no value text).
 */
@Composable
fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isDarkTheme()

    SettingsItemCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDark) Color.White.copy(alpha = 0.5f) else LightSubtitleText,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color.White else LightTitleText,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = if (isDark) Color.White.copy(alpha = 0.3f) else LightSubtitleText,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * Info row for App Version: title + value text (gray), no icon, no chevron.
 */
@Composable
fun SettingsInfoRow(
    title: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    val isDark = isDarkTheme()

    SettingsItemCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .then(
                    if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDark) Color.White.copy(alpha = 0.5f) else LightSubtitleText,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color.White else LightTitleText,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (value != null) {
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.5f) else LightSubtitleText,
                )
            } else if (onClick != null) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = if (isDark) Color.White.copy(alpha = 0.3f) else LightSubtitleText,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

/**
 * Switch row with icon, title, subtitle, and trailing toggle switch.
 */
@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isDarkTheme()

    SettingsItemCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 75.dp)
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDark) Color.White.copy(alpha = 0.5f) else LightSubtitleText,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color.White else LightTitleText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.5f) else LightSubtitleText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = if (isDark) Color.White.copy(alpha = 0.1f) else LightCardBorder,
                    uncheckedBorderColor = Color.Transparent,
                ),
            )
        }
    }
}
