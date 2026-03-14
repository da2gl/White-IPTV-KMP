package com.simplevideo.whiteiptv.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.simplevideo.whiteiptv.common.components.ChannelCardSquare
import com.simplevideo.whiteiptv.common.components.ContinueWatchingCard
import com.simplevideo.whiteiptv.common.components.PlaylistDropdown
import com.simplevideo.whiteiptv.common.components.SearchEmptyState
import com.simplevideo.whiteiptv.common.components.SearchTopBar
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection
import com.simplevideo.whiteiptv.feature.home.components.PlaylistSettingsBottomSheet
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

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(state.isSearchActive) {
        if (state.isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            if (state.isSearchActive) {
                SearchTopBar(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.obtainEvent(HomeEvent.OnSearchQueryChanged(it)) },
                    onClose = { viewModel.obtainEvent(HomeEvent.OnToggleSearch) },
                    focusRequester = focusRequester,
                    placeholder = "Search channels...",
                )
            } else {
                HomeTopAppBar(
                    playlists = state.playlists,
                    selection = state.selection,
                    onPlaylistSelect = { selection ->
                        viewModel.obtainEvent(HomeEvent.OnPlaylistSelected(selection))
                    },
                    onAddPlaylistClick = { viewModel.obtainEvent(HomeEvent.OnAddPlaylistClick) },
                    onSearchClick = { viewModel.obtainEvent(HomeEvent.OnToggleSearch) },
                    onPlaylistSettingsClick = {
                        viewModel.obtainEvent(HomeEvent.OnPlaylistSettingsClick)
                    },
                    isPlaylistSettingsEnabled = state.selection is PlaylistSelection.Selected,
                )
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (state.isSearchActive) {
                HomeSearchResults(
                    searchResults = state.searchResults,
                    searchQuery = state.searchQuery,
                    onChannelClick = { channelId ->
                        viewModel.obtainEvent(HomeEvent.OnSearchResultClick(channelId))
                    },
                )
            } else if (state.isLoading) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar(
    playlists: List<PlaylistEntity>,
    selection: PlaylistSelection,
    onPlaylistSelect: (PlaylistSelection) -> Unit,
    onAddPlaylistClick: () -> Unit,
    onSearchClick: () -> Unit,
    onPlaylistSettingsClick: () -> Unit,
    isPlaylistSettingsEnabled: Boolean,
) {
    TopAppBar(
        title = {
            HomeTopAppBarTitle(
                playlists = playlists,
                selection = selection,
                onPlaylistSelect = onPlaylistSelect,
                onAddPlaylistClick = onAddPlaylistClick,
            )
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
            IconButton(
                onClick = onPlaylistSettingsClick,
                enabled = isPlaylistSettingsEnabled,
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Playlist Settings")
            }
        },
    )
}

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
private fun HomeTopAppBarTitle(
    playlists: List<PlaylistEntity>,
    selection: PlaylistSelection,
    onPlaylistSelect: (PlaylistSelection) -> Unit,
    onAddPlaylistClick: () -> Unit,
) {
    PlaylistDropdown(
        playlists = playlists,
        selection = selection,
        onPlaylistSelect = onPlaylistSelect,
        onAddPlaylistClick = onAddPlaylistClick,
    )
}

@Composable
private fun HomeContent(
    state: HomeState,
    onFavoritesViewAllClick: () -> Unit,
    onGroupViewAllClick: (groupId: String) -> Unit,
    onChannelClick: (channelId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Continue Watching
        if (state.continueWatchingItems.isNotEmpty()) {
            Section(title = "Continue Watching") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.continueWatchingItems) { item ->
                        ContinueWatchingCard(
                            name = item.channel.name,
                            logoUrl = item.channel.logoUrl,
                            onClick = { onChannelClick(item.channel.id) },
                        )
                    }
                }
            }
        }

        // Favorites
        if (state.favoriteChannels.isNotEmpty()) {
            Section(
                title = "Favorites",
                onViewAllClick = onFavoritesViewAllClick,
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.favoriteChannels) { channel ->
                        ChannelCardSquare(
                            name = channel.name,
                            logoUrl = channel.logoUrl,
                            isFavorite = channel.isFavorite,
                            onClick = { onChannelClick(channel.id) },
                            onToggleFavorite = {},
                            modifier = Modifier.width(150.dp),
                        )
                    }
                }
            }
        }

        // Dynamic Groups
        state.categories.forEach { (group, channels) ->
            if (channels.isNotEmpty()) {
                Section(
                    title = group.displayName,
                    onViewAllClick = { onGroupViewAllClick(group.id) },
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(channels) { channel ->
                            ChannelCardSquare(
                                name = channel.name,
                                logoUrl = channel.logoUrl,
                                isFavorite = channel.isFavorite,
                                onClick = { onChannelClick(channel.id) },
                                onToggleFavorite = {},
                                modifier = Modifier.width(150.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    onViewAllClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            if (onViewAllClick != null) {
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onViewAllClick() },
                )
            }
        }
        content()
    }
}

@Composable
private fun HomeSearchResults(
    searchResults: List<ChannelEntity>,
    searchQuery: String,
    onChannelClick: (Long) -> Unit,
) {
    if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
        SearchEmptyState(query = searchQuery)
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(searchResults, key = { it.id }) { channel ->
                SearchResultItem(
                    channel = channel,
                    onClick = { onChannelClick(channel.id) },
                )
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    channel: ChannelEntity,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = channel.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = channel.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
