package com.simplevideo.whiteiptv.feature.channels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.simplevideo.whiteiptv.common.components.ChannelCardList
import com.simplevideo.whiteiptv.common.components.ChannelCardSquare
import com.simplevideo.whiteiptv.common.components.GroupFilterChips
import com.simplevideo.whiteiptv.common.components.PlaylistFilterChips
import com.simplevideo.whiteiptv.common.components.SearchEmptyState
import com.simplevideo.whiteiptv.common.components.SearchTopBar
import com.simplevideo.whiteiptv.common.components.channelSubtitle
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.domain.model.ChannelViewMode
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsAction
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsEvent
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChannelsScreen(
    onNavigateToPlayer: (Long) -> Unit,
    openSearch: Boolean = false,
) {
    val viewModel = koinViewModel<ChannelsViewModel>()
    val state by viewModel.viewStates().collectAsState()
    val action by viewModel.viewActions().collectAsState(initial = null)
    val pagedItems = viewModel.pagedChannels.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        if (openSearch && !state.isSearchActive) {
            viewModel.obtainEvent(ChannelsEvent.OnToggleSearch)
        }
    }

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

    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            if (state.isSearchActive) {
                SearchTopBar(
                    query = state.searchQuery,
                    onQueryChange = {
                        viewModel.obtainEvent(ChannelsEvent.OnSearchQueryChanged(it))
                    },
                    onClose = { viewModel.obtainEvent(ChannelsEvent.OnToggleSearch) },
                    focusRequester = focusRequester,
                    placeholder = "Search channels...",
                )
            } else {
                TopAppBar(
                    title = { Text("Channels") },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.obtainEvent(ChannelsEvent.OnToggleSearch)
                            },
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    },
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (state.playlists.size > 1) {
                PlaylistFilterChips(
                    playlists = state.playlists,
                    selection = state.selection,
                    onPlaylistSelect = { selection ->
                        viewModel.obtainEvent(ChannelsEvent.OnPlaylistSelected(selection))
                    },
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            if (state.groups.isNotEmpty()) {
                GroupFilterChips(
                    groups = state.groups,
                    selectedGroup = state.selectedGroup,
                    onGroupSelect = {
                        viewModel.obtainEvent(ChannelsEvent.OnGroupSelected(it))
                    },
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
            ChannelsBody(
                state = state,
                pagedItems = pagedItems,
                onChannelClick = { channelId ->
                    viewModel.obtainEvent(ChannelsEvent.OnChannelClick(channelId))
                },
                onToggleFavorite = { channelId ->
                    viewModel.obtainEvent(ChannelsEvent.OnToggleFavorite(channelId))
                },
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun ChannelsBody(
    state: ChannelsState,
    pagedItems: LazyPagingItems<ChannelEntity>,
    onChannelClick: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isInitialLoading = pagedItems.loadState.refresh is LoadState.Loading && pagedItems.itemCount == 0
    val isEmptyResult = pagedItems.loadState.refresh is LoadState.NotLoading && pagedItems.itemCount == 0

    Box(modifier = modifier.fillMaxSize()) {
        if (isInitialLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (isEmptyResult) {
            if (state.searchQuery.isNotEmpty()) {
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
            when (state.channelViewMode) {
                ChannelViewMode.Grid -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            count = pagedItems.itemCount,
                            key = pagedItems.itemKey { it.id },
                        ) { index ->
                            val channel = pagedItems[index]
                            if (channel != null) {
                                ChannelCardSquare(
                                    name = channel.name,
                                    logoUrl = channel.logoUrl,
                                    isFavorite = channel.isFavorite,
                                    onClick = { onChannelClick(channel.id) },
                                    onToggleFavorite = { onToggleFavorite(channel.id) },
                                )
                            }
                        }

                        if (pagedItems.loadState.append is LoadState.Loading) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
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
                ChannelViewMode.List -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            count = pagedItems.itemCount,
                            key = pagedItems.itemKey { it.id },
                        ) { index ->
                            val channel = pagedItems[index]
                            if (channel != null) {
                                ChannelCardList(
                                    name = channel.name,
                                    logoUrl = channel.logoUrl,
                                    isFavorite = channel.isFavorite,
                                    onClick = { onChannelClick(channel.id) },
                                    onToggleFavorite = { onToggleFavorite(channel.id) },
                                    subtitle = channelSubtitle(channel),
                                )
                            }
                        }

                        if (pagedItems.loadState.append is LoadState.Loading) {
                            item {
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
    }
}
