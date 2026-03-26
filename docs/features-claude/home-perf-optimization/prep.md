# HomeScreen Performance Optimization — Implementation Plan

## Problem

HomeScreen lags during rendering due to excessive recompositions. Root causes:

1. **Single monolithic `HomeState`** — any field change triggers full screen recomposition
2. **`combine(4 flows)` inside `flatMapLatest`** — playlists and continueWatching don't depend on selection but re-execute on every selection change
3. **`getRandomChannelsForGroups` loads ALL channels for groups into memory** — with 5 groups × 5000 channels = 25000+ rows loaded, then shuffled in Kotlin
4. **Group filtering in Kotlin** instead of SQL — fetches 20 groups, filters invalid names in memory

## Changes

### A. HomeViewModel — Split into Independent Flows

**File:** `feature/home/HomeViewModel.kt`

Current: single `combine(getPlaylists, getContinueWatching, getFavorites, getHomeCategories)` inside `flatMapLatest(selection)`.

New architecture:
- `playlists: StateFlow<ImmutableList<PlaylistEntity>>` — independent, outside flatMapLatest
- `continueWatchingItems: StateFlow<ImmutableList<ContinueWatchingItem>>` — independent, outside flatMapLatest
- `favoriteChannels: StateFlow<ImmutableList<ChannelEntity>>` — depends on selection, in flatMapLatest
- `categories: StateFlow<ImmutableList<CategoryItem>>` — depends on selection, in flatMapLatest
- `viewState` (HomeState) — only UI flags: isLoading, dialog booleans, error, search state, isUpdatingPlaylist, playlistManagementError

Implementation:
```kotlin
// Independent flows (don't re-execute on selection change)
val playlists: StateFlow<ImmutableList<PlaylistEntity>>
val continueWatchingItems: StateFlow<ImmutableList<ContinueWatchingItem>>

// Selection-dependent flows
val favoriteChannels: StateFlow<ImmutableList<ChannelEntity>>
val categories: StateFlow<ImmutableList<CategoryItem>>
```

Each flow is a `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue)`.

### B. HomeMvi — Slim Down HomeState

**File:** `feature/home/mvi/HomeMvi.kt`

Remove data lists from HomeState. Keep only:
```kotlin
@Immutable
data class HomeState(
    val selection: PlaylistSelection = PlaylistSelection.All,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val searchResults: ImmutableList<ChannelEntity> = persistentListOf(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showPlaylistSettings: Boolean = false,
    val showRenameDialog: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val showViewUrlDialog: Boolean = false,
    val isUpdatingPlaylist: Boolean = false,
    val playlistManagementError: String? = null,
)
```

### C. HomeScreen — Subscribe to Separate Flows

**File:** `feature/home/HomeScreen.kt`

Each section subscribes to its own StateFlow:
```kotlin
val playlists by viewModel.playlists.collectAsState()
val continueWatching by viewModel.continueWatchingItems.collectAsState()
val favorites by viewModel.favoriteChannels.collectAsState()
val categories by viewModel.categories.collectAsState()
val state by viewModel.viewStates().collectAsState() // only UI flags
```

This limits recomposition scope — changing categories won't recompose the top bar or dialogs.

### D. DAO — Optimize getChannelsForGroupIds with LIMIT per group

**File:** `data/local/PlaylistDao.kt`

Replace the current query that fetches ALL channels for groups:
```sql
-- CURRENT: fetches everything, shuffles in Kotlin
SELECT c.*, cgr.groupId AS crossRefGroupId FROM channels c
INNER JOIN channel_group_cross_ref cgr ON c.id = cgr.channelId
WHERE cgr.groupId IN (:groupIds)
```

New: Add a DAO method with per-group random limit using a ranked subquery approach.
Since Room KMP doesn't support window functions (ROW_NUMBER), use a simpler approach —
keep the batch query but add ORDER BY RANDOM() and use the in-memory grouping with take().
The key optimization is that we already have `getRandomChannelsByGroupId` with LIMIT per group.
Change `getRandomChannelsForGroups` to call that per group (5 groups × 1 query = 5 small queries
instead of 1 massive query):

```kotlin
override suspend fun getRandomChannelsForGroups(
    groupIds: List<Long>,
    limitPerGroup: Int,
): Map<Long, List<ChannelEntity>> =
    groupIds.associateWith { groupId ->
        playlistDao.getRandomChannelsByGroupId(groupId, limitPerGroup)
    }
```

This uses the existing `getRandomChannelsByGroupId` which already has `ORDER BY RANDOM() LIMIT :limit` —
5 queries of ~10 rows each (50 total) vs 1 query of 25000+ rows.

### E. GetHomeCategoriesUseCase — Filter invalid groups in SQL

**File:** `domain/usecase/GetHomeCategoriesUseCase.kt`
**File:** `data/local/PlaylistDao.kt`
**File:** `domain/repository/ChannelRepository.kt`
**File:** `data/repository/ChannelRepositoryImpl.kt`

Add new DAO queries that exclude invalid group names:
```sql
SELECT * FROM channel_groups
WHERE name != '' AND LOWER(name) NOT IN ('undefined', 'unknown', 'other')
ORDER BY channelCount DESC
LIMIT :limit
```

Add `getTopValidGroups(limit)` and `getTopValidGroupsByPlaylist(playlistId, limit)` to DAO.
Add corresponding methods to ChannelRepository interface and impl.
Update GetHomeCategoriesUseCase to use new methods and remove Kotlin filtering.

## Files Modified

| File | Change |
|------|--------|
| `feature/home/mvi/HomeMvi.kt` | Remove data lists from HomeState |
| `feature/home/HomeViewModel.kt` | Split into independent StateFlows |
| `feature/home/HomeScreen.kt` | Subscribe to separate flows, pass to sections |
| `data/local/PlaylistDao.kt` | Add `getTopValidGroups` queries |
| `domain/repository/ChannelRepository.kt` | Add `getTopValidGroups` methods |
| `data/repository/ChannelRepositoryImpl.kt` | Implement new methods + optimize `getRandomChannelsForGroups` |
| `domain/usecase/GetHomeCategoriesUseCase.kt` | Use SQL filtering, remove Kotlin filter |

## No Changes Needed

- `BaseViewModel` — stays as-is, additional StateFlows live alongside `viewState`
- `GetFavoritesUseCase`, `GetContinueWatchingUseCase`, `GetPlaylistsUseCase` — unchanged
- Database schema — no migration needed (queries only)

## Risk Assessment

- **Low risk**: splitting flows is additive, doesn't change behavior
- **Low risk**: SQL filtering is equivalent to Kotlin filtering
- **Medium risk**: `getRandomChannelsForGroups` change from 1 query to N queries — but N=5 and each is tiny with LIMIT 10
