# Add "Add New Playlist" to Playlist Dropdown -- Implementation Plan

## Summary

Add a "+ Add new playlist" action item at the bottom of the playlist dropdown on the Home screen. When tapped, it navigates the user to the Onboarding screen to import a new playlist. This aligns the implementation with the existing product spec in `docs/features/home.md`, which already describes this behavior. The navigation plumbing (`HomeAction.NavigateToOnboarding` and `onNavigateToOnboarding` callback) already exists -- only the dropdown UI needs the extra item.

## Decisions Made

- **Decision**: Add the "Add new playlist" item directly inside `PlaylistDropdown.kt` rather than modifying the generic `DropdownSelector` component.
  - **Rationale**: `DropdownSelector` is a reusable generic component also used by `GroupDropdown`. Adding a special action item to the generic component would pollute its API. The playlist-specific behavior belongs in `PlaylistDropdown`.
  - **Alternatives considered**: (1) Adding an `onAction` callback to `DropdownSelector` -- rejected because it couples a generic component to a specific use case. (2) Adding it in `HomeScreen.kt` -- rejected because the dropdown logic is encapsulated in `PlaylistDropdown`.

- **Decision**: Use a `HorizontalDivider` above the "+ Add new playlist" item to visually separate it from playlist items, matching the spec in `docs/features/home.md`.
  - **Rationale**: The spec says "separated by a divider." This is the simplest way to achieve it.

- **Decision**: Wire the action through the existing `HomeEvent`/`HomeAction` pattern by adding a new `OnAddPlaylistClick` event, which triggers `HomeAction.NavigateToOnboarding`.
  - **Rationale**: Keeps the MVI pattern consistent. The `NavigateToOnboarding` action already exists and is handled in both the ViewModel and the screen.

## Current State

### Files involved

1. **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/PlaylistDropdown.kt`** (lines 1-35)
   - Wraps `DropdownSelector` passing playlists, selection, and callbacks.
   - No "add playlist" item exists.

2. **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/DropdownSelector.kt`** (lines 1-85)
   - Generic dropdown: shows "All" option + list of items. No extension point for extra actions.

3. **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeScreen.kt`** (lines 350-360)
   - `HomeTopAppBarTitle` calls `PlaylistDropdown` with `playlists`, `selection`, `onPlaylistSelect`. No `onAddPlaylist` callback.

4. **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/mvi/HomeMvi.kt`** (lines 27-54)
   - `HomeEvent` has no `OnAddPlaylistClick` event.
   - `HomeAction.NavigateToOnboarding` already exists (line 53).

5. **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeViewModel.kt`** (lines 93-172)
   - `obtainEvent` does not handle an add-playlist event.

## Changes Required

### Modified Files

#### 1. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/PlaylistDropdown.kt`

**What changes**: Replace the delegation to `DropdownSelector` with an inline dropdown implementation that includes the "Add new playlist" item at the bottom, separated by a `HorizontalDivider`.

**Why**: `DropdownSelector` has no extension point for custom trailing items. Rather than complicating the generic API, `PlaylistDropdown` will manage its own `DropdownMenu` directly. This keeps the change contained.

**New signature**:
```kotlin
@Composable
fun PlaylistDropdown(
    playlists: List<PlaylistEntity>,
    selection: PlaylistSelection,
    onPlaylistSelect: (PlaylistSelection) -> Unit,
    onAddPlaylistClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

**Key implementation details**:
- Copy the visual structure from `DropdownSelector` (OutlinedCard trigger + DropdownMenu).
- After the playlist items loop, add `HorizontalDivider()`.
- Add a `DropdownMenuItem` with `leadingIcon` showing an `Icons.Default.Add` icon and text "+ Add new playlist".
- On click: call `onAddPlaylistClick()` and close the dropdown.

#### 2. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/mvi/HomeMvi.kt`

**What changes**: Add `OnAddPlaylistClick` to `HomeEvent` sealed interface.

**Specific addition** (after line 46, inside `HomeEvent`):
```kotlin
data object OnAddPlaylistClick : HomeEvent
```

#### 3. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeViewModel.kt`

**What changes**: Handle the new event in `obtainEvent`.

**Specific addition** (inside the `when` block in `obtainEvent`, e.g., after the `OnChannelClick` handler):
```kotlin
is HomeEvent.OnAddPlaylistClick -> {
    viewAction = HomeAction.NavigateToOnboarding
}
```

#### 4. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeScreen.kt`

**What changes**:
- Update `HomeTopAppBarTitle` to accept and forward an `onAddPlaylistClick` callback.
- Update `HomeTopAppBar` to accept and forward the same callback.
- In the `HomeScreen` composable, pass `{ viewModel.obtainEvent(HomeEvent.OnAddPlaylistClick) }` as the callback.
- Update the `PlaylistDropdown` call in `HomeTopAppBarTitle` to pass `onAddPlaylistClick`.

**Specific changes**:

(a) `HomeTopAppBar` signature (line 238) -- add parameter:
```kotlin
onAddPlaylistClick: () -> Unit,
```
Forward it to `HomeTopAppBarTitle`.

(b) `HomeTopAppBarTitle` signature (line 350) -- add parameter:
```kotlin
onAddPlaylistClick: () -> Unit,
```
Forward it to `PlaylistDropdown`.

(c) `PlaylistDropdown` call (line 355) -- add:
```kotlin
onAddPlaylistClick = onAddPlaylistClick,
```

(d) `HomeTopAppBar` call site in `Scaffold` (line 142) -- add:
```kotlin
onAddPlaylistClick = { viewModel.obtainEvent(HomeEvent.OnAddPlaylistClick) },
```

### No New Files

This feature requires only modifications to existing files.

### No Database Changes

### No DI Changes

## Implementation Order

1. **Add `OnAddPlaylistClick` event to `HomeMvi.kt`** -- one-line addition to the sealed interface.
2. **Handle the event in `HomeViewModel.kt`** -- two-line addition to the `when` block.
3. **Rewrite `PlaylistDropdown.kt`** -- replace `DropdownSelector` delegation with inline dropdown that includes the divider and "+ Add new playlist" item. Add `onAddPlaylistClick` parameter.
4. **Update `HomeScreen.kt`** -- thread the `onAddPlaylistClick` callback from `HomeScreen` through `HomeTopAppBar` and `HomeTopAppBarTitle` down to `PlaylistDropdown`.

## Testing Strategy

- **Manual test**: Open Home screen, tap playlist dropdown, verify "+ Add new playlist" appears below a divider after all playlist names. Tap it, verify navigation to Onboarding.
- **Unit test for HomeViewModel**: Send `HomeEvent.OnAddPlaylistClick`, assert `HomeAction.NavigateToOnboarding` is emitted. This follows the same pattern as existing delete-last-playlist test logic.
- **Edge case**: Only one playlist exists -- dropdown should show "All", the playlist name, divider, and "+ Add new playlist".
- **Edge case**: No playlists (should not happen in practice since Home is only reachable after onboarding, but if it does, the dropdown should still show the add option).

## Doc Updates Required

- `docs/features/home.md` -- No update needed. The spec already describes the "+ Add new playlist" behavior (line 11). Implementation will match the spec.
- `docs/constraints/current-limitations.md` -- No entry exists for this gap, so no removal needed.

## Build & Test Commands

```bash
./gradlew :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
```
