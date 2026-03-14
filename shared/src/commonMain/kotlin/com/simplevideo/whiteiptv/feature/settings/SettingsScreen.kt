package com.simplevideo.whiteiptv.feature.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.simplevideo.whiteiptv.domain.model.AccentColor
import com.simplevideo.whiteiptv.domain.model.ChannelViewMode
import com.simplevideo.whiteiptv.domain.model.ThemeMode
import com.simplevideo.whiteiptv.feature.settings.components.SettingsItem
import com.simplevideo.whiteiptv.feature.settings.components.SettingsSection
import com.simplevideo.whiteiptv.feature.settings.components.SettingsSegmentedButton
import com.simplevideo.whiteiptv.feature.settings.components.SettingsSwitchItem
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
            message = "This will reset all settings to their default values. Your playlists and channels will not be affected.",
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
                .padding(paddingValues),
        ) {
            item { AppearanceSection(state, viewModel::obtainEvent) }
            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
            item { AppBehaviorSection(state, viewModel::obtainEvent) }
            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
            item { DataStorageSection(state, viewModel::obtainEvent) }
            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
            item { AboutSection(state, viewModel::obtainEvent) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun AppearanceSection(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
) {
    SettingsSection(title = "Appearance") {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        SettingsSegmentedButton(
            options = listOf(ThemeMode.System, ThemeMode.Light, ThemeMode.Dark),
            selected = state.themeMode,
            onSelect = { onEvent(SettingsEvent.OnThemeModeChanged(it)) },
            label = { mode ->
                when (mode) {
                    ThemeMode.System -> "System"
                    ThemeMode.Light -> "Light"
                    ThemeMode.Dark -> "Dark"
                }
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Accent Color",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        SettingsSegmentedButton(
            options = AccentColor.entries.toList(),
            selected = state.accentColor,
            onSelect = { onEvent(SettingsEvent.OnAccentColorChanged(it)) },
            label = { it.name },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Channel View",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        SettingsSegmentedButton(
            options = ChannelViewMode.entries.toList(),
            selected = state.channelViewMode,
            onSelect = { onEvent(SettingsEvent.OnChannelViewModeChanged(it)) },
            label = { it.name },
        )
    }
}

@Composable
private fun AppBehaviorSection(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
) {
    SettingsSection(title = "App Behavior") {
        SettingsItem(
            title = "Language",
            subtitle = "System",
            onClick = {},
        )
        SettingsSwitchItem(
            title = "Auto-Update Playlists",
            subtitle = "Automatically refresh playlists on app start",
            checked = state.autoUpdateEnabled,
            onCheckedChange = { onEvent(SettingsEvent.OnAutoUpdateChanged(it)) },
        )
    }
}

@Composable
private fun DataStorageSection(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
) {
    SettingsSection(title = "Data & Storage") {
        SettingsItem(
            title = "Clear Cache",
            subtitle = state.cacheSize,
            onClick = { onEvent(SettingsEvent.OnClearCacheClick) },
        )
        SettingsItem(
            title = "Clear Favorites",
            subtitle = "Remove all favorite channels",
            onClick = { onEvent(SettingsEvent.OnClearFavoritesClick) },
        )
        SettingsItem(
            title = "Reset to Defaults",
            subtitle = "Restore all settings to default values",
            onClick = { onEvent(SettingsEvent.OnResetClick) },
        )
    }
}

@Composable
private fun AboutSection(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
) {
    SettingsSection(title = "About") {
        SettingsItem(
            title = "Version",
            subtitle = state.appVersion,
            onClick = {},
        )
        SettingsItem(
            title = "Contact Support",
            subtitle = "support@simplevideo.com",
            onClick = { onEvent(SettingsEvent.OnContactSupportClick) },
        )
        SettingsItem(
            title = "Privacy Policy",
            onClick = { onEvent(SettingsEvent.OnPrivacyPolicyClick) },
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
