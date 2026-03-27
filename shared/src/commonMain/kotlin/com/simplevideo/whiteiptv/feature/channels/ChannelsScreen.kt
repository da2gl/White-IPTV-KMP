package com.simplevideo.whiteiptv.feature.channels

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
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
                onDeleteChannel = { channelId ->
                    viewModel.obtainEvent(ChannelsEvent.OnDeleteChannel(channelId))
                },
                onRenameChannel = { channelId, newName ->
                    viewModel.obtainEvent(ChannelsEvent.OnRenameChannel(channelId, newName))
                },
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChannelsBody(
    state: ChannelsState,
    pagedItems: LazyPagingItems<ChannelEntity>,
    onChannelClick: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onDeleteChannel: (Long) -> Unit,
    onRenameChannel: (Long, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isInitialLoading = pagedItems.loadState.refresh is LoadState.Loading && pagedItems.itemCount == 0
    val isEmptyResult = pagedItems.loadState.refresh is LoadState.NotLoading && pagedItems.itemCount == 0

    var channelToDelete by remember { mutableStateOf<ChannelEntity?>(null) }
    var channelToRename by remember { mutableStateOf<ChannelEntity?>(null) }

    channelToDelete?.let { channel ->
        DeleteChannelDialog(
            channelName = channel.name,
            onConfirm = {
                onDeleteChannel(channel.id)
                channelToDelete = null
            },
            onDismiss = { channelToDelete = null },
        )
    }

    channelToRename?.let { channel ->
        RenameChannelDialog(
            currentName = channel.name,
            onConfirm = { newName ->
                onRenameChannel(channel.id, newName)
                channelToRename = null
            },
            onDismiss = { channelToRename = null },
        )
    }

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
                                ChannelGridItem(
                                    channel = channel,
                                    onChannelClick = onChannelClick,
                                    onToggleFavorite = onToggleFavorite,
                                    onRename = { channelToRename = channel },
                                    onDelete = { channelToDelete = channel },
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
                                ChannelListItem(
                                    channel = channel,
                                    onChannelClick = onChannelClick,
                                    onToggleFavorite = onToggleFavorite,
                                    onRename = { channelToRename = channel },
                                    onDelete = { channelToDelete = channel },
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

@Composable
private fun ChannelGridItem(
    channel: ChannelEntity,
    onChannelClick: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        ChannelCardSquare(
            name = channel.name,
            logoUrl = channel.logoUrl,
            isFavorite = channel.isFavorite,
            onClick = { onChannelClick(channel.id) },
            onToggleFavorite = { onToggleFavorite(channel.id) },
            onLongClick = { showMenu = true },
        )

        ChannelContextMenu(
            expanded = showMenu,
            onDismiss = { showMenu = false },
            onRename = {
                showMenu = false
                onRename()
            },
            onDelete = {
                showMenu = false
                onDelete()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChannelListItem(
    channel: ChannelEntity,
    onChannelClick: (Long) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        SwipeableChannelItem(
            onSwipeRight = onRename,
            onSwipeLeft = onDelete,
        ) {
            ChannelCardList(
                name = channel.name,
                logoUrl = channel.logoUrl,
                isFavorite = channel.isFavorite,
                onClick = { onChannelClick(channel.id) },
                onToggleFavorite = { onToggleFavorite(channel.id) },
                onLongClick = { showMenu = true },
                subtitle = channelSubtitle(channel),
            )
        }

        ChannelContextMenu(
            expanded = showMenu,
            onDismiss = { showMenu = false },
            onRename = {
                showMenu = false
                onRename()
            },
            onDelete = {
                showMenu = false
                onDelete()
            },
        )
    }
}

@Composable
private fun ChannelContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        DropdownMenuItem(
            text = { Text("Rename") },
            onClick = onRename,
        )
        DropdownMenuItem(
            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
            onClick = onDelete,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableChannelItem(
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    content: @Composable () -> Unit,
) {
    @Suppress("DEPRECATION")
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> onSwipeRight()
                SwipeToDismissBoxValue.EndToStart -> onSwipeLeft()
                SwipeToDismissBoxValue.Settled -> {}
            }
            false
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.25f },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val isSettled = dismissState.targetValue == SwipeToDismissBoxValue.Settled

            val color = when {
                isSettled -> Color.Transparent
                direction == SwipeToDismissBoxValue.StartToEnd -> Color(0xFF1565C0)
                else -> Color(0xFFD32F2F)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Alignment.CenterStart
                } else {
                    Alignment.CenterEnd
                },
            ) {
                if (!isSettled) {
                    Icon(
                        imageVector = if (direction == SwipeToDismissBoxValue.StartToEnd) {
                            Icons.Default.Edit
                        } else {
                            Icons.Default.Delete
                        },
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
            }
        },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
    ) {
        content()
    }
}

@Composable
private fun RenameChannelDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(currentName) }
    val renameFocusRequester = remember { FocusRequester() }
    val isDark = com.simplevideo.whiteiptv.common.components.isDarkTheme()

    LaunchedEffect(Unit) {
        renameFocusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Color(0xFF1E2830) else Color.White,
        title = { Text("Rename channel") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(renameFocusRequester),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim()) },
                enabled = name.isNotBlank() && name.trim() != currentName,
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
private fun DeleteChannelDialog(
    channelName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isDark = com.simplevideo.whiteiptv.common.components.isDarkTheme()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Color(0xFF1E2830) else Color.White,
        title = { Text("Delete channel") },
        text = { Text("Delete \"$channelName\"?") },
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
