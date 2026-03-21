package com.simplevideo.whiteiptv.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Hd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.simplevideo.whiteiptv.domain.model.AccentColor
import com.simplevideo.whiteiptv.domain.model.ChannelViewMode
import com.simplevideo.whiteiptv.domain.model.ThemeMode
import com.simplevideo.whiteiptv.feature.settings.components.SettingsActionRow
import com.simplevideo.whiteiptv.feature.settings.components.SettingsCard
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
                uriHandler.openUri(currentAction.email)
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
            message = "This will reset all settings to their default values. " +
                "Your playlists and channels will not be affected.",
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

            // Appearance Section
            item { AppearanceSection(state, viewModel::obtainEvent) }
            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Playback Section
            item { PlaybackSection(state, viewModel::obtainEvent) }
            item { Spacer(modifier = Modifier.height(24.dp)) }

            // App Behavior Section
            item { AppBehaviorSection(state, viewModel::obtainEvent) }
            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Data & Storage Section
            item { DataStorageSection(viewModel::obtainEvent) }
            item { Spacer(modifier = Modifier.height(24.dp)) }

            // About Section
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
    SettingsSectionHeader(title = "Appearance")
    SettingsCard {
        SettingsDropdownRow(
            icon = Icons.Filled.Palette,
            title = "Theme",
            subtitle = themeModeLabel(state.themeMode),
            options = listOf(ThemeMode.System, ThemeMode.Light, ThemeMode.Dark),
            selectedOption = state.themeMode,
            onOptionSelected = { onEvent(SettingsEvent.OnThemeModeChanged(it)) },
            optionLabel = ::themeModeLabel,
        )
        SettingsDropdownRow(
            icon = Icons.Filled.Star,
            title = "Accent Color",
            subtitle = state.accentColor.name,
            options = AccentColor.entries.toList(),
            selectedOption = state.accentColor,
            onOptionSelected = { onEvent(SettingsEvent.OnAccentColorChanged(it)) },
            optionLabel = { it.name },
            optionLeadingContent = { color ->
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(accentColorPreview(color)),
                )
            },
        )
        SettingsDropdownRow(
            icon = Icons.AutoMirrored.Filled.ViewList,
            title = "Channel View",
            subtitle = state.channelViewMode.name,
            options = ChannelViewMode.entries.toList(),
            selectedOption = state.channelViewMode,
            onOptionSelected = { onEvent(SettingsEvent.OnChannelViewModeChanged(it)) },
            optionLabel = { it.name },
            showDivider = false,
        )
    }
}

@Composable
private fun PlaybackSection(
    @Suppress("UNUSED_PARAMETER") state: SettingsState,
    @Suppress("UNUSED_PARAMETER") onEvent: (SettingsEvent) -> Unit,
) {
    SettingsSectionHeader(title = "Playback")
    SettingsCard {
        SettingsDropdownRow(
            icon = Icons.Filled.PlayCircle,
            title = "Default Player",
            subtitle = "ExoPlayer",
            options = listOf("ExoPlayer"),
            selectedOption = "ExoPlayer",
            onOptionSelected = {},
            optionLabel = { it },
            showDivider = true,
        )
        SettingsDropdownRow(
            icon = Icons.Outlined.Hd,
            title = "Preferred Quality",
            subtitle = "Auto",
            options = listOf("Auto", "HD", "SD"),
            selectedOption = "Auto",
            onOptionSelected = {},
            optionLabel = { it },
            showDivider = false,
        )
    }
}

@Composable
private fun AppBehaviorSection(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
) {
    SettingsSectionHeader(title = "App Behavior")
    SettingsCard {
        SettingsDropdownRow(
            icon = Icons.Filled.Language,
            title = "Language",
            subtitle = "System",
            options = listOf("System"),
            selectedOption = "System",
            onOptionSelected = {},
            optionLabel = { it },
        )
        SettingsSwitchRow(
            icon = Icons.Filled.Refresh,
            title = "Auto-Update Playlists",
            subtitle = "Automatically refresh playlists on app start",
            checked = state.autoUpdateEnabled,
            onCheckedChange = { onEvent(SettingsEvent.OnAutoUpdateChanged(it)) },
            showDivider = false,
        )
    }
}

@Composable
private fun DataStorageSection(
    onEvent: (SettingsEvent) -> Unit,
) {
    SettingsSectionHeader(title = "Data & Storage")
    SettingsCard {
        SettingsActionRow(
            icon = Icons.Filled.Cached,
            title = "Clear Cache",
            onClick = { onEvent(SettingsEvent.OnClearCacheClick) },
        )
        SettingsActionRow(
            icon = Icons.Filled.Delete,
            title = "Clear Favorites",
            onClick = { onEvent(SettingsEvent.OnClearFavoritesClick) },
        )
        SettingsActionRow(
            icon = Icons.Filled.RestartAlt,
            title = "Reset to Defaults",
            onClick = { onEvent(SettingsEvent.OnResetClick) },
            isDestructive = true,
            showDivider = false,
        )
    }
}

@Composable
private fun AboutSection(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
) {
    SettingsSectionHeader(title = "About")
    SettingsCard {
        SettingsInfoRow(
            title = "App Version",
            value = "v${state.appVersion}",
        )
        SettingsInfoRow(
            title = "Contact Support",
            onClick = { onEvent(SettingsEvent.OnContactSupportClick) },
        )
        SettingsInfoRow(
            title = "Privacy Policy",
            onClick = { onEvent(SettingsEvent.OnPrivacyPolicyClick) },
            showDivider = false,
        )
    }
}

private fun themeModeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.System -> "System"
    ThemeMode.Light -> "Light"
    ThemeMode.Dark -> "Dark"
}

private val tealPreview = Color(0xFF0284C7)
private val bluePreview = Color(0xFF1a73e8)
private val redPreview = Color(0xFFc62828)

private fun accentColorPreview(color: AccentColor): Color = when (color) {
    AccentColor.Teal -> tealPreview
    AccentColor.Blue -> bluePreview
    AccentColor.Red -> redPreview
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
