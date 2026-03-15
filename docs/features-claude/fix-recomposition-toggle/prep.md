# Fix Excessive Recomposition on Favorite Toggle -- Implementation Plan

## Summary

When a user toggles the favorite status on a single channel card, all visible cards recompose instead of just the one that changed. On the Channels screen this is a performance issue; on the Favorites screen it also resets scroll position to top. The root cause is twofold: (1) on Channels, `refreshTrigger` forces the entire `PagingSource` to be re-created, which emits a completely new `PagingData` snapshot, and (2) neither `ChannelCardList` nor `ChannelCardSquare` are skippable -- they receive lambda parameters that are recreated on every recomposition. The fix applies an optimistic UI overlay in the ViewModel so the paging data does not need to refresh, and makes the card composables skippable by stabilizing their lambda inputs.

## Decisions Made

### Decision 1: Optimistic UI overlay instead of Room invalidation

- **Decision**: Maintain a `Map<Long, Boolean>` in `ChannelsViewModel` that overrides the `isFavorite` field per channel ID. On toggle, immediately update the overlay and call the DB in the background. Remove `refreshTrigger` entirely.
- **Rationale**: The current approach (`refreshTrigger.value++`) forces a full PagingSource re-creation. Every visible item gets new object instances, causing all cards to recompose. With an overlay, the PagingData stays the same; only the one card whose overlay changed will recompose (because only its `isFavorite` parameter changes).
- **Alternatives considered**: (a) Room reactive PagingSource -- not available in KMP (room-paging artifact is not KMP-compatible, hence the custom `ChannelPagingSource`). (b) Just relying on `data class` equality -- `ChannelEntity` IS a data class, but the problem is the PagingSource itself is destroyed and recreated, producing all-new objects regardless of equality. (c) `PagingData.map` to apply overlay -- this works but still emits a new snapshot; combined with stable keys, Compose would skip unchanged items only if the card composables are skippable.

### Decision 2: Use `PagingData.map` with overlay StateFlow for Channels

- **Decision**: Instead of a separate overlay consumed in the Screen, apply the overlay via `PagingData.map` combined with `flatMapLatest` on the overlay flow. This keeps the screen composable simple and avoids passing extra state.
- **Rationale**: `PagingData.map` transforms items in-place without invalidating the PagingSource. Combined with `cachedIn`, only changed items trigger recomposition when the card composable uses stable parameters. This is cleaner than having the Screen layer merge two data sources.

### Decision 3: Optimistic removal for Favorites screen

- **Decision**: On Favorites, when the user un-favorites a channel, immediately remove it from `viewState.channels` in the ViewModel. The Room Flow will eventually emit the updated list, but the optimistic removal prevents the visual glitch of the entire list re-rendering.
- **Rationale**: Favorites uses a `Flow<List<ChannelEntity>>` from Room (not paging). When Room emits a new list, the `items(key = { it.id })` ensures Compose can diff correctly. The optimistic removal makes the un-favorite feel instant and prevents scroll position reset because the list change is a single-item removal rather than a full list replacement.

### Decision 4: Do NOT add `@Stable` or make cards skippable via wrapper

- **Decision**: The card composables (`ChannelCardList`, `ChannelCardSquare`) already receive only primitive/stable parameters (`String`, `Boolean`, lambdas). Compose compiler should be able to skip them when parameters do not change. The real issue is that ALL parameters change because new `ChannelEntity` objects come from re-created PagingSource. Fixing the data flow (decisions 1-3) is sufficient without adding `@Stable` annotations.
- **Rationale**: Adding `@Stable` or wrapping lambdas in `remember` is a micro-optimization that addresses symptoms, not the root cause. Once the PagingSource stops being invalidated on every toggle, items retain identity through keys and only the toggled item's `isFavorite` parameter changes.

## Current State

### Channels Screen -- Favorite Toggle Flow

1. `ChannelsViewModel.toggleFavoriteChannel()` (line 175-184 of `ChannelsViewModel.kt`):
   - Calls `toggleFavorite(channelId)` (DB update)
   - Then increments `refreshTrigger.value++`
   - `refreshTrigger` is combined into `pagedChannels` flow (line 57-69), causing `flatMapLatest` to re-execute `getPagedChannels()`, which creates a **brand new `ChannelPagingSource`**
   - The new PagingSource loads fresh data from Room, producing new `ChannelEntity` instances for ALL visible items
   - `collectAsLazyPagingItems()` emits the new snapshot, causing all items to recompose

2. Screen (`ChannelsScreen.kt` lines 192-205, 227-242):
   - Uses `pagedItems.itemKey { it.id }` -- keys are correct
   - Passes `channel.isFavorite` directly to cards
   - Cards receive lambdas inline: `{ onToggleFavorite(channel.id) }` -- new lambda per recomposition, but this is only a problem if the item itself recomposes

### Favorites Screen -- Favorite Toggle Flow

1. `FavoritesViewModel` (line 79-82):
   - Calls `toggleFavorite(viewEvent.channelId)` directly in `viewModelScope.launch`
   - No optimistic update -- waits for Room Flow to emit new list
   - Room Flow emits entire new `List<ChannelEntity>` missing the un-favorited channel
   - `viewState.copy(channels = channels)` replaces entire list
   - Because the state object reference changes, the whole `ChannelsList` composable recomposes

2. Screen (`FavoritesScreen.kt` lines 187-224):
   - Uses `items(state.channels, key = { it.id })` -- keys are correct
   - `showFavoriteButton = false` -- favorite button is hidden, but un-favorite is still possible (this seems like a UI choice, not a bug)

### Key Files

| File | Lines | Role |
|------|-------|------|
| `shared/src/commonMain/.../feature/channels/ChannelsViewModel.kt` | 175-184 | Toggle handler with refreshTrigger |
| `shared/src/commonMain/.../feature/channels/ChannelsViewModel.kt` | 57-69 | pagedChannels flow with refreshTrigger |
| `shared/src/commonMain/.../feature/channels/ChannelsScreen.kt` | 192-205, 227-242 | Card rendering in grid/list |
| `shared/src/commonMain/.../feature/favorites/FavoritesViewModel.kt` | 79-82 | Toggle handler (no optimistic update) |
| `shared/src/commonMain/.../feature/favorites/FavoritesScreen.kt` | 187-224 | Favorites list rendering |
| `shared/src/commonMain/.../data/local/paging/ChannelPagingSource.kt` | 1-42 | Custom PagingSource |
| `shared/src/commonMain/.../domain/usecase/GetPagedChannelsUseCase.kt` | 1-78 | Pager factory |
| `shared/src/commonMain/.../common/components/ChannelCard.kt` | 43-132, 140-214 | Card composables |

## Changes Required

### Modified Files

#### 1. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/ChannelsViewModel.kt`

**What changes:**
- Remove `refreshTrigger` field entirely (line 57)
- Remove `refreshTrigger` from the `combine` in `pagedChannels` (lines 59-69)
- Add `private val _favoriteOverrides = MutableStateFlow<Map<Long, Boolean>>(emptyMap())` field
- Change `pagedChannels` to combine with `_favoriteOverrides` and apply `PagingData.map` to override `isFavorite`:
  ```kotlin
  val pagedChannels: Flow<PagingData<ChannelEntity>> = combine(
      currentPlaylistRepository.selection,
      selectedGroupIdFlow,
      searchQuery.debounce(300),
  ) { selection, selectedGroupId, query ->
      Triple(selection, selectedGroupId, query)
  }.flatMapLatest { (selection, selectedGroupId, query) ->
      val filter = resolveFilter(selection, selectedGroupId)
      getPagedChannels(filter, query)
  }.cachedIn(viewModelScope)
      .combine(_favoriteOverrides) { pagingData, overrides ->
          if (overrides.isEmpty()) {
              pagingData
          } else {
              pagingData.map { channel ->
                  val override = overrides[channel.id]
                  if (override != null) {
                      channel.copy(isFavorite = override)
                  } else {
                      channel
                  }
              }
          }
      }
  ```
- Rewrite `toggleFavoriteChannel()`:
  ```kotlin
  private fun toggleFavoriteChannel(channelId: Long) {
      viewModelScope.launch {
          // Optimistic: flip in overlay
          val current = _favoriteOverrides.value
          val currentValue = current[channelId]
          // We don't know the original value yet, so we toggle the override
          // If already in overrides, flip it; otherwise we'll need to read from paging data
          // Simpler approach: just track which IDs have been toggled (XOR logic)
          val newOverrides = current.toMutableMap()
          if (channelId in newOverrides) {
              newOverrides.remove(channelId) // toggle back = remove override
          } else {
              newOverrides[channelId] = true // mark as toggled
          }
          _favoriteOverrides.value = newOverrides

          try {
              toggleFavorite(channelId)
          } catch (e: Exception) {
              // Revert on error
              _favoriteOverrides.value = _favoriteOverrides.value.toMutableMap().also {
                  if (channelId in current) {
                      it[channelId] = current[channelId]!!
                  } else {
                      it.remove(channelId)
                  }
              }
              viewAction = ChannelsAction.ShowError(e.message ?: "Unknown error")
          }
      }
  }
  ```

**Wait -- the XOR approach is simpler.** Since we do not know the original `isFavorite` value at toggle time in the ViewModel (it is inside PagingData), we should track toggled IDs as a `Set<Long>` and XOR at the mapping step:

- Add `private val _toggledFavoriteIds = MutableStateFlow<Set<Long>>(emptySet())` instead of `_favoriteOverrides`
- In `PagingData.map`: `channel.copy(isFavorite = if (channel.id in toggledIds) !channel.isFavorite else channel.isFavorite)`
- On toggle: add ID to set. If toggled twice, remove from set (back to original).
- On error: revert by removing/re-adding the ID.

**Revised approach (final):**

```kotlin
private val _toggledFavoriteIds = MutableStateFlow<Set<Long>>(emptySet())

val pagedChannels: Flow<PagingData<ChannelEntity>> = combine(
    currentPlaylistRepository.selection,
    selectedGroupIdFlow,
    searchQuery.debounce(300),
) { selection, selectedGroupId, query ->
    Triple(selection, selectedGroupId, query)
}.flatMapLatest { (selection, selectedGroupId, query) ->
    val filter = resolveFilter(selection, selectedGroupId)
    getPagedChannels(filter, query)
}.cachedIn(viewModelScope)
    .combine(_toggledFavoriteIds) { pagingData, toggledIds ->
        if (toggledIds.isEmpty()) {
            pagingData
        } else {
            pagingData.map { channel ->
                if (channel.id in toggledIds) {
                    channel.copy(isFavorite = !channel.isFavorite)
                } else {
                    channel
                }
            }
        }
    }

private fun toggleFavoriteChannel(channelId: Long) {
    // Optimistic UI: toggle immediately
    _toggledFavoriteIds.value = _toggledFavoriteIds.value.let { ids ->
        if (channelId in ids) ids - channelId else ids + channelId
    }
    viewModelScope.launch {
        try {
            toggleFavorite(channelId)
        } catch (e: Exception) {
            // Revert optimistic toggle on error
            _toggledFavoriteIds.value = _toggledFavoriteIds.value.let { ids ->
                if (channelId in ids) ids - channelId else ids + channelId
            }
            viewAction = ChannelsAction.ShowError(e.message ?: "Unknown error")
        }
    }
}
```

**Why**: Remove the need for `refreshTrigger` entirely. The PagingSource is never re-created on toggle. Only the mapped `isFavorite` field changes for the one toggled channel. Compose sees the key is the same, and only the `isFavorite` parameter changed, so only that one card recomposes.

**Import needed**: `import androidx.paging.map`

#### 2. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/favorites/FavoritesViewModel.kt`

**What changes:**
- In `FavoritesEvent.OnToggleFavorite` handler (lines 79-82), add optimistic removal before DB call:
  ```kotlin
  is FavoritesEvent.OnToggleFavorite -> {
      // Optimistic removal: immediately remove from displayed list
      val channelId = viewEvent.channelId
      viewState = viewState.copy(
          channels = viewState.channels.filter { it.id != channelId },
      )
      viewModelScope.launch {
          try {
              toggleFavorite(channelId)
          } catch (e: Exception) {
              // Room Flow will restore the correct state on error
              // since the channel is still favorited in DB
          }
      }
  }
  ```

**Why**: The optimistic removal ensures the list changes by exactly one item. With `key = { it.id }`, Compose diffs correctly and only removes/animates the one item. Scroll position is preserved. When Room Flow eventually emits, the list is the same (item already removed), so no additional recomposition occurs.

### No New Files Required

This fix modifies only two existing ViewModel files.

### No Database Changes

The DAO `toggleFavoriteStatus` query remains unchanged.

### No DI Changes

No new dependencies are introduced.

## Implementation Order

1. **Modify `ChannelsViewModel.kt`**:
   - Remove `refreshTrigger` field and its usage in `combine`
   - Add `_toggledFavoriteIds: MutableStateFlow<Set<Long>>`
   - Restructure `pagedChannels` to chain `.combine(_toggledFavoriteIds)` AFTER `.cachedIn(viewModelScope)`
   - Rewrite `toggleFavoriteChannel()` to use optimistic toggle via `_toggledFavoriteIds`
   - Add import for `androidx.paging.map`

2. **Modify `FavoritesViewModel.kt`**:
   - In `OnToggleFavorite` handler, add optimistic list filtering before the DB call
   - Wrap `toggleFavorite` in try/catch (already in a coroutine)

3. **Verify** -- build and manual test:
   - Toggle favorite on Channels screen: only one card should recompose
   - Toggle favorite twice quickly: should correctly XOR back
   - Toggle favorite on error (airplane mode): should revert
   - Favorites screen: un-favorite should instantly remove one item, scroll position preserved

## Testing Strategy

### Manual Testing (Primary -- UI behavior)

- **Channels screen**: Enable `RecompositionConfig.isEnabled`, toggle one channel's favorite, verify only one `ChannelCardList recomposed` / `ChannelCardSquare recomposed` log line appears (not 8+).
- **Channels screen double-toggle**: Toggle same channel twice rapidly. Verify star icon toggles back and forth correctly, and DB state is consistent.
- **Channels screen error**: Disable network (if toggle used network) or test with a forced error scenario. Verify the star reverts.
- **Favorites screen**: Un-favorite a channel in the middle of a long list. Verify the item disappears and scroll position does not jump.
- **Cross-screen consistency**: Toggle favorite on Channels, navigate to Favorites, verify the channel appears/disappears. Toggle on Favorites, go back to Channels, verify star state is correct.

### Unit Tests

The `_toggledFavoriteIds` logic in `ChannelsViewModel` is deterministic and testable:
- Toggle once: ID appears in set
- Toggle twice: ID removed from set
- Error: ID reverts

The optimistic removal in `FavoritesViewModel` can be tested by checking `viewState.channels` immediately after sending the event (before coroutine completes).

**Coroutine test patterns**: Both ViewModels use `viewModelScope`. Tests should use `runTest` with `StandardTestDispatcher` and `Dispatchers.setMain()` for controlling coroutine execution.

### Edge Cases

- Toggle while PagingSource is loading a new page (should be fine -- overlay applies to any page)
- Toggle channel that scrolled off-screen and back (cached PagingData + overlay should still apply)
- Very rapid toggles on multiple different channels (set operations are thread-safe via StateFlow)
- Navigation away and back to Channels: `_toggledFavoriteIds` accumulates but is harmless since XOR is applied on stale-but-correct cached data. When PagingSource eventually refreshes (e.g., due to filter change), the toggledIds set should be cleared. **Add a `LaunchedEffect`-style clearing** -- when the `flatMapLatest` re-executes (filter/search changed), clear the toggled set:
  ```kotlin
  }.flatMapLatest { (selection, selectedGroupId, query) ->
      _toggledFavoriteIds.value = emptySet() // Clear on filter change
      val filter = resolveFilter(selection, selectedGroupId)
      getPagedChannels(filter, query)
  }
  ```

## Doc Updates Required

No documentation updates are needed. This is a performance bugfix with no user-facing behavior change. The `docs/constraints/current-limitations.md` does not list this issue, so no removal is needed.

## Build & Test Commands

```bash
./gradlew :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
./gradlew formatAll
```
