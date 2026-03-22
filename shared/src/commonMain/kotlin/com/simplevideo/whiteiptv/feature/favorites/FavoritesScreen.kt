package com.simplevideo.whiteiptv.feature.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.StarOutline
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.simplevideo.whiteiptv.common.components.ChannelCardList
import com.simplevideo.whiteiptv.common.components.ChannelCardSquare
import com.simplevideo.whiteiptv.common.components.PlaylistFilterChips
import com.simplevideo.whiteiptv.common.components.SearchEmptyState
import com.simplevideo.whiteiptv.common.components.SearchTopBar
import com.simplevideo.whiteiptv.common.components.channelSubtitle
import com.simplevideo.whiteiptv.designsystem.FavoritePink
import com.simplevideo.whiteiptv.domain.model.ChannelViewMode
import com.simplevideo.whiteiptv.feature.favorites.mvi.FavoritesAction
import com.simplevideo.whiteiptv.feature.favorites.mvi.FavoritesEvent
import com.simplevideo.whiteiptv.feature.favorites.mvi.FavoritesState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FavoritesScreen(
    onNavigateToPlayer: (Long) -> Unit,
) {
    val viewModel = koinViewModel<FavoritesViewModel>()
    val state by viewModel.viewStates().collectAsState()
    val action by viewModel.viewActions().collectAsState(initial = null)

    LaunchedEffect(action) {
        when (val currentAction = action) {
            is FavoritesAction.NavigateToPlayer -> {
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
                        viewModel.obtainEvent(FavoritesEvent.OnSearchQueryChanged(it))
                    },
                    onClose = { viewModel.obtainEvent(FavoritesEvent.OnToggleSearch) },
                    focusRequester = focusRequester,
                    placeholder = "Search favorites...",
                )
            } else {
                TopAppBar(
                    title = { Text("Favorites") },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.obtainEvent(FavoritesEvent.OnToggleSearch)
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
                        viewModel.obtainEvent(FavoritesEvent.OnPlaylistSelected(selection))
                    },
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.channels.isEmpty()) {
                if (state.searchQuery.isNotEmpty()) {
                    SearchEmptyState(query = state.searchQuery)
                } else {
                    FavoritesEmptyState()
                }
            } else {
                ChannelsList(state = state, onEvent = viewModel::obtainEvent)
            }
        }
    }
}

@Composable
private fun FavoritesEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(FavoritePink.copy(alpha = 0.1f)),
                )
                Box(
                    Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(FavoritePink.copy(alpha = 0.2f)),
                )
                Icon(
                    Icons.Outlined.StarOutline,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = FavoritePink,
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "No favorites yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Tap the heart icon on any channel to add it here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ChannelsList(
    state: FavoritesState,
    onEvent: (FavoritesEvent) -> Unit,
) {
    when (state.channelViewMode) {
        ChannelViewMode.Grid -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.channels, key = { it.id }) { channel ->
                    ChannelCardSquare(
                        name = channel.name,
                        logoUrl = channel.logoUrl,
                        isFavorite = true,
                        onClick = { onEvent(FavoritesEvent.OnChannelClick(channel.id)) },
                        onToggleFavorite = { onEvent(FavoritesEvent.OnToggleFavorite(channel.id)) },
                        showFavoriteButton = true,
                    )
                }
            }
        }
        ChannelViewMode.List -> {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.channels, key = { it.id }) { channel ->
                    ChannelCardList(
                        name = channel.name,
                        logoUrl = channel.logoUrl,
                        isFavorite = true,
                        onClick = { onEvent(FavoritesEvent.OnChannelClick(channel.id)) },
                        onToggleFavorite = { onEvent(FavoritesEvent.OnToggleFavorite(channel.id)) },
                        showFavoriteButton = true,
                        subtitle = channelSubtitle(channel),
                    )
                }
            }
        }
    }
}
