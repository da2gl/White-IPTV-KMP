package com.simplevideo.whiteiptv.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.domain.model.ChannelGroup
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection
import com.simplevideo.whiteiptv.feature.home.components.PlaylistSettingsBottomSheet
import com.simplevideo.whiteiptv.feature.home.mvi.ContinueWatchingItem
import com.simplevideo.whiteiptv.feature.home.mvi.HomeAction
import com.simplevideo.whiteiptv.feature.home.mvi.HomeEvent
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
                        continueWatchingItems = state.continueWatchingItems,
                        favoriteChannels = state.favoriteChannels,
                        categories = state.categories,
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

    selectedPlaylist?.let { playlist ->
        if (state.showPlaylistSettings) {
            PlaylistSettingsBottomSheet(
                playlist = playlist,
                onDismiss = { viewModel.obtainEvent(HomeEvent.OnPlaylistSettingsDismiss) },
                onRename = { viewModel.obtainEvent(HomeEvent.OnRenameClick) },
                onUpdate = { viewModel.obtainEvent(HomeEvent.OnUpdatePlaylistClick) },
                onDelete = { viewModel.obtainEvent(HomeEvent.OnDeleteClick) },
                onViewUrl = { viewModel.obtainEvent(HomeEvent.OnViewUrlClick) },
            )
        }

        if (state.showRenameDialog) {
            RenameDialog(
                currentName = playlist.name,
                onDismiss = { viewModel.obtainEvent(HomeEvent.OnRenameDialogDismiss) },
                onConfirm = { newName -> viewModel.obtainEvent(HomeEvent.OnRenameConfirm(newName)) },
            )
        }

        if (state.showDeleteConfirmation) {
            DeleteConfirmationDialog(
                playlistName = playlist.name,
                onDismiss = { viewModel.obtainEvent(HomeEvent.OnDeleteDialogDismiss) },
                onConfirm = { viewModel.obtainEvent(HomeEvent.OnDeleteConfirm) },
            )
        }

        if (state.showViewUrlDialog) {
            ViewUrlDialog(
                url = playlist.url,
                onDismiss = { viewModel.obtainEvent(HomeEvent.OnViewUrlDialogDismiss) },
            )
        }
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
    val bgColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
    val buttonBg = MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
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
                .background(buttonBg),
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
                    .background(buttonBg),
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
    continueWatchingItems: List<ContinueWatchingItem>,
    favoriteChannels: List<ChannelEntity>,
    categories: List<Pair<ChannelGroup, List<ChannelEntity>>>,
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
        if (continueWatchingItems.isNotEmpty()) {
            item(key = "cw", contentType = "continue_watching") {
                ContinueWatchingSection(
                    items = continueWatchingItems,
                    onChannelClick = onChannelClick,
                )
            }
        }

        if (favoriteChannels.isNotEmpty()) {
            item(key = "fav", contentType = "favorites") {
                FavoritesSection(
                    channels = favoriteChannels,
                    onViewAllClick = onFavoritesViewAllClick,
                    onChannelClick = onChannelClick,
                    onToggleFavorite = onToggleFavorite,
                )
            }
        }

        val nonEmptyCategories = categories.filter { it.second.isNotEmpty() }
        items(
            count = nonEmptyCategories.size,
            key = { index -> "group_${nonEmptyCategories[index].first.id}" },
            contentType = { "category" },
        ) { index ->
            val (group, channels) = nonEmptyCategories[index]
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

@Composable
private fun ContinueWatchingSection(
    items: List<ContinueWatchingItem>,
    onChannelClick: (Long) -> Unit,
) {
    SectionHeader(
        title = "Continue Watching",
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items.forEach { item ->
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        channels.forEach { channel ->
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        channels.forEach { channel ->
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
