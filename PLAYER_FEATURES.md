# Player Features Roadmap

## ✅ Completed Features

### Core Player

- [x] ExoPlayer integration with Media3 Compose UI
- [x] Cronet network stack with Play Services (HTTP/2, QUIC)
- [x] Switchable network stack (`useCronet` flag in PlayerConfig)
- [x] IPTV-optimized buffering (DefaultLoadControl)
- [x] Live playback speed control
- [x] Auto-recovery from behind live window error
- [x] Audio focus handling
- [x] Wake lock for network
- [x] Keep screen on during playback

### Track Selection

- [x] Audio track selection
- [x] Subtitle track selection
- [x] Video quality selection (with Auto option)
- [x] TracksInfo data classes
- [x] TracksInfoMapper for ExoPlayer tracks

### UI Components

- [x] PlayerControlsOverlay (top/bottom gradients)
- [x] LiveIndicator (red LIVE badge / gray offset)
- [x] TrackSelectionDialog (ModalBottomSheet)
- [x] Tap to toggle controls
- [x] Channel name display
- [x] Back navigation

## ❌ Pending Features

### UI Enhancements (Priority: High)

- [ ] **Center play/pause button** - Large circular button in center
- [ ] **Auto-hide controls** - Hide after 3 seconds of inactivity
- [ ] **Loading/buffering progress** - Show percentage or progress bar

### Gesture Controls (Priority: Medium)

- [ ] **Volume control** - Vertical swipe on right side of screen
- [ ] **Brightness control** - Vertical swipe on left side of screen
- [ ] **Double-tap seek** - Skip forward/backward (not typical for live IPTV)

### Channel Navigation (Priority: High)

- [ ] **Channel switching in player** - Swipe up/down or buttons to switch channels
- [ ] **Channel list overlay** - Show mini channel list while playing
- [ ] **Previous/Next channel buttons** - Quick navigation
- [ ] **Channel number input** - Type channel number to jump directly

### Additional Features (Priority: Low)

- [ ] **Aspect ratio selector** - Fit/Fill/Zoom/16:9/4:3
- [ ] **PiP support** - Picture-in-Picture for Android 8+
- [ ] **Lock screen** - Prevent accidental touches
- [ ] **Sleep timer** - Auto-stop after set time
- [ ] **Favorite toggle** - Add/remove from favorites while watching
- [ ] **EPG overlay** - Show current/next program info

## Implementation Notes

### Channel Switching Options

#### Option 1: Swipe Gestures

```kotlin
// Swipe up = next channel, swipe down = previous channel
Modifier.pointerInput(Unit) {
    detectVerticalDragGestures { _, dragAmount ->
        if (dragAmount > threshold) onPreviousChannel()
        if (dragAmount < -threshold) onNextChannel()
    }
}
```

#### Option 2: Overlay Buttons

```kotlin
// Add to PlayerControlsOverlay
IconButton(onClick = onPreviousChannel) {
    Icon(Icons.Default.SkipPrevious, "Previous")
}
IconButton(onClick = onNextChannel) {
    Icon(Icons.Default.SkipNext, "Next")
}
```

#### Option 3: Mini Channel List

```kotlin
// Slide-in panel with channel list
AnimatedVisibility(visible = showChannelList) {
    LazyColumn {
        items(channels) { channel ->
            ChannelItem(
                channel = channel,
                isPlaying = channel.id == currentChannelId,
                onClick = { onChannelSelect(channel.id) }
            )
        }
    }
}
```

### Auto-hide Implementation

```kotlin
LaunchedEffect(controlsVisible) {
    if (controlsVisible) {
        delay(3000)
        onEvent(PlayerEvent.OnHideControls)
    }
}
```

### Gesture Controls Implementation

```kotlin
// Using Modifier.pointerInput for custom gestures
Modifier
    .pointerInput(Unit) {
        detectVerticalDragGestures(
            onDragStart = { offset ->
                isVolumeGesture = offset.x > size.width / 2
            },
            onDrag = { _, dragAmount ->
                if (isVolumeGesture) {
                    adjustVolume(-dragAmount / size.height)
                } else {
                    adjustBrightness(-dragAmount / size.height)
                }
            }
        )
    }
```

## Architecture Considerations

### Channel Switching

To implement channel switching, we need:

1. **PlayerViewModel** needs access to:
    - Current playlist/group channels list
    - Current channel index in the list

2. **Navigation options**:
    - Pass channels list to PlayerScreen via navigation args (limited by size)
    - Query channels from repository in ViewModel
    - Use shared state via Koin/StateFlow

3. **Recommended approach**:
    - Add `GetChannelsByGroupIdUseCase` or `GetAdjacentChannelsUseCase`
    - Store current playlist context in PlayerState
    - Implement `OnNextChannel` / `OnPreviousChannel` events
