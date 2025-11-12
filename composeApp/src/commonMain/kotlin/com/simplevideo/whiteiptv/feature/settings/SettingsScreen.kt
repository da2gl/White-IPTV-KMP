package com.simplevideo.whiteiptv.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplevideo.whiteiptv.feature.settings.mvi.SettingsEvent
import com.simplevideo.whiteiptv.feature.settings.mvi.SettingsState
import org.jetbrains.compose.resources.painterResource
import whiteiptvkmp.composeapp.generated.resources.Res
import whiteiptvkmp.composeapp.generated.resources.ic_cleaning


@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.state.collectAsState()

    SettingsScreenContent(
        state = state,
        onEvent = viewModel::obtainEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(SettingsEvent.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Appearance Section
            item {
                SettingsSection(title = "Appearance") {
                    SettingsItemWithSubtitle(
                        icon = Icons.Default.Contrast,
                        title = "Theme",
                        subtitle = state.theme,
                        onClick = { onEvent(SettingsEvent.OnThemeClicked) }
                    )
                    SettingsItemWithSubtitle(
                        icon = Icons.Default.Palette,
                        title = "Accent Color",
                        subtitle = state.accentColor,
                        onClick = { onEvent(SettingsEvent.OnAccentColorClicked) }
                    )
                    SettingsItemWithSubtitle(
                        icon = Icons.Default.ViewList,
                        title = "Channel View",
                        subtitle = state.channelView,
                        onClick = { onEvent(SettingsEvent.OnChannelViewClicked) },
                        isLastItem = true
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Playback Section
            item {
                SettingsSection(title = "Playback") {
                    SettingsItemWithSubtitle(
                        icon = Icons.Default.PlayCircle,
                        title = "Default Player",
                        subtitle = state.defaultPlayer,
                        onClick = { onEvent(SettingsEvent.OnDefaultPlayerClicked) }
                    )
                    SettingsItemWithSubtitle(
                        icon = Icons.Default.Hd,
                        title = "Preferred Quality",
                        subtitle = state.preferredQuality,
                        onClick = { onEvent(SettingsEvent.OnPreferredQualityClicked) },
                        isLastItem = true
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // App Behavior Section
            item {
                SettingsSection(title = "App Behavior") {
                    SettingsItemWithSubtitle(
                        icon = Icons.Default.PlaylistPlay,
                        title = "Default Playlist",
                        subtitle = state.defaultPlaylist,
                        onClick = { onEvent(SettingsEvent.OnDefaultPlaylistClicked) }
                    )
                    SettingsItemWithSubtitle(
                        icon = Icons.Default.Language,
                        title = "Language",
                        subtitle = state.language,
                        onClick = { onEvent(SettingsEvent.OnLanguageClicked) }
                    )
                    SettingsItemWithToggle(
                        icon = Icons.Default.Update,
                        title = "Auto Update Playlists",
                        subtitle = "Daily",
                        isChecked = state.autoUpdatePlaylists,
                        onCheckedChange = { onEvent(SettingsEvent.OnAutoUpdatePlaylistsToggled(it)) },
                        isLastItem = true
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Data & Storage Section
            item {
                SettingsSection(title = "Data & Storage") {
                    SettingsItem(
                        icon = { Icon(painterResource(Res.drawable.ic_cleaning), contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        title = "Clear Cache",
                        onClick = { onEvent(SettingsEvent.OnClearCacheClicked) }
                    )
                    SettingsItem(
                        icon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        title = "Clear Favorites",
                        onClick = { onEvent(SettingsEvent.OnClearFavoritesClicked) }
                    )
                    SettingsItem(
                        icon = { Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Color.Red) },
                        title = "Reset to Defaults",
                        onClick = { onEvent(SettingsEvent.OnResetToDefaultsClicked) },
                        isLastItem = true,
                        titleColor = Color.Red
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // About Section
            item {
                SettingsSection(title = "About") {
                    SettingsItem(
                        title = "App Version",
                        onClick = {},
                        trailingContent = { Text(state.appVersion, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    )
                    SettingsItem(
                        title = "Contact Support",
                        onClick = { onEvent(SettingsEvent.OnContactSupportClicked) }
                    )
                    SettingsItem(
                        title = "Privacy Policy",
                        onClick = { onEvent(SettingsEvent.OnPrivacyPolicyClicked) },
                        isLastItem = true
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable Column.() -> Unit
) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsItemWithSubtitle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isLastItem: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
fun SettingsItemWithToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isLastItem: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}


@Composable
fun SettingsItem(
    title: String,
    onClick: () -> Unit,
    icon: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = {
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    },
    isLastItem: Boolean = false,
    titleColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (icon != null) {
            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                icon()
            }
        }
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f), color = titleColor)
        if (trailingContent != null) {
            trailingContent()
        }
    }
}
