package com.simplevideo.whiteiptv.feature.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.simplevideo.whiteiptv.common.LogRecomposition
import com.simplevideo.whiteiptv.common.components.ChannelCardList
import com.simplevideo.whiteiptv.common.components.ChannelCardSquare
import com.simplevideo.whiteiptv.common.components.GradientBackground
import com.simplevideo.whiteiptv.common.components.GroupFilterChips
import com.simplevideo.whiteiptv.common.components.SearchEmptyState
import com.simplevideo.whiteiptv.common.components.StyledSearchBar
import com.simplevideo.whiteiptv.common.components.isDarkTheme
import com.simplevideo.whiteiptv.common.trackRecomposition
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.designsystem.HeaderDarkBg
import com.simplevideo.whiteiptv.domain.model.ChannelGroup
import com.simplevideo.whiteiptv.domain.model.ChannelViewMode
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

    Scaffold(
        containerColor = Color.Transparent,
    ) { paddingValues ->
        GradientBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                ChannelsHeader(
                    searchQuery = state.searchQuery,
                    onSearchQueryChange = { viewModel.obtainEvent(ChannelsEvent.OnSearchQueryChanged(it)) },
                    groups = state.groups,
                    selectedGroup = state.selectedGroup,
                    onGroupSelect = { viewModel.obtainEvent(ChannelsEvent.OnGroupSelected(it)) },
                )

                ChannelsBody(
                    state = state,
                    pagedItems = pagedItems,
                    onChannelClick = { channelId ->
                        viewModel.obtainEvent(ChannelsEvent.OnChannelClick(channelId))
                    },
                    onToggleFavorite = { channelId ->
                        viewModel.obtainEvent(ChannelsEvent.OnToggleFavorite(channelId))
                    },
                )
            }
        }
    }
}

@Composable
private fun ChannelsHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    groups: List<ChannelGroup>,
    selectedGroup: ChannelGroup?,
    onGroupSelect: (ChannelGroup?) -> Unit,
) {
    val isDark = isDarkTheme()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isDark) HeaderDarkBg.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f),
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "All Channels",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(12.dp))
        StyledSearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            placeholder = "Search channels...",
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (groups.isNotEmpty()) {
            GroupFilterChips(
                groups = groups,
                selectedGroup = selectedGroup,
                onGroupSelect = onGroupSelect,
                modifier = Modifier.padding(start = 0.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
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
    val isInitialLoading = pagedItems.loadState.refresh is LoadState.Loading
    val isEmptyResult = pagedItems.loadState.refresh is LoadState.NotLoading && pagedItems.itemCount == 0

    LogRecomposition("ChannelsContent")
    Box(modifier = modifier.fillMaxSize().trackRecomposition("ChannelsContent")) {
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
                        columns = GridCells.Fixed(2),
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
                                    subtitle = channel.tvgLanguage ?: channel.tvgCountry,
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
