package com.simplevideo.whiteiptv.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.simplevideo.whiteiptv.designsystem.CyanGradientEnd
import com.simplevideo.whiteiptv.designsystem.CyanGradientStart

private val ContinueWatchingCardShape = RoundedCornerShape(16.dp)

/**
 * Card for continue watching section on the Home screen.
 *
 * Full-width card with 16:9 aspect ratio, background image, LIVE badge,
 * channel name overlay, and a cyan gradient progress bar at the bottom.
 */
@Composable
fun ContinueWatchingCard(
    name: String,
    logoUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    category: String? = null,
    progress: Float = 0f,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.1f), ContinueWatchingCardShape),
        onClick = onClick,
        shape = ContinueWatchingCardShape,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1a2026),
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
        ) {
            val placeholderColor = Color(0xFF1a2026)
            val context = LocalPlatformContext.current
            val imageRequest = remember(logoUrl) {
                ImageRequest.Builder(context)
                    .data(logoUrl)
                    .crossfade(true)
                    .build()
            }
            AsyncImage(
                model = imageRequest,
                contentDescription = name,
                placeholder = ColorPainter(placeholderColor),
                error = ColorPainter(placeholderColor),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.5f to Color.Black.copy(alpha = 0.1f),
                            1.0f to Color.Black.copy(alpha = 0.7f),
                        ),
                    ),
            )

            LiveBadge(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                if (category != null) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color.White.copy(alpha = 0.2f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                            .height(3.dp)
                            .background(
                                Brush.horizontalGradient(listOf(CyanGradientStart, CyanGradientEnd)),
                            ),
                    )
                }
                if (progress > 0f) {
                    val minutesLeft = ((1f - progress) * 60).toInt().coerceAtLeast(1)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${minutesLeft}m left",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}
