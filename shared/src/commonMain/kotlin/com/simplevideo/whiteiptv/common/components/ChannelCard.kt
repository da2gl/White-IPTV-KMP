package com.simplevideo.whiteiptv.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.simplevideo.whiteiptv.common.LogRecomposition
import com.simplevideo.whiteiptv.common.trackRecomposition
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.designsystem.LiveCyan
import com.simplevideo.whiteiptv.designsystem.PlaceholderColors
import kotlin.math.abs

private val CardShape = RoundedCornerShape(16.dp)

/**
 * Square channel card for grid layouts (Home favorites, Favorites, Channels grid).
 *
 * Features 1:1 aspect ratio, background image, favorite toggle, and channel name below card.
 * Shows a colored letter placeholder when no logo is available.
 */
@Composable
fun ChannelCardSquare(
    name: String,
    logoUrl: String?,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    category: String? = null,
    showLiveBadge: Boolean = false,
    showFavoriteButton: Boolean = true,
) {
    LogRecomposition("ChannelCardSquare")
    val isDark = isDarkTheme()
    Column(
        modifier = modifier.fillMaxWidth().trackRecomposition("ChannelCardSquare"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF3F4F6),
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isDark) 0.dp else 1.dp,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFE5E7EB),
                    RoundedCornerShape(20.dp),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center,
            ) {
                if (logoUrl.isNullOrBlank()) {
                    ChannelPlaceholder(
                        name = name,
                        textStyle = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                        showName = true,
                    )
                } else {
                    AsyncImage(
                        model = logoUrl,
                        contentDescription = name,
                        modifier = Modifier
                            .size(80.dp),
                        contentScale = ContentScale.Fit,
                    )
                }

                if (showFavoriteButton) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(36.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (isFavorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Outlined.FavoriteBorder
                                },
                                contentDescription = null,
                                tint = if (isFavorite) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.White
                                },
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }

                if (showLiveBadge) {
                    LiveBadge(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        if (category != null) {
            Text(
                text = category,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * List-style channel card for list layouts.
 *
 * Row layout with logo, channel name, optional subtitle, and favorite toggle.
 * Shows a colored letter placeholder when no logo is available.
 */
@Composable
fun ChannelCardList(
    name: String,
    logoUrl: String?,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showFavoriteButton: Boolean = true,
) {
    LogRecomposition("ChannelCardList")
    val isDark = isDarkTheme()
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .trackRecomposition("ChannelCardList")
            .border(
                1.dp,
                if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFE5E7EB),
                CardShape,
            ),
        onClick = onClick,
        shape = CardShape,
        color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFEEF0F3),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (logoUrl.isNullOrBlank()) {
                    ChannelPlaceholder(
                        name = name,
                        textStyle = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    AsyncImage(
                        model = logoUrl,
                        contentDescription = name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    color = if (isDark) Color.White else Color(0xFF101828),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF6A7282),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (showFavoriteButton) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            if (isDark) Color.White.copy(alpha = 0.3f) else Color(0xFF6A7282)
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun LiveBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = LiveCyan,
    ) {
        Text(
            text = "LIVE",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

/**
 * Builds a subtitle string from channel metadata (language, country).
 */
fun channelSubtitle(channel: ChannelEntity): String? {
    val parts = listOfNotNull(
        channel.tvgLanguage,
        channel.tvgCountry,
    ).filter { it.isNotBlank() }
    return parts.joinToString(" · ").ifEmpty { null }
}

@Composable
private fun ChannelPlaceholder(
    name: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    showName: Boolean = false,
) {
    val colorIndex = abs(name.hashCode()) % PlaceholderColors.size
    val backgroundColor = PlaceholderColors[colorIndex]

    Box(
        modifier = modifier.background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        if (showName) {
            Text(
                text = name.take(10),
                style = textStyle,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(4.dp),
            )
        } else {
            val letter = name.firstOrNull()?.uppercase() ?: "?"
            Text(
                text = letter,
                style = textStyle,
                color = Color.White,
            )
        }
    }
}
