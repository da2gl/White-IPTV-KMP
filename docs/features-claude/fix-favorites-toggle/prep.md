# Fix Favorite Toggle in Channels Screen — Implementation Plan

## Summary

When a user taps the favorite (star) icon on a channel card in the Channels screen, the database is
updated but the UI does not reflect the change. This happens because `ChannelPagingSource` is a
snapshot-based source that does not automatically invalidate when the underlying data changes. The
fix is to invalidate the current `PagingSource` after every favorite toggle, causing the Pager to
re-query and refresh the displayed items.

## Root Cause Analysis

The Favorites screen works because it uses Room's `Flow<List<ChannelEntity>>` queries, which
automatically re-emit when the `channels` table changes. The Channels screen uses a custom
`ChannelPagingSource` backed by suspend queries (not Flow). The `Pager` creates a
`ChannelPagingSource` instance and reads from it. After `toggleFavoriteStatus()` updates the DB, the
existing `PagingSource` has no mechanism to know the data changed, so it continues to serve stale
items with the old `isFavorite` value.

## Decisions Made

- **Decision**: Invalidate the PagingSource after toggle rather than switching to a reactive
  PagingSource or maintaining local state.
- **Rationale**: PagingSource invalidation is the standard Paging 3 approach. It causes a fresh
  query from the DB while preserving scroll position (Pager re-fetches around the last anchor
  position). Local state management (optimistically flipping the flag in the PagingData) is fragile
  and diverges from the single-source-of-truth pattern. A reactive PagingSource (room-paging) is not
  KMP-compatible, as documented in `ChannelPagingSource.kt` line 9.
- **Alternatives considered**:
  1. Optimistic UI update by modifying PagingData in-memory — rejected because it requires complex
     snapshot management and can drift from DB state.
  2. Full refresh via `pagedItems.refresh()` from the Screen — rejected because it couples UI to
     business logic and does not follow MVI pattern.

## Current State

### ChannelsViewModel (`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/ChannelsViewModel.kt`)

- Lines 57-66: `pagedChannels` Flow is created by combining `currentPlaylistRepository.selection`,
  `selectedGroupIdFlow`, and `searchQuery`, then `flatMapLatest` into `getPagedChannels()`. Each new
  emission of filter/query params creates a fresh `Pager` and `PagingSource`. But toggling favorite
  does not change any of these trigger flows, so no new `PagingSource` is created.
- Lines 172-180: `toggleFavoriteChannel()` calls `toggleFavorite(channelId)` but does nothing to
  signal the paging pipeline.

### GetPagedChannelsUseCase (`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/usecase/GetPagedChannelsUseCase.kt`)

- Lines 29-35: Creates a `Pager` with `pagingSourceFactory` lambda. The factory is called whenever
  the previous `PagingSource` is invalidated. This is the mechanism we need to trigger.

### ChannelPagingSource (`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/paging/ChannelPagingSource.kt`)

- Extends `PagingSource<Int, ChannelEntity>`. Has an `invalidate()` method inherited from
  `PagingSource` that marks this source as invalid, causing the `Pager` to call the factory to
  create a new one.

## Changes Required

### Modified Files

#### 1. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/ChannelsViewModel.kt`

**What changes**: Add a `MutableSharedFlow` trigger that the `toggleFavoriteChannel` method emits
to after a successful toggle. Include this trigger in the `pagedChannels` flow pipeline so that each
emission causes `flatMapLatest` to create a new Pager/PagingSource.

**Specific changes**:

Add a new field:
```kotlin
private val refreshTrigger = MutableSharedFlow<Long>(replay = 1)
```

Initialize it in init or at declaration with an initial value so `combine` emits immediately:
```kotlin
private val refreshTrigger = MutableStateFlow(0L)
```

Using `MutableStateFlow<Long>` with a timestamp/counter is simpler. Each toggle increments or
updates it, causing `flatMapLatest` to re-execute.

Modify the `pagedChannels` flow to include `refreshTrigger`:
```kotlin
val pagedChannels: Flow<PagingData<ChannelEntity>> = combine(
    currentPlaylistRepository.selection,
    selectedGroupIdFlow,
    searchQuery.debounce(300),
    refreshTrigger,
) { selection, selectedGroupId, query, _ ->
    Triple(selection, selectedGroupId, query)
}.flatMapLatest { (selection, selectedGroupId, query) ->
    val filter = resolveFilter(selection, selectedGroupId)
    getPagedChannels(filter, query)
}.cachedIn(viewModelScope)
```

Modify `toggleFavoriteChannel` to emit to the trigger after success:
```kotlin
private fun toggleFavoriteChannel(channelId: Long) {
    viewModelScope.launch {
        try {
            toggleFavorite(channelId)
            refreshTrigger.value++
        } catch (e: Exception) {
            viewAction = ChannelsAction.ShowError(e.message ?: "Unknown error")
        }
    }
}
```

**Why**: This is the minimal change that uses the existing `flatMapLatest` pattern. When
`refreshTrigger` emits a new value, `flatMapLatest` cancels the old Pager flow and creates a new
one via `getPagedChannels()`, which creates a fresh `PagingSource` that reads updated data from Room.

### No New Files

No new files are needed. This is a one-file fix.

### No Database Changes

The DB toggle query works correctly. The issue is purely in the presentation layer refresh mechanism.

### No DI Changes

No new dependencies are introduced.

## Implementation Order

1. Add `refreshTrigger` field to `ChannelsViewModel` (MutableStateFlow<Long> initialized to 0).
2. Add `refreshTrigger` to the `combine()` call in `pagedChannels` property.
3. Add `refreshTrigger.value++` after successful `toggleFavorite()` call in
   `toggleFavoriteChannel()`.
4. Verify the fix builds and test manually.

## Testing Strategy

### Manual Testing
- Open Channels screen with "All" filter (no group selected).
- Tap the star icon on any channel card.
- Verify the star icon updates immediately (filled/gold vs outlined).
- Scroll down, toggle a favorite, verify it updates.
- Switch to a group filter, toggle favorite, verify it updates.
- Switch back to "All", verify previous toggles are persisted.
- Navigate to Favorites screen, verify toggled channels appear/disappear correctly.

### Unit Test
- `ChannelsViewModel` test verifying that after `OnToggleFavorite` event, the `refreshTrigger`
  value increments (or more directly, that `pagedChannels` re-emits).
- Edge case: rapid double-tap on favorite should not cause issues (two increments, two refreshes,
  net effect is original state restored).

### Coroutine Test Patterns
- Use `StandardTestDispatcher` with `advanceUntilIdle()` for ViewModel tests.
- The `pagedChannels` flow uses `cachedIn(viewModelScope)` so tests need a test scope that supports
  it (use `Dispatchers.setMain(testDispatcher)` pattern).

## Build & Test Commands

```bash
./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug
```

## Doc Updates Required

- Update `docs/constraints/current-limitations.md` AFTER implementation to remove this bug if it was
  listed (it is not currently listed, so no change needed).
