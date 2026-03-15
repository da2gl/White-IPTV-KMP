# Chromecast (Android) + AirPlay (iOS) — Implementation Plan

## Summary

Enable casting the current IPTV stream to external displays. On Android, integrate the Google Cast SDK to send the stream URL to Chromecast devices via `CastPlayer` (Media3). On iOS, leverage AVPlayer's built-in AirPlay support via the already-implemented `AVRoutePickerView`. The scope is minimal viable casting: send the stream URL, show cast state in the player UI, pause/resume local playback when casting starts/stops. No custom receiver app, no fancy remote controls beyond play/pause.

## Decisions Made

### 1. Use Media3 CastPlayer instead of raw RemoteMediaClient

- **Decision**: Use `androidx.media3:media3-cast` `CastPlayer` to send media to Cast devices.
- **Rationale**: CastPlayer implements Media3's `Player` interface, which aligns with the existing ExoPlayer architecture. It handles session management, media loading, and state callbacks uniformly. The dependency `media3-cast` is already declared in `libs.versions.toml` and `shared/build.gradle.kts`.
- **Alternatives considered**: Using `RemoteMediaClient` directly — more boilerplate, no Player interface compatibility; using `SessionManagerListener` with manual MediaInfo construction — lower level, more error-prone.

### 2. MainActivity extends AppCompatActivity

- **Decision**: Change `MainActivity` from `ComponentActivity` to `AppCompatActivity`.
- **Rationale**: `MediaRouteButton.showDialog()` internally requires `FragmentActivity` to show the `MediaRouteChooserDialogFragment`. `AppCompatActivity` extends `FragmentActivity`. The `androidx-appcompat` dependency already exists in `libs.versions.toml` (version 1.7.1). This is the standard approach for Cast-enabled Android apps.
- **Alternatives considered**: Using a custom dialog — fragile, fights the SDK; wrapping in a new FragmentActivity — breaks single-activity architecture.

### 3. Lazy CastContext initialization

- **Decision**: Initialize `CastContext` lazily on first use, not eagerly in `WhiteIPTVApplication`.
- **Rationale**: Cast SDK initialization is heavy (~200ms). The player screen is the only consumer. Lazy init avoids slowing down app startup. `CastContext.getSharedInstance(context)` is safe to call multiple times (idempotent after first init).
- **Alternatives considered**: Eager init in Application.onCreate — simpler but adds to cold start time.

### 4. Cast session management via a dedicated CastSessionManager

- **Decision**: Create a `CastSessionManager` class in `androidMain/platform/cast/` that wraps `CastContext`, `SessionManager`, and `CastPlayer`. Expose cast state as a `StateFlow<CastState>`.
- **Rationale**: Centralizes session lifecycle, makes cast state observable from the player screen, and keeps `ExoVideoPlayer` clean. The manager handles the handoff: when a Cast session starts, pause local ExoPlayer and load media on CastPlayer; when session ends, resume local playback.
- **Alternatives considered**: Putting everything in ExoVideoPlayer — violates SRP, makes it harder to test.

### 5. AirPlay: no additional code needed for iOS

- **Decision**: The existing `AirPlayButton` + `AVRoutePickerView` implementation is sufficient. AVPlayer automatically routes audio/video to AirPlay devices when the user selects one via the route picker. No code changes needed on the iOS side beyond verifying it works.
- **Rationale**: AVPlayer handles AirPlay natively. The `AVRoutePickerView` already exists with `prioritizesVideoDevices = true`. The `AVPlayerWrapper` uses `AVPlayer` directly, and AirPlay routing is handled at the system level.
- **Alternatives considered**: Adding `allowsExternalPlayback` configuration — it's `true` by default on AVPlayer, no change needed.

### 6. Cast button visibility: always show when on Android player

- **Decision**: Always show the `CastButton` (MediaRouteButton) in the player bottom bar on Android. The MediaRouteButton itself handles visibility — it only becomes visible when Cast-compatible devices are discovered on the network.
- **Rationale**: `MediaRouteButton` has built-in device discovery behavior. When no devices are found, the button is invisible (GONE). When a device is found, it appears. This is the standard Google-recommended UX.
- **Alternatives considered**: Manually tracking device availability via `MediaRouter.addCallback` — redundant, since the button does this already.

### 7. Cast state reflected in PlayerState

- **Decision**: Add `isCasting: Boolean` to `PlayerState` (already exists, currently unused). When casting, the local player surface shows a "Casting to [device]" message instead of video. Controls remain functional (back, channel switch).
- **Rationale**: Users need visual feedback that casting is active. Channel switching while casting should load the new URL on the Cast device.
- **Alternatives considered**: Navigating away from the player — bad UX, user loses controls.

### 8. Error handling during Cast

- **Decision**: On Cast connection failure or mid-stream disconnect, resume local playback automatically. Show a brief snackbar/toast "Cast disconnected" via PlayerAction.
- **Rationale**: Users expect seamless fallback. IPTV streams are live, so there's no position to restore — just restart local playback at the current channel's URL.
- **Alternatives considered**: Showing an error dialog — too disruptive for a recoverable situation.

### 9. No custom Cast receiver app

- **Decision**: Use `CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID` (already configured in `CastOptionsProvider`).
- **Rationale**: The default media receiver supports HLS and basic media URLs, which covers most IPTV streams. A custom receiver would only be needed for DRM or specialized UI, neither of which is in scope.

## Current State

### What already exists:

| Component | File | Status |
|---|---|---|
| CastButton expect | `shared/src/commonMain/.../platform/CastButton.kt` | Complete |
| CastButton Android actual | `shared/src/androidMain/.../platform/CastButton.kt` | Complete (uses MediaRouteButton + CastButtonFactory) |
| CastButton iOS actual | `shared/src/iosMain/.../platform/CastButton.kt` | Complete (no-op) |
| AirPlayButton expect | `shared/src/commonMain/.../platform/AirPlayButton.kt` | Complete |
| AirPlayButton Android actual | `shared/src/androidMain/.../platform/AirPlayButton.kt` | Complete (no-op) |
| AirPlayButton iOS actual | `shared/src/iosMain/.../platform/AirPlayButton.kt` | Complete (AVRoutePickerView) |
| CastOptionsProvider | `shared/src/androidMain/.../platform/cast/CastOptionsProvider.kt` | Complete |
| AndroidManifest meta-data | `androidApp/src/main/AndroidManifest.xml:29-30` | Complete (references CastOptionsProvider) |
| CastButtonTheme style | `shared/src/androidMain/res/values/styles.xml` | Complete |
| media3-cast dependency | `gradle/libs.versions.toml:77` | Declared |
| play-services-cast-framework | `gradle/libs.versions.toml:78` | Declared |
| androidx-mediarouter | `gradle/libs.versions.toml:79` | Declared |
| All 3 deps in shared build | `shared/build.gradle.kts:75-78` | Declared in androidMain.dependencies |
| PlayerState.isCasting | `shared/src/commonMain/.../feature/player/mvi/PlayerMvi.kt:21` | Field exists, unused |
| CastButton in PlayerControls | `shared/src/commonMain/.../feature/player/components/PlayerControls.kt:246-247` | Commented out with TODO |

### What does NOT exist:

1. `CastSessionManager` — orchestrates Cast session, CastPlayer, state flow
2. `MainActivity` does not extend `AppCompatActivity` (extends `ComponentActivity`)
3. No `appcompat` dependency in `androidApp/build.gradle.kts`
4. No cast-related events in `PlayerMvi.kt` (only `isCasting` field)
5. No cast logic in `PlayerScreen.kt` or `PlayerViewModel.kt`
6. No "Casting to..." overlay UI
7. CastButton is commented out in `PlayerControls.kt`

## Changes Required

### New Files

#### 1. `shared/src/androidMain/kotlin/com/simplevideo/whiteiptv/platform/cast/CastSessionManager.kt`

- **Purpose**: Manages Cast session lifecycle, wraps CastPlayer, exposes cast state as StateFlow.
- **Key contents**:
  ```kotlin
  class CastSessionManager(private val context: Context) {

      data class CastState(
          val isCasting: Boolean = false,
          val deviceName: String? = null,
      )

      private val _castState = MutableStateFlow(CastState())
      val castState: StateFlow<CastState> = _castState.asStateFlow()

      private var castContext: CastContext? = null
      private var castPlayer: CastPlayer? = null
      private var sessionManagerListener: SessionManagerListener<CastSession>? = null

      /** Lazily initializes CastContext. Call from player screen. */
      fun initialize()

      /** Load a media URL on the Cast device. */
      fun loadMedia(url: String, title: String, userAgent: String?, referer: String?)

      /** Stop casting and release CastPlayer. */
      fun stopCasting()

      /** Clean up listeners. */
      fun release()
  }
  ```

#### 2. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/platform/CastManager.kt`

- **Purpose**: Common interface for cast state observation. Android provides real implementation, iOS provides no-op.
- **Key contents**:
  ```kotlin
  /** Platform-agnostic cast state. */
  data class CastConnectionState(
      val isCasting: Boolean = false,
      val deviceName: String? = null,
  )

  /** expect declaration for cast session management. */
  expect class CastManager {
      val castState: StateFlow<CastConnectionState>
      fun initialize()
      fun loadMedia(url: String, title: String, userAgent: String?, referer: String?)
      fun stopCasting()
      fun release()
  }
  ```

#### 3. `shared/src/iosMain/kotlin/com/simplevideo/whiteiptv/platform/CastManager.kt`

- **Purpose**: No-op implementation for iOS (AirPlay is handled at the AVPlayer level automatically).
- **Key contents**:
  ```kotlin
  actual class CastManager {
      actual val castState: StateFlow<CastConnectionState> = MutableStateFlow(CastConnectionState()).asStateFlow()
      actual fun initialize() {}
      actual fun loadMedia(url: String, title: String, userAgent: String?, referer: String?) {}
      actual fun stopCasting() {}
      actual fun release() {}
  }
  ```

#### 4. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/player/components/CastOverlay.kt`

- **Purpose**: Shows "Casting to [device name]" overlay when casting is active, replacing the video surface.
- **Key contents**:
  ```kotlin
  @Composable
  fun CastOverlay(
      deviceName: String?,
      channelName: String,
      modifier: Modifier = Modifier,
  )
  ```
  Displays a centered Cast icon, "Casting to [device]" text, and the channel name on a dark background.

### Modified Files

#### 1. `androidApp/src/main/kotlin/com/simplevideo/whiteiptv/MainActivity.kt`

- **What changes**: Change `ComponentActivity` to `AppCompatActivity`.
- **Why**: `MediaRouteButton.showDialog()` requires `FragmentActivity`. `AppCompatActivity` extends `FragmentActivity`.
- **Specific change**:
  ```kotlin
  // Before:
  import androidx.activity.ComponentActivity
  class MainActivity : ComponentActivity() {

  // After:
  import androidx.appcompat.app.AppCompatActivity
  class MainActivity : AppCompatActivity() {
  ```
  All other code (`enableEdgeToEdge()`, `setContent {}`, window insets) remains unchanged — `AppCompatActivity` supports all of these.

#### 2. `androidApp/build.gradle.kts`

- **What changes**: Add `androidx-appcompat` dependency.
- **Why**: `AppCompatActivity` requires appcompat library.
- **Specific change**: Add to dependencies block:
  ```kotlin
  implementation(libs.androidx.appcompat)
  ```

#### 3. `shared/src/androidMain/kotlin/com/simplevideo/whiteiptv/platform/cast/CastSessionManager.kt` (new, see above)

#### 4. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/player/mvi/PlayerMvi.kt`

- **What changes**: Add cast-related events and an action for cast disconnection.
- **Specific additions**:
  ```kotlin
  // In PlayerEvent:
  data class OnCastStateChanged(val isCasting: Boolean, val deviceName: String?) : PlayerEvent

  // In PlayerAction:
  data class ShowCastDisconnected(val deviceName: String?) : PlayerAction
  ```

#### 5. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/player/mvi/PlayerMvi.kt` — PlayerState

- **What changes**: Add `castDeviceName: String?` field to hold the device name for the overlay.
- **Specific change**:
  ```kotlin
  data class PlayerState(
      // ... existing fields ...
      val isCasting: Boolean = false,
      val castDeviceName: String? = null,  // NEW
  )
  ```

#### 6. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/player/PlayerViewModel.kt`

- **What changes**: Handle `OnCastStateChanged` event. When casting starts, update state. When casting stops (and was previously casting), emit `ShowCastDisconnected` action.
- **Specific addition in `obtainEvent()`**:
  ```kotlin
  is PlayerEvent.OnCastStateChanged -> {
      val wasCasting = viewState.isCasting
      viewState = viewState.copy(
          isCasting = viewEvent.isCasting,
          castDeviceName = viewEvent.deviceName,
      )
      if (wasCasting && !viewEvent.isCasting) {
          viewAction = PlayerAction.ShowCastDisconnected(viewEvent.deviceName)
      }
  }
  ```

#### 7. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/player/PlayerScreen.kt`

- **What changes**:
  1. Inject `CastManager` via Koin.
  2. Collect `castState` flow and forward changes as `PlayerEvent.OnCastStateChanged`.
  3. When `state.isCasting` is true, call `castManager.loadMedia()` instead of local player `setMediaSource()`.
  4. When cast state transitions to casting, pause local player. When it transitions back, resume.
  5. Show `CastOverlay` when `state.isCasting` is true (instead of the video surface).
  6. Handle `PlayerAction.ShowCastDisconnected` — currently just log, optionally show snackbar later.
- **Key changes in `PlayerScreenContent`**:
  ```kotlin
  val castManager = koinInject<CastManager>()
  val castState by castManager.castState.collectAsState()

  // Initialize cast manager
  LaunchedEffect(Unit) {
      castManager.initialize()
  }

  // Observe cast state changes
  LaunchedEffect(castState) {
      onEvent(PlayerEvent.OnCastStateChanged(castState.isCasting, castState.deviceName))
  }

  // When channel changes while casting, load on cast device
  LaunchedEffect(state.channel, state.isCasting) {
      if (state.isCasting && state.channel != null) {
          castManager.loadMedia(
              url = state.channel.url,
              title = state.channel.name,
              userAgent = state.channel.userAgent,
              referer = state.channel.referer,
          )
      }
  }

  // Pause/resume local player based on cast state
  LaunchedEffect(state.isCasting) {
      if (state.isCasting) {
          player.pause()
      } else if (state.channel != null) {
          player.play()
      }
  }

  // In the Box layout, conditionally show CastOverlay or PlayerView:
  if (state.isCasting) {
      CastOverlay(
          deviceName = state.castDeviceName,
          channelName = state.channel?.name ?: "",
      )
  } else if (state.channel != null) {
      player.PlayerView(modifier = Modifier.fillMaxSize())
  }
  ```

#### 8. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/player/components/PlayerControls.kt`

- **What changes**: Uncomment the `CastButton()` call at line 247.
- **Specific change**:
  ```kotlin
  // Before:
  // TODO: Enable when Cast SDK is fully configured (CastOptionsProvider + FragmentActivity)
  // CastButton()
  AirPlayButton()

  // After:
  CastButton()
  AirPlayButton()
  ```

#### 9. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/di/KoinModule.kt`

- **What changes**: No changes needed in commonMain KoinModule. CastManager registration goes in platformModule.

#### 10. `shared/src/androidMain/kotlin/com/simplevideo/whiteiptv/di/PlatformModule.kt`

- **What changes**: Register `CastManager` (wrapping `CastSessionManager`) as a singleton.
- **Specific addition**:
  ```kotlin
  single<CastManager> { CastManager(get()) }
  ```

#### 11. `shared/src/iosMain/kotlin/com/simplevideo/whiteiptv/di/PlatformModule.kt`

- **What changes**: Register the no-op `CastManager`.
- **Specific addition**:
  ```kotlin
  single<CastManager> { CastManager() }
  ```

### Database Changes

None.

### DI Changes

- `CastManager` registered as `single` in both Android and iOS `platformModule()`.

## Implementation Order

1. **Create CastConnectionState + expect CastManager** in `commonMain/platform/CastManager.kt` — defines the common contract.

2. **Create iOS actual CastManager** in `iosMain/platform/CastManager.kt` — no-op implementation so the project compiles cross-platform.

3. **Create CastSessionManager** in `androidMain/platform/cast/CastSessionManager.kt` — the real Cast session logic using CastContext, SessionManagerListener, CastPlayer.

4. **Create Android actual CastManager** in `androidMain/platform/CastManager.kt` — wraps CastSessionManager, delegates all calls.

5. **Change MainActivity to AppCompatActivity** in `androidApp/src/main/kotlin/.../MainActivity.kt` and add `appcompat` dependency to `androidApp/build.gradle.kts`.

6. **Update PlayerMvi.kt** — add `OnCastStateChanged` event, `ShowCastDisconnected` action, `castDeviceName` field to state.

7. **Update PlayerViewModel.kt** — handle `OnCastStateChanged` event.

8. **Create CastOverlay composable** in `commonMain/feature/player/components/CastOverlay.kt`.

9. **Update PlayerScreen.kt** — inject CastManager, collect cast state, handle local/remote player switching, show CastOverlay.

10. **Uncomment CastButton** in `PlayerControls.kt` (line 247).

11. **Register CastManager in DI** — both Android and iOS platformModule.

12. **Build and test** — verify on Android with a Chromecast device on the network.

## Testing Strategy

### Manual Testing (Android Chromecast)

1. **Cast device discovery**: Open player, verify MediaRouteButton appears when a Chromecast is on the same network.
2. **Start casting**: Tap Cast button, select device, verify stream plays on TV and local player shows "Casting to [device]" overlay.
3. **Channel switching while casting**: Swipe up/down to switch channels, verify the Cast device loads the new stream.
4. **Stop casting**: Tap Cast button, select "Stop casting", verify local playback resumes.
5. **Cast disconnect (device powered off)**: Verify local playback resumes automatically.
6. **App backgrounding while casting**: Verify cast session survives.
7. **Player exit while casting**: Navigate back, verify cast session stops.

### Manual Testing (iOS AirPlay)

1. **AirPlay button visible**: Open player, verify AVRoutePickerView button appears.
2. **AirPlay to Apple TV**: Select AirPlay device, verify video plays on TV.
3. **Channel switching**: Switch channels, verify new stream plays on AirPlay device.
4. **Disconnect**: Stop AirPlay, verify local playback resumes.

### Unit Tests

- `CastSessionManagerTest`: Verify state flow emissions for connect/disconnect scenarios (requires mocking CastContext — may need to be integration-tested instead).
- Test `PlayerViewModel` handling of `OnCastStateChanged`: verify state updates and `ShowCastDisconnected` action emission.

### Edge Cases

- No Google Play Services on device (Cast SDK will fail gracefully — MediaRouteButton stays hidden).
- Stream URL that Cast default receiver cannot play (e.g., some MPEG-TS streams) — Cast device will show an error; local playback should be unaffected.
- Rapid connect/disconnect — verify no race conditions in state flow.
- Multiple Cast devices on network — MediaRouteButton handles the chooser dialog.

### Coroutine Test Patterns

- `PlayerViewModel` tests: Use `runTest` with `StandardTestDispatcher`. Mock CastManager with a `MutableStateFlow` to simulate cast state changes.

## Doc Updates Required

**Update AFTER implementation:**

- `docs/features/player.md` — change "Chromecast / AirPlay" from "Planned Features" to a documented feature section.
- `docs/constraints/current-limitations.md` — no entry needed (cast was never listed as a limitation).

## Build & Test Commands

```bash
# Build Android app (verifies AppCompatActivity change, Cast SDK integration)
./gradlew :androidApp:assembleDebug

# Run shared unit tests
./gradlew :shared:testAndroidHostTest

# Build iOS framework (verifies no-op CastManager compiles)
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# Full check
./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug :shared:linkDebugFrameworkIosSimulatorArm64
```
