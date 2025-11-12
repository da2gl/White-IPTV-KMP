package com.simplevideo.whiteiptv.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.simplevideo.whiteiptv.domain.model.Channel
import com.simplevideo.whiteiptv.domain.model.ContinueWatchingItem
import com.simplevideo.whiteiptv.feature.home.mvi.HomeState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.viewStates().collectAsState()

    Scaffold(
        topBar = {
            HomeTopAppBar()
        },
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            HomeContent(
                state = state,
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
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Continue Watching
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

        // Favorites
        Section(title = "Favorites") {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.favoriteChannels) { channel ->
                    ChannelItem(channel)
                }
            }
        }

        // Sports
        Section(title = "Sports") {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.sportsChannels) { channel ->
                    ChannelItem(channel)
                }
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp),
        )
        content()
    }
}

@Composable
private fun ContinueWatchingItem(item: ContinueWatchingItem) {
    Card(modifier = Modifier.width(200.dp)) {
        Column {
            // TODO: Replace with an async image loading library
            Box(modifier = Modifier.height(100.dp).fillMaxWidth().background(Color.Gray))
            Column(modifier = Modifier.padding(8.dp)) {
                Text(item.name, style = MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(progress = item.progress)
                Text(item.timeLeft, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ChannelItem(channel: Channel) {
    Card(modifier = Modifier.width(150.dp)) {
        Column {
            // TODO: Replace with an async image loading library
            Box(modifier = Modifier.height(100.dp).fillMaxWidth().background(Color.Gray))
            Column(modifier = Modifier.padding(8.dp)) {
                Text(channel.name, style = MaterialTheme.typography.bodyMedium)
                if (channel.isLive) {
                    Text("LIVE", style = MaterialTheme.typography.bodySmall, color = Color.Red)
                }
            }
        }
    }
}
