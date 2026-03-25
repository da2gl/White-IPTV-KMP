package com.simplevideo.whiteiptv.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.simplevideo.whiteiptv.common.components.ChannelCardSquare
import com.simplevideo.whiteiptv.common.components.ContinueWatchingCard
import com.simplevideo.whiteiptv.common.components.GradientBackground
import com.simplevideo.whiteiptv.common.components.PlaylistDropdown
import com.simplevideo.whiteiptv.common.components.SectionHeader
import com.simplevideo.whiteiptv.common.components.SectionHeaderWithViewAll
import com.simplevideo.whiteiptv.common.components.isDarkTheme
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.designsystem.HeaderDarkBg
import com.simplevideo.whiteiptv.domain.model.ChannelGroup
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection
import com.simplevideo.whiteiptv.feature.home.components.PlaylistSettingsBottomSheet
import com.simplevideo.whiteiptv.feature.home.mvi.ContinueWatchingItem
import com.simplevideo.whiteiptv.feature.home.mvi.HomeAction
import com.simplevideo.whiteiptv.feature.home.mvi.HomeEvent
import com.simplevideo.whiteiptv.feature.home.mvi.HomeState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToFavorites: () -> Unit,
    onNavigateToChannels: (String?) -> Unit,
    onNavigateToPlayer: (Long) -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToSearch: () -> Unit = {},
) {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.viewStates().collectAsState()
    val action by viewModel.viewActions().collectAsState(initial = null)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(action) {
        when (val currentAction = action) {
            is HomeAction.NavigateToFavorites -> {
                onNavigateToFavorites()
                viewModel.clearAction()
            }

            is HomeAction.NavigateToChannels -> {
                onNavigateToChannels(currentAction.groupId)
                viewModel.clearAction()
            }

            is HomeAction.NavigateToPlayer -> {
                onNavigateToPlayer(currentAction.channelId)
                viewModel.clearAction()
            }

            is HomeAction.NavigateToOnboarding -> {
                onNavigateToOnboarding()
                viewModel.clearAction()
            }

            else -> Unit
        }
    }

    LaunchedEffect(state.playlistManagementError) {
        state.playlistManagementError?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.obtainEvent(HomeEvent.OnPlaylistManagementErrorDismiss)
        }
    }

    val selectedPlaylist = remember(state.selection, state.playlists) {
        (state.selection as? PlaylistSelection.Selected)?.let { sel ->
            state.playlists.find { it.id == sel.id }
        }
    }

    Scaffold(
        topBar = {
            HomeTopAppBar(
                playlists = state.playlists,
                selection = state.selection,
                onPlaylistSelect = { viewModel.obtainEvent(HomeEvent.OnPlaylistSelected(it)) },
                onAddPlaylistClick = { viewModel.obtainEvent(HomeEvent.OnAddPlaylistClick) },
                onSearchClick = onNavigateToSearch,
                onPlaylistSettingsClick = {
                    viewModel.obtainEvent(HomeEvent.OnPlaylistSettingsClick)
                },
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
        containerColor = Color.Transparent,
    ) { paddingValues ->
        GradientBackground {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                } else {
                    HomeContent(
                        state = state,
                        onFavoritesViewAllClick = {
                            viewModel.obtainEvent(HomeEvent.OnFavoritesViewAllClick)
                        },
                        onGroupViewAllClick = { groupId ->
                            viewModel.obtainEvent(HomeEvent.OnGroupViewAllClick(groupId))
                        },
                        onChannelClick = { channelId ->
                            viewModel.obtainEvent(HomeEvent.OnChannelClick(channelId))
                        },
                        onToggleFavorite = { channelId ->
                            viewModel.obtainEvent(HomeEvent.OnToggleFavorite(channelId))
                        },
                    )
                }

                if (state.isUpdatingPlaylist) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    if (state.showPlaylistSettings && selectedPlaylist != null) {
        PlaylistSettingsBottomSheet(
            playlist = selectedPlaylist!!,
            onDismiss = { viewModel.obtainEvent(HomeEvent.OnPlaylistSettingsDismiss) },
            onRename = { viewModel.obtainEvent(HomeEvent.OnRenameClick) },
            onUpdate = { viewModel.obtainEvent(HomeEvent.OnUpdatePlaylistClick) },
            onDelete = { viewModel.obtainEvent(HomeEvent.OnDeleteClick) },
            onViewUrl = { viewModel.obtainEvent(HomeEvent.OnViewUrlClick) },
        )
    }

    if (state.showRenameDialog && selectedPlaylist != null) {
        RenameDialog(
            currentName = selectedPlaylist!!.name,
            onDismiss = { viewModel.obtainEvent(HomeEvent.OnRenameDialogDismiss) },
            onConfirm = { newName -> viewModel.obtainEvent(HomeEvent.OnRenameConfirm(newName)) },
        )
    }

    if (state.showDeleteConfirmation && selectedPlaylist != null) {
        DeleteConfirmationDialog(
            playlistName = selectedPlaylist!!.name,
            onDismiss = { viewModel.obtainEvent(HomeEvent.OnDeleteDialogDismiss) },
            onConfirm = { viewModel.obtainEvent(HomeEvent.OnDeleteConfirm) },
        )
    }

    if (state.showViewUrlDialog && selectedPlaylist != null) {
        ViewUrlDialog(
            url = selectedPlaylist!!.url,
            onDismiss = { viewModel.obtainEvent(HomeEvent.OnViewUrlDialogDismiss) },
        )
    }
}

@Composable
private fun HomeTopAppBar(
    playlists: List<PlaylistEntity>,
    selection: PlaylistSelection,
    onPlaylistSelect: (PlaylistSelection) -> Unit,
    onAddPlaylistClick: () -> Unit,
    onSearchClick: () -> Unit,
    onPlaylistSettingsClick: () -> Unit,
) {
    val isDark = isDarkTheme()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isDark) HeaderDarkBg.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f),
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlaylistDropdown(
            playlists = playlists,
            selection = selection,
            onPlaylistSelect = onPlaylistSelect,
            onAddPlaylistClick = onAddPlaylistClick,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSearchClick,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .then(
                    if (isDark) {
                        Modifier
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    } else {
                        Modifier
                            .background(Color.White)
                            .border(1.dp, Color(0xFFe5e7eb), RoundedCornerShape(12.dp))
                    },
                ),
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
        if (selectedPlaylistAvailable(selection)) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onPlaylistSettingsClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (isDark) {
                            Modifier
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(12.dp),
                                )
                        } else {
                            Modifier
                                .background(Color.White)
                                .border(1.dp, Color(0xFFe5e7eb), RoundedCornerShape(12.dp))
                        },
                    ),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Playlist Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

private fun selectedPlaylistAvailable(selection: PlaylistSelection): Boolean =
    selection is PlaylistSelection.Selected

@Composable
private fun RenameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Playlist") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Playlist name") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun DeleteConfirmationDialog(
    playlistName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Playlist") },
        text = {
            Text("Delete '$playlistName'? All channels and groups will be removed.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun ViewUrlDialog(
    url: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Playlist URL") },
        text = {
            SelectionContainer {
                Text(url)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
    )
}

@Composable
private fun HomeContent(
    state: HomeState,
    onFavoritesViewAllClick: () -> Unit,
    onGroupViewAllClick: (groupId: String) -> Unit,
    onChannelClick: (channelId: Long) -> Unit,
    onToggleFavorite: (channelId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
    ) {
        if (state.continueWatchingItems.isNotEmpty()) {
            item(key = "cw") {
                ContinueWatchingSection(
                    items = state.continueWatchingItems,
                    onChannelClick = onChannelClick,
                )
            }
        }

        if (state.favoriteChannels.isNotEmpty()) {
            item(key = "fav") {
                FavoritesSection(
                    channels = state.favoriteChannels,
                    onViewAllClick = onFavoritesViewAllClick,
                    onChannelClick = onChannelClick,
                    onToggleFavorite = onToggleFavorite,
                )
            }
        }

        state.categories.forEach { (group, channels) ->
            if (channels.isNotEmpty()) {
                item(key = "group_${group.id}") {
                    CategorySection(
                        group = group,
                        channels = channels,
                        onViewAllClick = { onGroupViewAllClick(group.id) },
                        onChannelClick = onChannelClick,
                        onToggleFavorite = onToggleFavorite,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContinueWatchingSection(
    items: List<ContinueWatchingItem>,
    onChannelClick: (Long) -> Unit,
) {
    SectionHeader(
        title = "Continue Watching",
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items) { item ->
            ContinueWatchingCard(
                name = item.channel.name,
                logoUrl = item.channel.logoUrl,
                onClick = { onChannelClick(item.channel.id) },
                progress = item.progress,
                modifier = Modifier.width(200.dp),
            )
        }
    }
}

@Composable
private fun FavoritesSection(
    channels: List<ChannelEntity>,
    onViewAllClick: () -> Unit,
    onChannelClick: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
) {
    SectionHeaderWithViewAll(
        title = "Favorites",
        onViewAllClick = onViewAllClick,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(channels, key = { it.id }) { channel ->
            ChannelCardSquare(
                name = channel.name,
                logoUrl = channel.logoUrl,
                isFavorite = channel.isFavorite,
                onClick = { onChannelClick(channel.id) },
                onToggleFavorite = { onToggleFavorite(channel.id) },
                modifier = Modifier.width(160.dp),
            )
        }
    }
}

@Composable
private fun CategorySection(
    group: ChannelGroup,
    channels: List<ChannelEntity>,
    onViewAllClick: () -> Unit,
    onChannelClick: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
) {
    SectionHeaderWithViewAll(
        title = group.displayName,
        onViewAllClick = onViewAllClick,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(channels, key = { it.id }) { channel ->
            ChannelCardSquare(
                name = channel.name,
                logoUrl = channel.logoUrl,
                isFavorite = channel.isFavorite,
                onClick = { onChannelClick(channel.id) },
                onToggleFavorite = { onToggleFavorite(channel.id) },
                modifier = Modifier.width(160.dp),
            )
        }
    }
}
