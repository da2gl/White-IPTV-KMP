package com.simplevideo.whiteiptv.feature.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.simplevideo.whiteiptv.common.components.GroupDropdown
import com.simplevideo.whiteiptv.common.components.PlaylistDropdown
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.domain.model.ChannelGroup
import com.simplevideo.whiteiptv.domain.model.PlaylistSelection
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsAction
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsEvent
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChannelsScreen(
    onNavigateToPlayer: (Long) -> Unit,
) {
    val viewModel = koinViewModel<ChannelsViewModel>()
    val state by viewModel.viewStates().collectAsState()
    val action by viewModel.viewActions().collectAsState(initial = null)

    LaunchedEffect(action) {
        when (val currentAction = action) {
            is ChannelsAction.NavigateToPlayer -> {
                onNavigateToPlayer(currentAction.channelId)
                viewModel.clearAction()
            }

            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            ChannelsTopAppBar()
        },
    ) { paddingValues ->
        ChannelsContent(
            state = state,
            onPlaylistSelected = { selection ->
                viewModel.obtainEvent(ChannelsEvent.OnPlaylistSelected(selection))
            },
            onGroupSelected = { group ->
                viewModel.obtainEvent(ChannelsEvent.OnGroupSelected(group))
            },
            onChannelClick = { channelId ->
                viewModel.obtainEvent(ChannelsEvent.OnChannelClick(channelId))
            },
            onToggleFavorite = { channelId ->
                viewModel.obtainEvent(ChannelsEvent.OnToggleFavorite(channelId))
            },
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChannelsTopAppBar() {
    TopAppBar(
        title = { Text("All Channels") },
        actions = {
            IconButton(onClick = { /* TODO: Search */ }) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        },
    )
}

@Composable
private fun ChannelsContent(
    state: ChannelsState,
    onPlaylistSelected: (PlaylistSelection) -> Unit,
    onGroupSelected: (ChannelGroup?) -> Unit,
    onChannelClick: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        PlaylistDropdown(
            playlists = state.playlists,
            selection = state.selection,
            onPlaylistSelected = onPlaylistSelected,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        GroupDropdown(
            groups = state.groups,
            selectedGroup = state.selectedGroup,
            onGroupSelected = onGroupSelected,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (state.channels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No channels found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.channels) { channel ->
                    ChannelGridItem(
                        channel = channel,
                        onClick = { onChannelClick(channel.id) },
                        onToggleFavorite = { onToggleFavorite(channel.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelGridItem(
    channel: ChannelEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            ) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = channel.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                    contentScale = ContentScale.Fit,
                )

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Icon(
                        imageVector = if (channel.isFavorite) {
                            Icons.Filled.Star
                        } else {
                            Icons.Outlined.StarOutline
                        },
                        contentDescription = if (channel.isFavorite) {
                            "Remove from favorites"
                        } else {
                            "Add to favorites"
                        },
                        tint = if (channel.isFavorite) {
                            Color(0xFFFFD700)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
            ) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
