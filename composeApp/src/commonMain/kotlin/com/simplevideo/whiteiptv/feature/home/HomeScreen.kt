package com.simplevideo.whiteiptv.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.simplevideo.whiteiptv.common.components.PlaylistDropdown
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection
import com.simplevideo.whiteiptv.feature.home.mvi.ContinueWatchingItem
import com.simplevideo.whiteiptv.feature.home.mvi.HomeAction
import com.simplevideo.whiteiptv.feature.home.mvi.HomeEvent
import com.simplevideo.whiteiptv.feature.home.mvi.HomeState
import com.simplevideo.whiteiptv.navigation.ChannelsDestination
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToFavorites: () -> Unit,
    onNavigateToChannels: (ChannelsDestination) -> Unit,
    onNavigateToPlayer: (Long) -> Unit,
) {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.viewStates().collectAsState()
    val action by viewModel.viewActions().collectAsState(initial = null)

    LaunchedEffect(action) {
        when (val currentAction = action) {
            is HomeAction.NavigateToFavorites -> {
                onNavigateToFavorites()
                viewModel.clearAction()
            }

            is HomeAction.NavigateToChannels -> {
                onNavigateToChannels(currentAction.destination)
                viewModel.clearAction()
            }

            is HomeAction.NavigateToPlayer -> {
                onNavigateToPlayer(currentAction.channelId)
                viewModel.clearAction()
            }

            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            HomeTopAppBar(
                playlists = state.playlists,
                selection = state.selection,
                onPlaylistSelected = { selection ->
                    viewModel.obtainEvent(HomeEvent.OnPlaylistSelected(selection))
                },
            )
        },
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
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
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar(
    playlists: List<PlaylistEntity>,
    selection: PlaylistSelection,
    onPlaylistSelected: (PlaylistSelection) -> Unit,
) {
    TopAppBar(
        title = {
            HomeTopAppBarTitle(
                playlists = playlists,
                selection = selection,
                onPlaylistSelected = onPlaylistSelected,
            )
        },
        actions = {
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        },
    )
}

@Composable
private fun HomeTopAppBarTitle(
    playlists: List<PlaylistEntity>,
    selection: PlaylistSelection,
    onPlaylistSelected: (PlaylistSelection) -> Unit,
) {
    PlaylistDropdown(
        playlists = playlists,
        selection = selection,
        onPlaylistSelected = onPlaylistSelected,
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
                        ContinueWatchingItem(item)
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
                        ChannelItem(
                            channel = channel,
                            onClick = { onChannelClick(channel.id) },
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
                            ChannelItem(
                                channel = channel,
                                onClick = { onChannelClick(channel.id) },
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
private fun ContinueWatchingItem(item: ContinueWatchingItem) {
    Card(modifier = Modifier.width(200.dp)) {
        Column {
            AsyncImage(
                model = item.channel.logoUrl,
                contentDescription = item.channel.name,
                modifier = Modifier.height(100.dp).fillMaxWidth().background(Color.Gray),
                contentScale = ContentScale.Crop,
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(item.channel.name, style = MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(progress = { item.progress })
                Text(item.timeLeft, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ChannelItem(
    channel: ChannelEntity,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick),
    ) {
        Column {
            AsyncImage(
                model = channel.logoUrl,
                contentDescription = channel.name,
                modifier = Modifier.height(100.dp).fillMaxWidth().background(Color.Gray),
                contentScale = ContentScale.Crop,
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(channel.name, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
