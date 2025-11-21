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
import com.simplevideo.whiteiptv.domain.model.ChannelCategory
import com.simplevideo.whiteiptv.feature.home.mvi.ContinueWatchingItem
import com.simplevideo.whiteiptv.feature.home.mvi.HomeEvent
import com.simplevideo.whiteiptv.feature.home.mvi.HomeState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToChannels: (categoryId: String?) -> Unit = {},
) {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.viewStates().collectAsState()

    Scaffold(
        topBar = {
            HomeTopAppBar()
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
                onViewAllClick = onNavigateToChannels,
                onPlaylistSelected = { playlistId ->
                    viewModel.obtainEvent(HomeEvent.OnPlaylistSelected(playlistId))
                },
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar() {
    TopAppBar(
        title = { Text("My Playlist") },
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
private fun HomeContent(
    state: HomeState,
    onViewAllClick: (categoryId: String?) -> Unit,
    onPlaylistSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Playlist selector
        PlaylistDropdown(
            playlists = state.playlists,
            selectedPlaylistId = state.selectedPlaylistId,
            onPlaylistSelected = onPlaylistSelected,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

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
                onViewAllClick = { onViewAllClick(ChannelCategory.Favorites.id) },
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.favoriteChannels) { channel ->
                        ChannelItem(channel)
                    }
                }
            }
        }

        // Dynamic Categories
        state.categories.forEach { (category, channels) ->
            if (channels.isNotEmpty()) {
                Section(
                    title = category.displayName,
                    onViewAllClick = { onViewAllClick(category.id) },
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(channels) { channel ->
                            ChannelItem(channel)
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
private fun ChannelItem(channel: ChannelEntity) {
    Card(modifier = Modifier.width(150.dp)) {
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
