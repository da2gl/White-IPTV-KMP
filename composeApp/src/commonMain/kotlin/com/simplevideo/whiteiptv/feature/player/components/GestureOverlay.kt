package com.simplevideo.whiteiptv.feature.player.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Gesture zone types for the player
 */
enum class GestureZone {
    LEFT, // Brightness control
    CENTER, // Channel switching
    RIGHT, // Volume control
}

/**
 * Gesture overlay for volume, brightness, and channel switching
 *
 * Zones:
 * - Left third: Brightness (vertical swipe)
 * - Center third: Channel switching (vertical swipe) or tap to show/hide controls
 * - Right third: Volume (vertical swipe)
 */
@Composable
fun GestureOverlay(
    currentVolume: Float,
    currentBrightness: Float,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onChannelChange: (Int) -> Unit, // -1 for previous, +1 for next
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var activeIndicator by remember { mutableStateOf<GestureIndicator?>(null) }
    var indicatorValue by remember { mutableFloatStateOf(0f) }
    var currentZone by remember { mutableStateOf<GestureZone?>(null) }
    var accumulatedDrag by remember { mutableFloatStateOf(0f) }

    // Sensitivity settings
    val dragSensitivity = 300f // pixels for full range (0 to 1)
    val channelSwitchThreshold = 100f // pixels to trigger channel switch

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        // TODO(human): Determine zone based on touch position
                        val zoneWidth = size.width / 3f
                        currentZone = when {
                            offset.x < zoneWidth -> GestureZone.LEFT
                            offset.x < zoneWidth * 2 -> GestureZone.CENTER
                            else -> GestureZone.RIGHT
                        }

                        // Initialize indicator based on zone
                        when (currentZone) {
                            GestureZone.LEFT -> {
                                activeIndicator = GestureIndicators.brightness
                                indicatorValue = currentBrightness
                            }

                            GestureZone.RIGHT -> {
                                activeIndicator = GestureIndicators.volume
                                indicatorValue = currentVolume
                            }

                            GestureZone.CENTER -> {
                                // Don't show indicator until threshold reached
                                activeIndicator = null
                            }

                            null -> {}
                        }
                        accumulatedDrag = 0f
                    },
                    onDragEnd = {
                        activeIndicator = null
                        currentZone = null
                        accumulatedDrag = 0f
                    },
                    onDragCancel = {
                        activeIndicator = null
                        currentZone = null
                        accumulatedDrag = 0f
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()

                        when (currentZone) {
                            GestureZone.LEFT -> {
                                // Brightness: swipe up = increase, swipe down = decrease
                                val delta = -dragAmount / dragSensitivity
                                val newValue = (indicatorValue + delta).coerceIn(0f, 1f)
                                indicatorValue = newValue
                                onBrightnessChange(newValue)
                            }

                            GestureZone.RIGHT -> {
                                // Volume: swipe up = increase, swipe down = decrease
                                val delta = -dragAmount / dragSensitivity
                                val newValue = (indicatorValue + delta).coerceIn(0f, 1f)
                                indicatorValue = newValue
                                onVolumeChange(newValue)
                            }

                            GestureZone.CENTER -> {
                                // Channel switching: accumulate drag until threshold
                                accumulatedDrag += dragAmount

                                if (accumulatedDrag > channelSwitchThreshold) {
                                    // Swipe down = next channel
                                    activeIndicator = GestureIndicators.channelNext
                                    onChannelChange(1)
                                    accumulatedDrag = 0f
                                } else if (accumulatedDrag < -channelSwitchThreshold) {
                                    // Swipe up = previous channel
                                    activeIndicator = GestureIndicators.channelPrevious
                                    onChannelChange(-1)
                                    accumulatedDrag = 0f
                                }
                            }

                            null -> {}
                        }
                    },
                )
            },
    ) {
        // Visual indicator for current gesture
        AnimatedVisibility(
            visible = activeIndicator != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            activeIndicator?.let { indicator ->
                GestureIndicatorView(
                    indicator = indicator,
                    value = indicatorValue,
                )
            }
        }
    }
}

/**
 * Data class for gesture indicator display
 */
data class GestureIndicator(
    val type: GestureIndicatorType,
    val icon: ImageVector,
    val label: String,
)

enum class GestureIndicatorType {
    BRIGHTNESS,
    VOLUME,
    CHANNEL,
}

@Composable
private fun GestureIndicatorView(
    indicator: GestureIndicator,
    value: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                Color.Black.copy(alpha = 0.7f),
                RoundedCornerShape(12.dp),
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = indicator.icon,
                contentDescription = indicator.label,
                tint = Color.White,
                modifier = Modifier.size(48.dp),
            )
            if (indicator.type != GestureIndicatorType.CHANNEL) {
                LinearProgressIndicator(
                    progress = { value.coerceIn(0f, 1f) },
                    modifier = Modifier.padding(top = 8.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                )
            } else {
                Text(
                    text = indicator.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

/**
 * Helper to get default indicators
 */
object GestureIndicators {
    val brightness = GestureIndicator(
        type = GestureIndicatorType.BRIGHTNESS,
        icon = Icons.Default.BrightnessHigh,
        label = "Brightness",
    )

    val volume = GestureIndicator(
        type = GestureIndicatorType.VOLUME,
        icon = Icons.AutoMirrored.Filled.VolumeUp,
        label = "Volume",
    )

    val channelNext = GestureIndicator(
        type = GestureIndicatorType.CHANNEL,
        icon = Icons.Default.KeyboardArrowDown,
        label = "Next Channel",
    )

    val channelPrevious = GestureIndicator(
        type = GestureIndicatorType.CHANNEL,
        icon = Icons.Default.KeyboardArrowUp,
        label = "Previous Channel",
    )
}
