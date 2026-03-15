# Channel View Mode in Favorites Screen -- Implementation Plan

## Summary

The Settings "Channel View" toggle (Grid/List) already controls the Channels screen layout. This plan extends the same preference to the Favorites screen so that both screens consistently respect the user's chosen view mode. The Favorites screen currently always renders a list via `LazyColumn` with `ChannelCardList`. When Grid mode is selected, it should render a `LazyVerticalGrid` with `ChannelCardSquare` instead.

## Decisions Made

### 1. Reuse the same preference -- no separate Favorites view mode
- **Decision**: The existing `channelViewMode` preference from `SettingsPreferences` controls both Channels and Favorites screens.
- **Rationale**: The Settings UI label is "Channel View" which implies a global channel display preference. Having one toggle control both screens is simpler and more consistent. Users expect uniform behavior.
- **Alternatives considered**: Separate per-screen toggle -- rejected as over-engineered for this use case.

### 2. Reuse existing shared card composables
- **Decision**: Use `ChannelCardSquare` (from `common/components/ChannelCard.kt`) for grid mode and `ChannelCardList` for list mode, exactly as `ChannelsScreen` does.
- **Rationale**: These composables already exist and are used by `ChannelsScreen`. Reusing them ensures visual consistency between screens. `ChannelCardSquare` accepts `isFavorite`, `onClick`, and `onToggleFavorite` -- all needed by Favorites.
- **Alternatives considered**: Custom Favorites-specific cards -- rejected as unnecessary duplication.

### 3. No UseCase needed -- ViewModel reads preference directly
- **Decision**: `FavoritesViewModel` reads `SettingsPreferences.channelViewModeFlow` directly, matching the pattern already established in `ChannelsViewModel.observeViewMode()`.
- **Rationale**: This is a UI preference, not business logic. `ChannelsViewModel` already follows this exact pattern (line 76-82). Consistency with existing code is paramount.
- **Alternatives considered**: Wrapping in a UseCase -- rejected as unnecessary indirection.

### 4. Grid uses 2-column fixed layout
- **Decision**: Grid mode in Favorites uses `GridCells.Fixed(2)` matching the Channels screen.
- **Rationale**: Consistent grid density across screens. The `ChannelCardSquare` composable with its 1:1 aspect ratio is designed for a 2-column grid.

### 5. All favorites are marked isFavorite=true
- **Decision**: When rendering `ChannelCardSquare` in Favorites, always pass `isFavorite = true`.
- **Rationale**: Every channel on the Favorites screen is by definition a favorite. The existing list mode already does this (line 172 of `FavoritesScreen.kt`).

## Current State

### What already works

- **`SettingsPreferences.channelViewModeFlow`** (line 25-29 of `SettingsPreferences.kt`): Reactive `Flow<ChannelViewMode>` already exists and emits `List` or `Grid`.
- **`ChannelsViewModel.observeViewMode()`** (lines 76-82 of `ChannelsViewModel.kt`): Pattern for observing the view mode flow and copying into state. This is the exact pattern to replicate.
- **`ChannelsScreen.kt`** (lines 181-256): Full working implementation of Grid/List switching with `when (state.channelViewMode)`. Already uses `ChannelCardSquare` for Grid and `ChannelCardList` for List.
- **`ChannelCardSquare`** and **`ChannelCardList`** (in `common/components/ChannelCard.kt`): Shared card composables ready to use.
- **`FavoritesScreen.kt`** (lines 159-178): `ChannelsList` composable currently hardcodes `LazyColumn` + `ChannelCardList`.

### What needs to change

- **`FavoritesMvi.kt`**: `FavoritesState` has no `channelViewMode` field.
- **`FavoritesViewModel.kt`**: Does not inject `SettingsPreferences` or observe `channelViewModeFlow`.
- **`FavoritesScreen.kt`**: `ChannelsList` always renders as a list, ignoring view mode.
- **`KoinModule.kt`**: `FavoritesViewModel` constructor will gain a new parameter; Koin auto-resolves it since `SettingsPreferences` is already registered.

## Changes Required

### Modified Files

#### 1. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/favorites/mvi/FavoritesMvi.kt`

- **What changes**: Add `channelViewMode: ChannelViewMode = ChannelViewMode.List` field to `FavoritesState`.
- **Why**: The screen composable needs the current view mode from state to decide which layout to render.
- **Specific change**:
  - Add import: `import com.simplevideo.whiteiptv.domain.model.ChannelViewMode`
  - Add field after `isLoading` (before `error`):
    ```kotlin
    val channelViewMode: ChannelViewMode = ChannelViewMode.List,
    ```

#### 2. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/favorites/FavoritesViewModel.kt`

- **What changes**: Add `SettingsPreferences` constructor parameter and observe `channelViewModeFlow`.
- **Why**: The ViewModel must bridge the persisted preference to the UI state.
- **Specific changes**:
  - Add constructor parameter: `private val settingsPreferences: SettingsPreferences`
  - Add import: `import com.simplevideo.whiteiptv.data.local.SettingsPreferences`
  - Add `observeViewMode()` private method (identical pattern to `ChannelsViewModel` lines 76-82):
    ```kotlin
    private fun observeViewMode() {
        settingsPreferences.channelViewModeFlow
            .onEach { mode ->
                viewState = viewState.copy(channelViewMode = mode)
            }
            .launchIn(viewModelScope)
    }
    ```
  - Call `observeViewMode()` at the end of the `init` block (after the existing `combine...launchIn` chain).

#### 3. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/favorites/FavoritesScreen.kt`

- **What changes**: Replace the hardcoded `ChannelsList` composable with a view-mode-aware implementation that switches between `LazyVerticalGrid` + `ChannelCardSquare` (Grid) and `LazyColumn` + `ChannelCardList` (List).
- **Why**: This is the visual change -- the screen must respect the user's preference.
- **Specific changes**:
  - Add imports:
    ```kotlin
    import androidx.compose.foundation.lazy.grid.GridCells
    import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
    import androidx.compose.foundation.lazy.grid.items
    import com.simplevideo.whiteiptv.common.components.ChannelCardSquare
    import com.simplevideo.whiteiptv.domain.model.ChannelViewMode
    ```
  - Update the `ChannelsList` call site (line 120) to pass `channelViewMode`:
    ```kotlin
    ChannelsList(
        channels = state.channels,
        channelViewMode = state.channelViewMode,
        onEvent = viewModel::obtainEvent,
    )
    ```
  - Rewrite `ChannelsList` composable (lines 159-178) to branch on view mode:
    ```kotlin
    @Composable
    private fun ChannelsList(
        channels: List<ChannelEntity>,
        channelViewMode: ChannelViewMode,
        onEvent: (FavoritesEvent) -> Unit,
    ) {
        when (channelViewMode) {
            ChannelViewMode.Grid -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(channels, key = { it.id }) { channel ->
                        ChannelCardSquare(
                            name = channel.name,
                            logoUrl = channel.logoUrl,
                            isFavorite = true,
                            onClick = { onEvent(FavoritesEvent.OnChannelClick(channel.id)) },
                            onToggleFavorite = { onEvent(FavoritesEvent.OnToggleFavorite(channel.id)) },
                        )
                    }
                }
            }
            ChannelViewMode.List -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(channels, key = { it.id }) { channel ->
                        ChannelCardList(
                            name = channel.name,
                            logoUrl = channel.logoUrl,
                            isFavorite = true,
                            onClick = { onEvent(FavoritesEvent.OnChannelClick(channel.id)) },
                            onToggleFavorite = { onEvent(FavoritesEvent.OnToggleFavorite(channel.id)) },
                        )
                    }
                }
            }
        }
    }
    ```

### New Files

None.

### Database Changes

None.

### DI Changes

None. `SettingsPreferences` is already registered as `singleOf(::SettingsPreferences)` in `settingsModule` (line 116 of `KoinModule.kt`). Koin will auto-resolve the new `SettingsPreferences` constructor parameter added to `FavoritesViewModel` since `viewModelOf(::FavoritesViewModel)` uses constructor injection.

## Implementation Order

1. **Add `channelViewMode` to `FavoritesState`** in `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/favorites/mvi/FavoritesMvi.kt`. Add the field and import.

2. **Update `FavoritesViewModel` to observe view mode** in `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/favorites/FavoritesViewModel.kt`. Add `SettingsPreferences` constructor parameter, add `observeViewMode()` method, call it from `init`.

3. **Update `FavoritesScreen` to render both layouts** in `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/favorites/FavoritesScreen.kt`. Add grid imports, pass `channelViewMode` to `ChannelsList`, rewrite `ChannelsList` with `when` branch.

4. **Format and lint**: `./gradlew formatAll`

5. **Build and test**: `./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug`

## Testing Strategy

### Unit Tests

No existing `FavoritesViewModelTest` was found. The scope of this change is small enough that manual verification is sufficient, but if tests are desired:

- **Test**: `FavoritesState` defaults to `ChannelViewMode.List`
- **Test**: When `SettingsPreferences.channelViewModeFlow` emits `Grid`, `FavoritesViewModel.viewState.channelViewMode` becomes `Grid`
- **Coroutine test patterns**: Use `Dispatchers.Main` set to `StandardTestDispatcher` via `Dispatchers.setMain`. DataStore flows need `TestScope` for deterministic emission.

### Manual Testing

1. Open Settings, set Channel View to "List". Navigate to Favorites tab -- should show list layout with `ChannelCardList` (existing behavior, unchanged).
2. Open Settings, set Channel View to "Grid". Navigate to Favorites tab -- should show 2-column grid layout with `ChannelCardSquare` cards.
3. Toggle a favorite off in Grid mode -- channel should disappear from the grid.
4. Toggle a favorite off in List mode -- channel should disappear from the list.
5. Verify Channel View mode also still works on the Channels tab (no regression).
6. Switch between Favorites and Channels tabs -- both should use the same view mode.
7. Change view mode while Favorites is the active tab (if returning from Settings) -- layout should update reactively.

### Edge Cases

- Empty favorites list -- the empty state composable renders above the `ChannelsList` call, so it is unaffected by view mode.
- Search active with Grid mode -- search results should display in grid layout.
- Channel with no logo -- `ChannelCardSquare` handles this via `AsyncImage` fallback (shows empty card area).
- Single favorite channel in Grid mode -- should render as one card in a 2-column grid (one cell filled, one empty).

## Doc Updates Required

- **`docs/constraints/current-limitations.md`**: If there is a listed limitation about view mode only applying to Channels, remove it after implementation.
  > [!NOTE] Update AFTER implementation

## Build & Test Commands

```bash
./gradlew formatAll
./gradlew :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
```
