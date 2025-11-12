package com.simplevideo.whiteiptv.feature.channels

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChannelsScreen() {
    val viewModel = koinViewModel<ChannelsViewModel>()
//    val state by viewModel.state.collectAsState()
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("All Channels") },
//                actions = {
//                    IconButton(onClick = { /* TODO */ }) {
//                        Icon(Icons.Default.Search, contentDescription = "Search")
//                    }
//                },
//            )
//        },
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier.fillMaxSize().padding(paddingValues),
//            contentAlignment = Alignment.Center,
//        ) {
//            if (state.isLoading) {
//                CircularProgressIndicator()
//            } else if (state.channels.isEmpty()) {
//                Text("No channels found", textAlign = TextAlign.Center)
//            } else {
//                Column {
//                    LazyRow(
//                        contentPadding = PaddingValues(horizontal = 16.dp),
//                        horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    ) {
//                        items(state.categories) { category ->
//                            Button(
//                                onClick = { viewModel.obtainEvent(ChannelsEvent.OnCategorySelected(category)) },
//                                colors = if (category == state.selectedCategory) {
//                                    ButtonDefaults.buttonColors()
//                                } else {
//                                    ButtonDefaults.buttonColors(
//                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
//                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
//                                    )
//                                },
//                            ) {
//                                Text(category.name)
//                            }
//                        }
//                    }
//                    LazyVerticalGrid(
//                        columns = GridCells.Adaptive(minSize = 158.dp),
//                        contentPadding = PaddingValues(16.dp),
//                        horizontalArrangement = Arrangement.spacedBy(16.dp),
//                        verticalArrangement = Arrangement.spacedBy(16.dp),
//                    ) {
//                        items(state.channels) { channel ->
//                            ChannelItem(
//                                channel = channel,
//                                onToggleFavorite = {
//                                    viewModel.obtainEvent(ChannelsEvent.OnToggleFavorite(it))
//                                },
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
}
//
//@Composable
//fun ChannelItem(
//    channel: com.simplevideo.whiteiptv.domain.model.Channel,
//    onToggleFavorite: (String) -> Unit,
//) {
//    Column(
//        modifier = Modifier.clickable { /* TODO */ },
//        horizontalAlignment = Alignment.CenterHorizontally,
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .aspectRatio(1f)
//                .clip(MaterialTheme.shapes.medium)
//                .background(MaterialTheme.colorScheme.surfaceVariant),
//        ) {
//            Image(
//                painter = rememberImagePainter(channel.logoUrl),
//                contentDescription = channel.name,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier.fillMaxSize(),
//            )
//            IconButton(
//                onClick = { onToggleFavorite(channel.id) },
//                modifier = Modifier
//                    .align(Alignment.TopEnd)
//                    .padding(8.dp)
//                    .clip(CircleShape)
//                    .background(Color.Black.copy(alpha = 0.5f)),
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Star,
//                    contentDescription = "Favorite",
//                    tint = if (channel.isFavorite) Color.Yellow else Color.White,
//                )
//            }
//        }
//        Spacer(modifier = Modifier.height(8.dp))
//        Text(text = channel.name, style = MaterialTheme.typography.bodyMedium)
//    }
//}
