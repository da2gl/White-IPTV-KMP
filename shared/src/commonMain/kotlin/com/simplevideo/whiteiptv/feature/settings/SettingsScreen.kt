package com.simplevideo.whiteiptv.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.simplevideo.whiteiptv.designsystem.SettingsAboutGradient
import com.simplevideo.whiteiptv.designsystem.SettingsAppBehaviorGradient
import com.simplevideo.whiteiptv.designsystem.SettingsAppearanceGradient
import com.simplevideo.whiteiptv.designsystem.SettingsDataStorageGradient
import com.simplevideo.whiteiptv.designsystem.SettingsPlaybackGradient
import com.simplevideo.whiteiptv.designsystem.SettingsPreferencesGradient
import com.simplevideo.whiteiptv.domain.model.AccentColor
import com.simplevideo.whiteiptv.domain.model.ChannelViewMode
import com.simplevideo.whiteiptv.domain.model.ThemeMode
import com.simplevideo.whiteiptv.feature.settings.components.SettingsActionRow
import com.simplevideo.whiteiptv.feature.settings.components.SettingsDropdownRow
import com.simplevideo.whiteiptv.feature.settings.components.SettingsInfoRow
import com.simplevideo.whiteiptv.feature.settings.components.SettingsSectionHeader
import com.simplevideo.whiteiptv.feature.settings.components.SettingsSwitchRow
import com.simplevideo.whiteiptv.feature.settings.mvi.SettingsAction
import com.simplevideo.whiteiptv.feature.settings.mvi.SettingsEvent
import com.simplevideo.whiteiptv.feature.settings.mvi.SettingsState
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val viewModel = koinViewModel<SettingsViewModel>()
    val state by viewModel.viewStates().collectAsState()
    val action by viewModel.viewActions().collectAsState(initial = null)
    val snackbarHostState = remember { SnackbarHostState() }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(action) {
        when (val currentAction = action) {
            is SettingsAction.ShowCacheCleared -> {
                snackbarHostState.showSnackbar("Cache cleared")
                viewModel.clearAction()
            }

            is SettingsAction.ShowFavoritesCleared -> {
                snackbarHostState.showSnackbar("All favorites cleared")
                viewModel.clearAction()
            }

            is SettingsAction.ShowSettingsReset -> {
                snackbarHostState.showSnackbar("Settings reset to defaults")
                viewModel.clearAction()
            }

            is SettingsAction.OpenUrl -> {
                uriHandler.openUri(currentAction.url)
                viewModel.clearAction()
            }

            is SettingsAction.OpenEmail -> {
                uriHandler.openUri("mailto:${currentAction.email}")
                viewModel.clearAction()
            }

            else -> Unit
        }
    }

    if (state.showClearFavoritesDialog) {
        ConfirmationDialog(
            title = "Clear Favorites",
            message = "This will remove all channels from your favorites. This action cannot be undone.",
            onConfirm = { viewModel.obtainEvent(SettingsEvent.OnClearFavoritesConfirm) },
            onDismiss = { viewModel.obtainEvent(SettingsEvent.OnDismissDialog) },
        )
    }

    if (state.showResetDialog) {
        ConfirmationDialog(
            title = "Reset to Defaults",
            message = "This will reset all settings to their default values. This action cannot be undone.",
            onConfirm = { viewModel.obtainEvent(SettingsEvent.OnResetConfirm) },
            onDismiss = { viewModel.obtainEvent(SettingsEvent.OnDismissDialog) },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // 1. Appearance Section
            item { AppearanceSection(state, viewModel::obtainEvent) }
            item { Spacer(modifier = Modifier.height(24.dp)) }

            // 2. Playback Section
            item { PlaybackSection() }
            item { Spacer(modifier = Modifier.height(24.dp)) }

            // 3. App Behavior Section
            item { AppBehaviorSection() }
            item { Spacer(modifier = Modifier.height(24.dp)) }

            // 4. Data & Storage Section
            item { DataStorageSection(viewModel::obtainEvent) }
            item { Spacer(modifier = Modifier.height(24.dp)) }

            // 5. Preferences Section
            item { PreferencesSection(state, viewModel::obtainEvent) }
            item { Spacer(modifier = Modifier.height(24.dp)) }

            // 6. About Section
            item { AboutSection(state, viewModel::obtainEvent) }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun AppearanceSection(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
) {
    SettingsSectionHeader(
        title = "Appearance",
        icon = Icons.Filled.Palette,
        gradientColors = SettingsAppearanceGradient,
    )
    SettingsDropdownRow(
        icon = Icons.Filled.Contrast,
        title = "Theme",
        subtitle = themeModeLabel(state.themeMode),
        options = listOf(ThemeMode.System, ThemeMode.Light, ThemeMode.Dark),
        selectedOption = state.themeMode,
        onOptionSelected = { onEvent(SettingsEvent.OnThemeModeChanged(it)) },
        optionLabel = ::themeModeLabel,
        optionDescription = ::themeModeDescription,
        optionIcon = { mode -> ThemeModeIcon(mode) },
    )
    Spacer(modifier = Modifier.height(8.dp))
    SettingsDropdownRow(
        icon = Icons.Filled.ColorLens,
        title = "Accent Color",
        subtitle = accentColorLabel(state.accentColor),
        options = AccentColor.entries.toList(),
        selectedOption = state.accentColor,
        onOptionSelected = { onEvent(SettingsEvent.OnAccentColorChanged(it)) },
        optionLabel = ::accentColorLabel,
        optionDescription = ::accentColorDescription,
        optionIcon = { color -> AccentColorIcon(color) },
    )
    Spacer(modifier = Modifier.height(8.dp))
    SettingsDropdownRow(
        icon = Icons.AutoMirrored.Filled.ViewList,
        title = "Channel View",
        subtitle = state.channelViewMode.name,
        options = ChannelViewMode.entries.toList(),
        selectedOption = state.channelViewMode,
        onOptionSelected = { onEvent(SettingsEvent.OnChannelViewModeChanged(it)) },
        optionLabel = { it.name },
        optionDescription = ::channelViewDescription,
        optionIcon = { mode -> ChannelViewIcon(mode) },
    )
}

@Composable
private fun PlaybackSection() {
    SettingsSectionHeader(
        title = "Playback",
        icon = Icons.Filled.PlayCircle,
        gradientColors = SettingsPlaybackGradient,
    )
    SettingsDropdownRow(
        icon = Icons.Filled.PlayCircle,
        title = "Default Player",
        subtitle = "Internal",
        options = listOf("Internal"),
        selectedOption = "Internal",
        onOptionSelected = {},
        optionLabel = { it },
        optionDescription = { "Built-in video player" },
    )
    Spacer(modifier = Modifier.height(8.dp))
    SettingsDropdownRow(
        icon = Icons.Filled.Tune,
        title = "Quality",
        subtitle = "Auto",
        options = listOf("Auto", "HD", "SD"),
        selectedOption = "Auto",
        onOptionSelected = {},
        optionLabel = { it },
        optionDescription = {
            when (it) {
                "Auto" -> "Automatically adjust quality"
                "HD" -> "High definition streaming"
                "SD" -> "Standard definition, less data"
                else -> ""
            }
        },
    )
}

@Composable
private fun AppBehaviorSection() {
    SettingsSectionHeader(
        title = "App Behavior",
        icon = Icons.Filled.Settings,
        gradientColors = SettingsAppBehaviorGradient,
    )
    SettingsDropdownRow(
        icon = Icons.AutoMirrored.Filled.QueueMusic,
        title = "Default Playlist",
        subtitle = "Main Playlist",
        options = listOf("Main Playlist"),
        selectedOption = "Main Playlist",
        onOptionSelected = {},
        optionLabel = { it },
        optionDescription = { "Primary playlist on startup" },
    )
    Spacer(modifier = Modifier.height(8.dp))
    SettingsDropdownRow(
        icon = Icons.Filled.Language,
        title = "Language",
        subtitle = "English",
        options = listOf("English"),
        selectedOption = "English",
        onOptionSelected = {},
        optionLabel = { it },
        optionDescription = { "Interface language" },
    )
}

@Composable
private fun DataStorageSection(
    onEvent: (SettingsEvent) -> Unit,
) {
    SettingsSectionHeader(
        title = "Data & Storage",
        icon = Icons.Filled.Storage,
        gradientColors = SettingsDataStorageGradient,
    )
    SettingsActionRow(
        icon = Icons.Filled.Delete,
        title = "Clear Favorites",
        onClick = { onEvent(SettingsEvent.OnClearFavoritesClick) },
    )
    Spacer(modifier = Modifier.height(8.dp))
    SettingsActionRow(
        icon = Icons.Filled.Cached,
        title = "Clear Cache",
        onClick = { onEvent(SettingsEvent.OnClearCacheClick) },
    )
    Spacer(modifier = Modifier.height(8.dp))
    SettingsActionRow(
        icon = Icons.Filled.RestartAlt,
        title = "Reset to Defaults",
        onClick = { onEvent(SettingsEvent.OnResetClick) },
    )
}

@Composable
private fun PreferencesSection(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
) {
    SettingsSectionHeader(
        title = "Preferences",
        icon = Icons.Filled.Notifications,
        gradientColors = SettingsPreferencesGradient,
    )
    SettingsSwitchRow(
        icon = Icons.Filled.Sync,
        title = "Auto Update Playlists",
        subtitle = "Update on app startup",
        checked = state.autoUpdateEnabled,
        onCheckedChange = { onEvent(SettingsEvent.OnAutoUpdateChanged(it)) },
    )
    Spacer(modifier = Modifier.height(8.dp))
    SettingsSwitchRow(
        icon = Icons.Filled.NotificationsNone,
        title = "Notifications",
        subtitle = "Channel updates",
        checked = state.notificationsEnabled,
        onCheckedChange = { onEvent(SettingsEvent.OnNotificationsChanged(it)) },
    )
}

@Composable
private fun AboutSection(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
) {
    SettingsSectionHeader(
        title = "About",
        icon = Icons.Outlined.Info,
        gradientColors = SettingsAboutGradient,
    )
    SettingsInfoRow(
        title = "App Version",
        value = state.appVersion,
    )
    Spacer(modifier = Modifier.height(8.dp))
    SettingsInfoRow(
        title = "Contact Support",
        icon = Icons.Filled.Email,
        onClick = { onEvent(SettingsEvent.OnContactSupportClick) },
    )
    Spacer(modifier = Modifier.height(8.dp))
    SettingsInfoRow(
        title = "Privacy Policy",
        icon = Icons.Filled.Security,
        onClick = { onEvent(SettingsEvent.OnPrivacyPolicyClick) },
    )
    Spacer(modifier = Modifier.height(8.dp))
    SettingsInfoRow(
        title = "Terms of Service",
        icon = Icons.Filled.Description,
        onClick = { onEvent(SettingsEvent.OnTermsOfServiceClick) },
    )
}

private fun themeModeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.System -> "System"
    ThemeMode.Light -> "Light"
    ThemeMode.Dark -> "Dark"
}

private fun themeModeDescription(mode: ThemeMode): String = when (mode) {
    ThemeMode.System -> "Follow system theme settings"
    ThemeMode.Light -> "Light background for daytime use"
    ThemeMode.Dark -> "Dark background for better viewing"
}

@Composable
private fun ThemeModeIcon(mode: ThemeMode) {
    val isDark = com.simplevideo.whiteiptv.common.components.isDarkTheme()
    val gradientColors = when (mode) {
        ThemeMode.Dark -> listOf(Color(0xFF1E293B), Color(0xFF0F172A))
        ThemeMode.Light -> listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))
        ThemeMode.System -> listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
    }
    val icon = when (mode) {
        ThemeMode.Dark -> Icons.Filled.DarkMode
        ThemeMode.Light -> Icons.Filled.LightMode
        ThemeMode.System -> Icons.Filled.Settings
    }
    val iconTint = when (mode) {
        ThemeMode.Dark -> Color(0xFFFBBF24)
        ThemeMode.Light -> Color(0xFFF59E0B)
        ThemeMode.System -> Color.White
    }
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(gradientColors))
            .then(
                if (!isDark && mode == ThemeMode.Light) {
                    Modifier.border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                } else {
                    Modifier.border(
                        1.dp,
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(14.dp),
                    )
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp),
        )
    }
}

private fun accentColorLabel(color: AccentColor): String = when (color) {
    AccentColor.Teal -> "Cyan"
    AccentColor.Blue -> "Blue"
    AccentColor.Red -> "Red"
}

private fun accentColorDescription(color: AccentColor): String = when (color) {
    AccentColor.Teal -> "Cyan accent throughout the app"
    AccentColor.Blue -> "Blue accent throughout the app"
    AccentColor.Red -> "Red accent throughout the app"
}

@Composable
private fun AccentColorIcon(color: AccentColor) {
    val previewColor = when (color) {
        AccentColor.Teal -> Color(0xFF00D4FF)
        AccentColor.Blue -> Color(0xFF1A73E8)
        AccentColor.Red -> Color(0xFFC62828)
    }
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(previewColor.copy(alpha = 0.15f))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(previewColor),
        )
    }
}

private fun channelViewDescription(mode: ChannelViewMode): String = when (mode) {
    ChannelViewMode.Grid -> "Display channels in a grid layout"
    ChannelViewMode.List -> "Display channels in a list layout"
}

@Composable
private fun ChannelViewIcon(mode: ChannelViewMode) {
    val isDark = com.simplevideo.whiteiptv.common.components.isDarkTheme()
    val icon = when (mode) {
        ChannelViewMode.Grid -> Icons.Filled.GridView
        ChannelViewMode.List -> Icons.AutoMirrored.Filled.ViewList
    }
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFF1F5F9))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDark) Color.White else Color(0xFF64748B),
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
