# EPG Player Integration — Implementation Plan

## Summary

Wire EPG data into the player overlay. When EPG data is available for the current channel, show the current program name + time range and the next program name + start time. Load EPG data automatically when the player opens if not already cached.

## Files to Modify

### 1. `feature/player/mvi/PlayerMvi.kt`
Add EPG fields to PlayerState.

```kotlin
import com.simplevideo.whiteiptv.domain.model.EpgProgram

data class PlayerState(
    // ... existing fields unchanged ...
    val currentProgram: EpgProgram? = null,
    val nextProgram: EpgProgram? = null,
)
```

### 2. `feature/player/PlayerViewModel.kt`
Add EPG use cases and load/refresh EPG data.

**Changes:**
- Add `LoadEpgUseCase` and `GetCurrentProgramUseCase` as constructor parameters
- After channel loads successfully in `observeChannel()`, call `loadEpgIfNeeded()` and `fetchCurrentProgram()`
- `loadEpgIfNeeded()`: check if EPG data exists, if not and playlist has `urlTvg`, load it
- `fetchCurrentProgram()`: get current + next program for channel's tvgId, update state
- Refresh EPG program info periodically (every 60s) since programs change over time
- On channel switch (`navigateToChannel`), re-fetch current program

```kotlin
class PlayerViewModel(
    savedStateHandle: SavedStateHandle,
    private val getChannelById: GetChannelByIdUseCase,
    private val getAdjacentChannel: GetAdjacentChannelUseCase,
    private val recordWatchEvent: RecordWatchEventUseCase,
    private val loadEpg: LoadEpgUseCase,
    private val getCurrentProgram: GetCurrentProgramUseCase,
) : BaseViewModel<PlayerState, PlayerAction, PlayerEvent>(...)
```

**New private methods:**

```kotlin
private var epgRefreshJob: Job? = null

private fun loadEpgAndFetchProgram(channel: ChannelEntity) {
    viewModelScope.launch {
        // Load EPG data if needed (silently, non-blocking)
        loadEpg(channel.playlistId)
        // Fetch current/next program
        fetchCurrentProgram(channel.tvgId)
        // Start periodic refresh
        startEpgRefreshTimer(channel.tvgId)
    }
}

private suspend fun fetchCurrentProgram(tvgId: String?) {
    val (current, next) = getCurrentProgram(tvgId)
    viewState = viewState.copy(
        currentProgram = current,
        nextProgram = next,
    )
}

private fun startEpgRefreshTimer(tvgId: String?) {
    epgRefreshJob?.cancel()
    if (tvgId.isNullOrBlank()) return
    epgRefreshJob = viewModelScope.launch {
        while (true) {
            delay(EPG_REFRESH_INTERVAL_MS)
            fetchCurrentProgram(tvgId)
        }
    }
}
```

**Constant:**
```kotlin
private const val EPG_REFRESH_INTERVAL_MS = 60_000L // 1 minute
```

**In `observeChannel()` onEach:** After setting channel state, call `loadEpgAndFetchProgram(channel)`.

**In `navigateToChannel()`:** Cancel epgRefreshJob and clear EPG state before switching.

**In `onCleared()`:** Cancel epgRefreshJob.

### 3. `feature/player/components/PlayerControls.kt`
Add EPG program info display to the controls overlay.

**Changes to `PlayerControlsOverlay`:**
- Add parameters: `currentProgram: EpgProgram?`, `nextProgram: EpgProgram?`
- Below the channel name in the top bar, add EPG program info (current program title + time, next program title)

```kotlin
@Composable
fun PlayerControlsOverlay(
    channelName: String,
    isVisible: Boolean,
    isBuffering: Boolean,
    tracksInfo: TracksInfo,
    currentProgram: EpgProgram?,  // NEW
    nextProgram: EpgProgram?,     // NEW
    onBackClick: () -> Unit,
    onShowAudioTracks: () -> Unit,
    onShowSubtitles: () -> Unit,
    onShowQuality: () -> Unit,
    modifier: Modifier = Modifier,
)
```

**EPG info display (inside top bar, below channel name):**
```kotlin
// After channel name Text:
if (currentProgram != null) {
    EpgProgramInfo(
        currentProgram = currentProgram,
        nextProgram = nextProgram,
    )
}
```

### 4. `feature/player/components/EpgProgramInfo.kt` (NEW)
Composable to display current and next program info.

```kotlin
@Composable
fun EpgProgramInfo(
    currentProgram: EpgProgram,
    nextProgram: EpgProgram?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // Current program: "▶ Program Name  12:00 - 13:00"
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = currentProgram.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatTimeRange(currentProgram.startTimeMs, currentProgram.endTimeMs),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
        // Next program: "Next: Program Name  13:00"
        if (nextProgram != null) {
            Text(
                text = "Next: ${nextProgram.title} · ${formatTime(nextProgram.startTimeMs)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
```

**Time formatting helpers** (in same file or a shared util):
```kotlin
private fun formatTimeRange(startMs: Long, endMs: Long): String {
    return "${formatTime(startMs)} – ${formatTime(endMs)}"
}

private fun formatTime(epochMs: Long): String {
    // Convert UTC epoch millis to local time HH:mm
    // Use kotlinx-datetime: Instant.fromEpochMilliseconds(epochMs)
    //   .toLocalDateTime(TimeZone.currentSystemDefault())
    //   .let { "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}" }
}
```

### 5. `feature/player/PlayerScreen.kt`
Pass new EPG params from state to PlayerControlsOverlay.

**Find the `PlayerControlsOverlay` call and add:**
```kotlin
currentProgram = state.currentProgram,
nextProgram = state.nextProgram,
```

### 6. `di/KoinModule.kt`
No changes needed — `LoadEpgUseCase` and `GetCurrentProgramUseCase` are already registered. Koin will auto-inject them into `PlayerViewModel`.

## Implementation Order

1. `PlayerMvi.kt` — Add `currentProgram` and `nextProgram` to PlayerState
2. `EpgProgramInfo.kt` — Create new composable component
3. `PlayerControls.kt` — Add EPG params and display
4. `PlayerViewModel.kt` — Add EPG use cases, load logic, periodic refresh
5. `PlayerScreen.kt` — Pass EPG state to controls overlay

## Error Handling

Per spec (`docs/features/epg.md`):
- No urlTvg in playlist → EPG features hidden (currentProgram/nextProgram stay null)
- XMLTV download fails → EPG features hidden, no error shown (LoadEpgUseCase catches silently)
- Channel has no tvg-id match → No program info shown (GetCurrentProgramUseCase returns nulls)

## Build Verification

```bash
./gradlew :composeApp:assembleDebug --no-daemon
```
