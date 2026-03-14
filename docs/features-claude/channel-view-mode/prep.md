# Channel View List/Grid Mode Toggle -- Implementation Plan

## Summary

The Settings screen already has a List/Grid toggle for channel view mode that persists the preference via DataStore, but the Channels screen always renders a 2-column grid regardless of the saved preference. This plan connects the persisted `ChannelViewMode` preference to the Channels screen so that selecting "List" shows a single-column list layout with larger items showing more detail, and "Grid" shows the existing 2-column card grid.

## Decisions Made

### 1. Reactive Flow for channel view mode
- **Decision**: Add a `channelViewModeFlow: Flow<ChannelViewMode>` to `SettingsPreferences` (mirroring the existing `autoUpdateEnabledFlow` pattern), rather than having `ChannelsViewModel` do a one-shot read.
- **Rationale**: If the user changes the view mode in Settings and navigates back to Channels, the change should apply immediately without requiring a screen recreation. A reactive Flow ensures this. The pattern already exists in `SettingsPreferences.autoUpdateEnabledFlow`.
- **Alternatives considered**: One-shot `suspend` read in `ChannelsViewModel.init` -- rejected because it would not react to changes made while the ViewModel is alive (e.g., split-screen, or navigating back from Settings).

### 2. List layout design
- **Decision**: Reuse the list item layout from `FavoritesScreen.ChannelListItem` as the reference design -- a horizontal `Row` with a 56dp logo, channel name, and a favorite star toggle. Show `tvgLanguage` or `tvgCountry` as optional secondary text when available.
- **Rationale**: FavoritesScreen already has a polished list item that users will recognize. Consistency across screens improves usability. Group title is not on `ChannelEntity` (it lives in the cross-ref/group tables) and adding a JOIN would complicate the paging query, so we use existing entity fields for secondary info.
- **Alternatives considered**: (a) A completely new list card design -- rejected for unnecessary complexity. (b) Adding group title via JOIN in paging query -- rejected as over-scoped; group info is already shown in the group dropdown filter.

### 3. Scope limited to Channels screen only
- **Decision**: Only the Channels tab respects the view mode toggle. Favorites stays as a list. Home category sections stay as horizontal rows.
- **Rationale**: The Settings toggle is labeled "Channel View" which implies it controls the Channels tab. FavoritesScreen already uses a list layout. Changing Home would require significant redesign.
- **Alternatives considered**: Applying view mode to all channel lists -- rejected as over-scoped for this fix.

### 4. No new UseCase needed
- **Decision**: `ChannelsViewModel` reads `SettingsPreferences.channelViewModeFlow` directly, without a UseCase wrapper.
- **Rationale**: This is a simple UI preference read, not business logic. Adding a UseCase for a single DataStore Flow read would be over-engineering. The pattern of ViewModels reading preferences directly is already established in `SettingsViewModel`.
- **Alternatives considered**: `GetChannelViewModeUseCase` -- rejected as unnecessary indirection.

### 5. Grid loading spinner span
- **Decision**: The "append loading" spinner at the bottom of the list in List mode uses a single full-width item. In Grid mode it continues to use `GridItemSpan(2)`.
- **Rationale**: `LazyColumn` does not have span concepts, so a full-width `Box` is natural. The grid already handles this correctly.

## Current State

### Files that exist and are relevant

- **`shared/src/commonMain/.../domain/model/ChannelViewMode.kt`** (line 3): `enum class ChannelViewMode { List, Grid }` -- already defined.
- **`shared/src/commonMain/.../data/local/SettingsPreferences.kt`** (lines 35-43): `getChannelViewMode()` and `setChannelViewMode()` suspend functions exist. No reactive Flow.
- **`shared/src/commonMain/.../feature/settings/SettingsViewModel.kt`** (lines 54-59): Handles `OnChannelViewModeChanged` event, persists to `SettingsPreferences`.
- **`shared/src/commonMain/.../feature/settings/mvi/SettingsMvi.kt`** (line 10): `channelViewMode` is in `SettingsState`.
- **`shared/src/commonMain/.../feature/channels/ChannelsScreen.kt`** (lines 187-219): Always uses `LazyVerticalGrid(columns = GridCells.Fixed(2))`. No awareness of view mode.
- **`shared/src/commonMain/.../feature/channels/ChannelsViewModel.kt`**: No reference to `ChannelViewMode` or `SettingsPreferences`.
- **`shared/src/commonMain/.../feature/channels/mvi/ChannelsMvi.kt`**: `ChannelsState` has no `channelViewMode` field.
- **`shared/src/commonMain/.../di/KoinModule.kt`** (line 61): `ChannelsViewModel` is registered with `viewModelOf(::ChannelsViewModel)`.
- **`shared/src/commonMain/.../feature/favorites/FavoritesScreen.kt`** (lines 189-246): `ChannelListItem` composable -- good reference for list layout.

## Changes Required

### Modified Files

#### 1. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/SettingsPreferences.kt`
- **What changes**: Add a reactive `channelViewModeFlow` property.
- **Why**: `ChannelsViewModel` needs to observe view mode changes reactively.
- **Details**: Add after line 23 (after `autoUpdateEnabledFlow`):
```kotlin
val channelViewModeFlow: Flow<ChannelViewMode> = dataStore.data
    .map { prefs ->
        val name = prefs[CHANNEL_VIEW_MODE_KEY] ?: ChannelViewMode.List.name
        runCatching { ChannelViewMode.valueOf(name) }.getOrDefault(ChannelViewMode.List)
    }
    .distinctUntilChanged()
```

#### 2. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/mvi/ChannelsMvi.kt`
- **What changes**: Add `channelViewMode: ChannelViewMode` field to `ChannelsState`.
- **Why**: The screen needs to know which layout to render.
- **Details**: Add field with default `ChannelViewMode.List`:
```kotlin
data class ChannelsState(
    val playlists: List<PlaylistEntity> = emptyList(),
    val selection: PlaylistSelection = PlaylistSelection.All,
    val groups: List<ChannelGroup> = emptyList(),
    val selectedGroup: ChannelGroup? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val channelViewMode: ChannelViewMode = ChannelViewMode.List,
)
```
- Add import: `import com.simplevideo.whiteiptv.domain.model.ChannelViewMode`

#### 3. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/ChannelsViewModel.kt`
- **What changes**: Inject `SettingsPreferences`, observe `channelViewModeFlow`, propagate to state.
- **Why**: ViewModel must bridge the preference to the UI state.
- **Details**:
  - Add `settingsPreferences: SettingsPreferences` constructor parameter.
  - In `loadData()`, add `settingsPreferences.channelViewModeFlow` to the `combine` call. Include `channelViewMode` in the emitted state.
  - Update the `DataState` and `LoadParams` inner classes to carry `channelViewMode`.
  - The `combine` currently combines 4 flows. Adding a 5th requires switching from `combine(f1, f2, f3, f4) { ... }` to `combine(listOf(f1, f2, f3, f4, f5)) { ... }` or nesting. Simplest: collect `channelViewModeFlow` in a separate `launchIn` that just updates state, since view mode does not affect data loading.

  Preferred approach -- separate collection (simpler, avoids changing the existing combine):
```kotlin
init {
    loadData()
    observeViewMode()
}

private fun observeViewMode() {
    settingsPreferences.channelViewModeFlow
        .onEach { mode ->
            viewState = viewState.copy(channelViewMode = mode)
        }
        .launchIn(viewModelScope)
}
```

#### 4. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/ChannelsScreen.kt`
- **What changes**: Conditionally render either `LazyVerticalGrid` (Grid mode) or `LazyColumn` (List mode) based on `state.channelViewMode`. Add a `ChannelListItem` composable for List mode.
- **Why**: This is the core visual change -- the screen must respect the user's preference.
- **Details**:
  - In `ChannelsContent`, replace the unconditional `LazyVerticalGrid` block (lines 187-219) with a `when (state.channelViewMode)` branch.
  - For `ChannelViewMode.Grid`: keep the existing `LazyVerticalGrid` code as-is.
  - For `ChannelViewMode.List`: render a `LazyColumn` with `ChannelListItem` composable items. Use `pagedItems` with the same paging pattern.
  - Add `channelViewMode` parameter to `ChannelsContent` or read from `state` (already in state).
  - New `ChannelListItem` composable: horizontal Row layout similar to `FavoritesScreen.ChannelListItem` but adapted for paging items.

  New composable (add at end of file):
```kotlin
@Composable
private fun ChannelListItem(
    channel: ChannelEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = channel.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitle = channel.tvgLanguage ?: channel.tvgCountry
                if (!subtitle.isNullOrEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (channel.isFavorite) {
                        Icons.Filled.Star
                    } else {
                        Icons.Outlined.StarOutline
                    },
                    contentDescription = if (channel.isFavorite) {
                        "Remove from favorites"
                    } else {
                        "Add to favorites"
                    },
                    tint = if (channel.isFavorite) {
                        Color(0xFFFFD700)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}
```

  Updated `ChannelsContent` body (the `else` branch after empty/loading checks):
```kotlin
when (state.channelViewMode) {
    ChannelViewMode.Grid -> {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                count = pagedItems.itemCount,
                key = pagedItems.itemKey { it.id },
            ) { index ->
                val channel = pagedItems[index]
                if (channel != null) {
                    ChannelGridItem(
                        channel = channel,
                        onClick = { onChannelClick(channel.id) },
                        onToggleFavorite = { onToggleFavorite(channel.id) },
                    )
                }
            }
            if (pagedItems.loadState.append is LoadState.Loading) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
    ChannelViewMode.List -> {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                count = pagedItems.itemCount,
                key = pagedItems.itemKey { it.id },
            ) { index ->
                val channel = pagedItems[index]
                if (channel != null) {
                    ChannelListItem(
                        channel = channel,
                        onClick = { onChannelClick(channel.id) },
                        onToggleFavorite = { onToggleFavorite(channel.id) },
                    )
                }
            }
            if (pagedItems.loadState.append is LoadState.Loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
```

  New imports needed in `ChannelsScreen.kt`:
  - `import androidx.compose.foundation.clickable`
  - `import androidx.compose.foundation.lazy.LazyColumn`
  - `import androidx.compose.foundation.lazy.items` (may not be needed if using count-based)
  - `import androidx.compose.foundation.layout.Row`
  - `import androidx.compose.foundation.layout.Spacer`
  - `import androidx.compose.foundation.layout.size`
  - `import androidx.compose.foundation.layout.width`
  - `import androidx.compose.foundation.shape.RoundedCornerShape`
  - `import androidx.compose.material3.Surface`
  - `import androidx.compose.ui.draw.clip`
  - `import androidx.compose.ui.text.font.FontWeight`
  - `import com.simplevideo.whiteiptv.domain.model.ChannelViewMode`

### No New Files

All changes are modifications to existing files. No new files are needed.

### Database Changes

None.

### DI Changes

None. `SettingsPreferences` is already registered as a `single` in `settingsModule` (KoinModule.kt line 116). Koin will auto-resolve the new constructor parameter for `ChannelsViewModel` since `SettingsPreferences` is already in the graph.

## Implementation Order

1. **Add `channelViewModeFlow` to `SettingsPreferences`** (`shared/src/commonMain/.../data/local/SettingsPreferences.kt`). Add the reactive Flow property mirroring `autoUpdateEnabledFlow`.

2. **Add `channelViewMode` to `ChannelsState`** (`shared/src/commonMain/.../feature/channels/mvi/ChannelsMvi.kt`). Add the field with default `ChannelViewMode.List` and the import.

3. **Update `ChannelsViewModel` to observe view mode** (`shared/src/commonMain/.../feature/channels/ChannelsViewModel.kt`). Add `SettingsPreferences` constructor parameter, add `observeViewMode()` method, call it from `init`.

4. **Update `ChannelsScreen` to render both layouts** (`shared/src/commonMain/.../feature/channels/ChannelsScreen.kt`). Add `ChannelListItem` composable, replace unconditional grid with `when` branch on `channelViewMode`, add necessary imports.

5. **Run formatting and lint** (`./gradlew formatAll`).

6. **Build and test** (`./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug`).

## Testing Strategy

### Unit Tests

#### SettingsPreferences -- `channelViewModeFlow`
- File: `shared/src/commonTest/.../data/local/SettingsPreferencesTest.kt` (existing)
- Add test: `channelViewModeFlow emits List as default`
- Add test: `channelViewModeFlow emits updated value after setChannelViewMode`
- Add test: `channelViewModeFlow emits List for invalid stored value`

#### ChannelsViewModel -- view mode observation
- File: `shared/src/commonTest/.../feature/channels/ChannelsViewModelTest.kt` (create if not exists, or add to existing)
- Test: initial state has `channelViewMode` matching the preference default
- Test: state updates when `channelViewModeFlow` emits a new value
- **Coroutine test patterns**: Use `TestScope` + `StandardTestDispatcher` for DataStore tests. ChannelsViewModel needs `Dispatchers.Main` set to test dispatcher via `setMain`.

### Manual Testing
- Set view mode to "List" in Settings, navigate to Channels tab -- should show single-column list with logo, name, group, and star
- Set view mode to "Grid" in Settings, navigate to Channels tab -- should show 2-column grid (existing behavior)
- Toggle view mode while on Channels tab (via split screen or quick settings toggle) -- layout should update reactively
- Scroll to trigger paging in both modes -- loading spinner should appear correctly
- Verify favorite toggle works in both List and Grid modes
- Verify search works in both modes

### Edge Cases
- Channel with no logo URL -- both layouts should handle gracefully (AsyncImage shows placeholder)
- Channel with no tvgLanguage/tvgCountry -- List mode should not show empty subtitle line
- Very long channel names -- both layouts should truncate with ellipsis
- Empty channel list -- empty state should render regardless of view mode (it is above the when branch)

## Doc Updates Required

After implementation:

- **`docs/features/channel-browsing.md`**: Update "Channel Grid" section to document both List and Grid layouts, mention that the mode is controlled from Settings.
  > [!NOTE] Update AFTER implementation

- **`docs/constraints/current-limitations.md`**: No new limitation entry needed (this removes a limitation). If there was a listed limitation about view mode not working, remove it.
  > [!NOTE] Update AFTER implementation

## Build & Test Commands

```bash
./gradlew :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
./gradlew formatAll
```
