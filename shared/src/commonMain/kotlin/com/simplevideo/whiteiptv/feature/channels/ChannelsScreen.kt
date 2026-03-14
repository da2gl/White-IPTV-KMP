package com.simplevideo.whiteiptv.feature.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.simplevideo.whiteiptv.common.components.GroupDropdown
import com.simplevideo.whiteiptv.common.components.PlaylistDropdown
import com.simplevideo.whiteiptv.common.components.SearchEmptyState
import com.simplevideo.whiteiptv.common.components.SearchTopBar
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
    val pagedItems = viewModel.pagedChannels.collectAsLazyPagingItems()

    LaunchedEffect(action) {
        when (val currentAction = action) {
            is ChannelsAction.NavigateToPlayer -> {
                onNavigateToPlayer(currentAction.channelId)
                viewModel.clearAction()
            }

            else -> Unit
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
                    onQueryChange = { viewModel.obtainEvent(ChannelsEvent.OnSearchQueryChanged(it)) },
                    onClose = { viewModel.obtainEvent(ChannelsEvent.OnToggleSearch) },
                    focusRequester = focusRequester,
                    placeholder = "Search channels...",
                )
            } else {
                ChannelsTopAppBar(
                    onSearchClick = { viewModel.obtainEvent(ChannelsEvent.OnToggleSearch) },
                )
            }
        },
    ) { paddingValues ->
        ChannelsContent(
            state = state,
            pagedItems = pagedItems,
            onPlaylistSelect = { selection ->
                viewModel.obtainEvent(ChannelsEvent.OnPlaylistSelected(selection))
            },
            onGroupSelect = { group ->
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
private fun ChannelsTopAppBar(onSearchClick: () -> Unit) {
    TopAppBar(
        title = { Text("All Channels") },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        },
    )
}

@Composable
private fun ChannelsContent(
    state: ChannelsState,
    pagedItems: LazyPagingItems<ChannelEntity>,
    onPlaylistSelect: (PlaylistSelection) -> Unit,
    onGroupSelect: (ChannelGroup?) -> Unit,
    onChannelClick: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isInitialLoading = pagedItems.loadState.refresh is LoadState.Loading
    val isEmptyResult = pagedItems.loadState.refresh is LoadState.NotLoading && pagedItems.itemCount == 0

    Column(modifier = modifier.fillMaxSize()) {
        PlaylistDropdown(
            playlists = state.playlists,
            selection = state.selection,
            onPlaylistSelect = onPlaylistSelect,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        GroupDropdown(
            groups = state.groups,
            selectedGroup = state.selectedGroup,
            onGroupSelect = onGroupSelect,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        if (isInitialLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (isEmptyResult) {
            if (state.isSearchActive && state.searchQuery.isNotEmpty()) {
                SearchEmptyState(query = state.searchQuery)
            } else {
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
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    count = pagedItems.itemCount,
                    key = pagedItems.itemKey { it.id },
                ) { index ->
                    val channel = pagedItems[index]
                    if (channel != null) {
                        ChannelGridItem(
                            channel = channel,
                            onClick = { onChannelClick(channel.id) },
                            onToggleFavorite = { onToggleFavorite(channel.id) },
                        )
                    }
                }

                if (pagedItems.loadState.append is LoadState.Loading) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
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
