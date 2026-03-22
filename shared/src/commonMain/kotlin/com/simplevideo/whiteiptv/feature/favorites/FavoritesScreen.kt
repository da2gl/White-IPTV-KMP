package com.simplevideo.whiteiptv.feature.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.simplevideo.whiteiptv.common.components.ChannelCardList
import com.simplevideo.whiteiptv.common.components.ChannelCardSquare
import com.simplevideo.whiteiptv.common.components.GradientBackground
import com.simplevideo.whiteiptv.common.components.PlaylistFilterChips
import com.simplevideo.whiteiptv.common.components.isDarkTheme
import com.simplevideo.whiteiptv.designsystem.FavoritePink
import com.simplevideo.whiteiptv.designsystem.HeaderDarkBg
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

    Scaffold(
        containerColor = Color.Transparent,
    ) { paddingValues ->
        GradientBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                FavoritesHeader(
                    state = state,
                    onPlaylistSelect = { selection ->
                        viewModel.obtainEvent(FavoritesEvent.OnPlaylistSelected(selection))
                    },
                )

                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (state.channels.isEmpty()) {
                    FavoritesEmptyState()
                } else {
                    // Favorites count
                    Text(
                        text = "${state.channels.size} favorites",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    ChannelsList(state = state, onEvent = viewModel::obtainEvent)
                }
            }
        }
    }
}

@Composable
private fun FavoritesHeader(
    state: FavoritesState,
    onPlaylistSelect: (com.simplevideo.whiteiptv.domain.model.PlaylistSelection) -> Unit,
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = FavoritePink,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.padding(start = 10.dp))
            Text(
                text = "Favorites",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (state.playlists.size > 1) {
            PlaylistFilterChips(
                playlists = state.playlists,
                selection = state.selection,
                onPlaylistSelect = onPlaylistSelect,
                modifier = Modifier.padding(start = 0.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
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
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
                    )
                }
            }
        }
    }
}
