# Recomposition Tracking -- Implementation Plan

## Summary

Add a debug-only recomposition tracking utility to the project that visually highlights recomposing composables and logs recomposition counts. This uses the **JetpackComposeTracker** library (`io.github.qamarelsafadi:compose-tracker:3.0.0`) which provides a `Modifier.trackRecompositions()` extension and a conditional `Modifier.trackRecompositionsIf(enabled)` variant. The tracking will be wired into high-risk composables (list items, player controls, state-heavy screens) and gated behind a runtime debug flag so it never ships to production builds.

## Decisions Made

### Decision 1: Library Choice -- JetpackComposeTracker

- **Decision**: Use `io.github.qamarelsafadi:compose-tracker:3.0.0` over compose-stability-analyzer or a custom solution.
- **Rationale**:
  - It is a **runtime library** (not a Gradle plugin or compiler plugin), so integration is minimal -- just add the dependency and apply a modifier.
  - It is **KMP compatible** and works with Compose Multiplatform, matching this project's setup.
  - It provides **visual feedback** (red border + count) which is more useful for a developer on-device than log-only solutions.
  - `trackRecompositionsIf(enabled)` allows leaving tracking code in-place permanently, toggled by a single flag.
  - compose-stability-analyzer (0.7.0) is a compiler plugin / IDE plugin that adds build complexity and is primarily useful for static analysis rather than runtime observation. It can complement this tool later but is heavier to integrate.
  - A custom `SideEffect`-based logger is too primitive -- no visual overlay, no recomposition counts, requires manual code in every composable.
- **Alternatives considered**:
  - **compose-stability-analyzer**: Better for compiler-level stability analysis, but overkill for runtime tracking. Could be added later as a complementary tool.
  - **Custom SideEffect logging**: Zero dependencies but no visual feedback, no recomposition counts, tedious to maintain.
  - **Rebugger**: Shows parameter diffs, but is Android-only (not KMP compatible).

### Decision 2: Debug-Only Gating Strategy

- **Decision**: Use a `RecompositionConfig` object in `commonMain` with a `val isEnabled: Boolean` property defaulting to `false`. Developers set it to `true` locally to enable tracking. The library dependency is added to `commonMain.dependencies` (not a debug-only source set) because KMP does not support `debugImplementation` in multiplatform source sets.
- **Rationale**: KMP `commonMain` has no concept of build-type-specific dependencies. The library is tiny and the `trackRecompositionsIf(false)` call is a no-op at runtime when disabled, adding zero overhead. Using a compile-time constant ensures the compiler can inline the check. The flag defaults to `false` so production builds are safe without any CI/CD changes.
- **Alternatives considered**:
  - **Android-only `debugImplementation`**: Would only work on Android, not iOS, defeating the KMP purpose.
  - **expect/actual for isDebug**: Over-engineered for a developer tool. A simple constant is sufficient.

### Decision 3: Which Composables to Track

- **Decision**: Track the following high-risk composables by wrapping their root modifier with `trackRecompositionsIf(RecompositionConfig.isEnabled)`:
  1. **ChannelCardSquare** -- rendered many times in LazyRow (HomeScreen categories, favorites) and LazyVerticalGrid (ChannelsScreen grid).
  2. **ChannelCardList** -- rendered many times in LazyColumn (ChannelsScreen list, FavoritesScreen).
  3. **ContinueWatchingCard** -- rendered in HomeScreen LazyRow.
  4. **SearchResultItem** -- rendered in HomeScreen search LazyColumn.
  5. **HomeContent** -- large composable that re-reads `HomeState` (17 fields), likely to recompose on unrelated state changes.
  6. **ChannelsContent** -- complex composable with paging, dropdowns, and conditional rendering.
  7. **PlayerControlsOverlay** -- re-renders on every state change (buffering, visibility, tracks, EPG).
  8. **GestureOverlay** -- has multiple mutable states (volume, brightness, drag) that update during gestures.
  9. **PlayerScreenContent** -- heavy composable with DisposableEffect, multiple LaunchedEffects, and local state.
  10. **BottomNavigationBar** -- re-renders on every navigation change.
- **Rationale**: These composables either (a) appear many times in lists, (b) depend on frequently-changing state, or (c) are complex enough that unnecessary recompositions would cause visible jank. Simpler composables like `Section`, `EmptyState`, or `ConfirmationDialog` are not worth tracking.

## Current State

### Existing files relevant to this feature

- **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/AppLogger.kt`** (lines 1-22): Defines `AppLogger.Tags` for logcat filtering. A new `RECOMPOSITION` tag will be added here.
- **`gradle/libs.versions.toml`** (lines 1-97): Version catalog. No recomposition tracking library exists yet.
- **`shared/build.gradle.kts`** (lines 80-110): `commonMain.dependencies` block where the new dependency will be added.
- **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/ChannelCard.kt`** (lines 41-126, 133-204): `ChannelCardSquare` and `ChannelCardList` -- most recomposition-sensitive components (used in lists of 10k+ channels).
- **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/ContinueWatchingCard.kt`**: Card used in HomeScreen LazyRow.
- **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeScreen.kt`** (lines 366-446): `HomeContent` composable with multiple LazyRows.
- **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/ChannelsScreen.kt`** (lines 130-258): `ChannelsContent` with paging.
- **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/player/PlayerScreen.kt`** (lines 81-296): `PlayerScreenContent` with gesture and control overlays.
- **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/player/components/PlayerControls.kt`** (lines 52-103): `PlayerControlsOverlay`.
- **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/player/components/GestureOverlay.kt`** (lines 54-181): `GestureOverlay`.
- **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/main/MainScreen.kt`** (lines 102-125): `BottomNavigationBar`.

### Recomposition Risk Analysis

| Composable | Risk Level | Reason |
|---|---|---|
| `ChannelCardSquare` | **High** | Rendered 100s of times in lazy lists. Receives lambda callbacks (`onClick`, `onToggleFavorite`) which, if not stable, force recomposition of every item when parent state changes. |
| `ChannelCardList` | **High** | Same as above, used in ChannelsScreen list mode and FavoritesScreen. |
| `HomeContent` | **High** | Reads entire `HomeState` (17 fields). Any field change triggers full recomposition of the scrollable column including all LazyRows. |
| `ChannelsContent` | **High** | Combines `ChannelsState` + `LazyPagingItems`. Both change frequently during scrolling and filtering. |
| `PlayerControlsOverlay` | **Medium** | Re-renders on `isVisible`, `isBuffering`, `tracksInfo`, `currentProgram`, `sleepTimerRemainingMs` changes. AnimatedVisibility helps but the content still recomposes. |
| `GestureOverlay` | **Medium** | Multiple `mutableStateOf` values change during drag gestures (every frame). |
| `PlayerScreenContent` | **Medium** | Heavy setup with `remember`, `DisposableEffect`, `LaunchedEffect`. State reads (`state.channel`, `state.controlsVisible`, `state.isCasting`) trigger recomposition. |
| `BottomNavigationBar` | **Low** | Recomposes on navigation, but infrequent and lightweight. |

## Changes Required

### New Files

#### 1. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/RecompositionConfig.kt`

- **Purpose**: Central configuration object for recomposition tracking.
- **Key contents**:

```kotlin
package com.simplevideo.whiteiptv.common

/**
 * Configuration for recomposition tracking.
 *
 * Set [isEnabled] to true during local development to see recomposition
 * counts and visual borders on tracked composables. This must remain
 * false in committed code and production builds.
 */
object RecompositionConfig {
    /**
     * Enable recomposition tracking. When false (default), all
     * trackRecompositionsIf() calls are no-ops with zero overhead.
     *
     * Toggle locally for debugging. Do not commit with value = true.
     */
    const val isEnabled: Boolean = false
}
```

### Modified Files

#### 2. `gradle/libs.versions.toml`

- **What changes**: Add compose-tracker version and library reference.
- **Why**: Register the dependency in the version catalog per project convention.
- Add in `[versions]` section:
  ```toml
  compose-tracker = "3.0.0"
  ```
- Add in `[libraries]` section:
  ```toml
  compose-tracker = { module = "io.github.qamarelsafadi:compose-tracker", version.ref = "compose-tracker" }
  ```

#### 3. `shared/build.gradle.kts`

- **What changes**: Add `compose-tracker` to `commonMain.dependencies`.
- **Why**: Make the library available across all platforms (Android and iOS).
- Add after line 109 (after `libs.androidx.datastore.preferences`):
  ```kotlin
  implementation(libs.compose.tracker)
  ```

#### 4. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/AppLogger.kt`

- **What changes**: Add `RECOMPOSITION` tag to `Tags` object.
- **Why**: Enable filtering recomposition logs in logcat with `WhiteIPTV:Recomposition`.
- Add inside `Tags`:
  ```kotlin
  const val RECOMPOSITION = "$BASE_TAG:Recomposition"
  ```

#### 5. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/ChannelCard.kt`

- **What changes**: Add `trackRecompositionsIf(RecompositionConfig.isEnabled)` to the root modifier of `ChannelCardSquare` and `ChannelCardList`.
- **Why**: These are the most frequently instantiated composables (hundreds of instances in lazy lists).
- For `ChannelCardSquare` (line 52), change:
  ```kotlin
  // Before
  modifier = modifier.fillMaxWidth(),
  // After
  modifier = modifier.fillMaxWidth().trackRecompositionsIf(RecompositionConfig.isEnabled),
  ```
- For `ChannelCardList` (line 143), change the `Surface` modifier:
  ```kotlin
  // Before
  modifier = modifier.fillMaxWidth(),
  // After
  modifier = modifier.fillMaxWidth().trackRecompositionsIf(RecompositionConfig.isEnabled),
  ```

#### 6. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/ContinueWatchingCard.kt`

- **What changes**: Add `trackRecompositionsIf(RecompositionConfig.isEnabled)` to root modifier.
- **Why**: Rendered in HomeScreen LazyRow; may recompose unnecessarily when HomeState changes.

#### 7. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeScreen.kt`

- **What changes**: Add tracking to `HomeContent` root Column modifier and `SearchResultItem` Card modifier.
- **Why**: `HomeContent` reads the full `HomeState` and `SearchResultItem` is rendered in a LazyColumn.
- For `HomeContent` (line 373):
  ```kotlin
  modifier = modifier.fillMaxSize().trackRecompositionsIf(RecompositionConfig.isEnabled).verticalScroll(...)
  ```
- For `SearchResultItem` (line 508):
  ```kotlin
  modifier = Modifier.fillMaxWidth().trackRecompositionsIf(RecompositionConfig.isEnabled),
  ```

#### 8. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/ChannelsScreen.kt`

- **What changes**: Add tracking to `ChannelsContent` root Column modifier.
- **Why**: Complex composable combining state, paging, and conditional rendering.
- For `ChannelsContent` (line 143):
  ```kotlin
  Column(modifier = modifier.fillMaxSize().trackRecompositionsIf(RecompositionConfig.isEnabled))
  ```

#### 9. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/player/components/PlayerControls.kt`

- **What changes**: Add tracking to `PlayerControlsOverlay` root Box modifier.
- **Why**: Re-renders on multiple rapidly changing state fields.
- For `PlayerControlsOverlay` (line 69):
  ```kotlin
  Box(modifier = modifier.fillMaxSize().trackRecompositionsIf(RecompositionConfig.isEnabled))
  ```

#### 10. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/player/components/GestureOverlay.kt`

- **What changes**: Add tracking to `GestureOverlay` root Box modifier.
- **Why**: Multiple mutable state values update during gesture interactions.
- For `GestureOverlay` (line 73):
  ```kotlin
  Box(
      modifier = modifier.fillMaxSize().trackRecompositionsIf(RecompositionConfig.isEnabled)
          .pointerInput(Unit) { ... }
  ```

#### 11. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/main/MainScreen.kt`

- **What changes**: Add tracking to `BottomNavigationBar` NavigationBar.
- **Why**: Helps verify navigation bar does not recompose excessively on tab switches.
- For `BottomNavigationBar` inside `NavigationBar` (line 108):
  ```kotlin
  NavigationBar(modifier = Modifier.trackRecompositionsIf(RecompositionConfig.isEnabled))
  ```

### Database Changes

None.

### DI Changes

None. `RecompositionConfig` is a simple object with a constant, no DI registration needed.

## Implementation Order

1. **Add dependency** -- Update `gradle/libs.versions.toml` and `shared/build.gradle.kts` to add `compose-tracker:3.0.0`.
2. **Create RecompositionConfig** -- Create `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/RecompositionConfig.kt`.
3. **Update AppLogger** -- Add `RECOMPOSITION` tag to `AppLogger.Tags`.
4. **Add tracking to list item components** -- Modify `ChannelCard.kt` (both `ChannelCardSquare` and `ChannelCardList`) and `ContinueWatchingCard.kt`.
5. **Add tracking to screen composables** -- Modify `HomeScreen.kt` (`HomeContent`, `SearchResultItem`), `ChannelsScreen.kt` (`ChannelsContent`).
6. **Add tracking to player components** -- Modify `PlayerControls.kt` (`PlayerControlsOverlay`), `GestureOverlay.kt`.
7. **Add tracking to navigation** -- Modify `MainScreen.kt` (`BottomNavigationBar`).
8. **Verify build** -- Run `./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug` to confirm everything compiles.

## Testing Strategy

### Manual verification (with `RecompositionConfig.isEnabled = true`)

1. **HomeScreen**: Open app, verify red borders appear on channel cards in LazyRows (Favorites, categories). Scroll through categories and observe recomposition counts. Cards should ideally recompose 0-1 times when scrolling into view.
2. **ChannelsScreen**: Navigate to Channels tab. Switch between Grid and List view. Verify channel cards show tracking borders. Scroll through the paged list and confirm items are not recomposing unnecessarily.
3. **FavoritesScreen**: Navigate to Favorites. Toggle a favorite. Only the toggled card should recompose, not the entire list.
4. **PlayerScreen**: Play a channel. Observe `PlayerControlsOverlay` and `GestureOverlay` tracking. Perform volume/brightness gestures. Check that gesture overlay recomposition is reasonable.
5. **Navigation**: Switch between tabs. `BottomNavigationBar` should show minimal recompositions (1 per navigation event).

### Automated (with `RecompositionConfig.isEnabled = false`)

- Existing unit tests must continue to pass. The `trackRecompositionsIf(false)` modifier is a no-op and should not affect behavior.
- Run `./gradlew :shared:testAndroidHostTest` to verify no regressions.

### Edge Cases

- **Config left enabled**: If `RecompositionConfig.isEnabled` is accidentally committed as `true`, the app still functions correctly -- it just shows red borders. No crash risk.
- **iOS compatibility**: Verify the library works on iOS simulator. If it does not render the border overlay on iOS (some Compose Multiplatform rendering differences), that is acceptable -- the primary debugging target is Android.

## Doc Updates Required

- **Update AFTER implementation**: Add `RECOMPOSITION` tag to any developer documentation that lists logcat filter tags.
- No product doc updates needed -- this is a developer-only debugging tool, not a user-facing feature.

## Build & Test Commands

```bash
# Verify the project compiles with the new dependency
./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug

# Format code after changes
./gradlew formatAll

# Build iOS framework to verify KMP compatibility
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```
