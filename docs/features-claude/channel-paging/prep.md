# Channel Paging -- Implementation Plan

## Summary

Add Paging 3 support to the Channels screen (and optionally Favorites screen) so that 10k+ channel playlists load incrementally instead of all-at-once. This uses the official AndroidX `paging-common` and `paging-compose` libraries (version 3.4.1), which are KMP-compatible. Because `room-paging` (the artifact that auto-generates `PagingSource` from Room DAO return types) is NOT KMP-compatible, we will write a custom `PagingSource` backed by Room LIMIT/OFFSET queries and use `Pager` in the repository layer.

## Decisions Made

### Decision 1: Use official AndroidX Paging 3.4.1 (not CashApp multiplatform-paging)

- **Decision**: Use `androidx.paging:paging-common:3.4.1` and `androidx.paging:paging-compose:3.4.1`.
- **Rationale**: CashApp's `multiplatform-paging` was archived on Sep 3, 2025. The official AndroidX paging-common and paging-compose artifacts have supported KMP (JVM, iOS, macOS, Linux, etc.) since version 3.4.0-alpha03, and version 3.4.1 is the latest stable release. No reason to use the deprecated library.
- **Alternatives considered**: CashApp `app.cash.paging:paging-compose-common:3.3.0-alpha02-0.5.1` (archived, not maintained).

### Decision 2: Custom PagingSource instead of Room DAO PagingSource return type

- **Decision**: Write a custom `ChannelPagingSource` class that wraps Room DAO LIMIT/OFFSET suspend queries. Do NOT use `PagingSource<Int, ChannelEntity>` as a Room DAO return type.
- **Rationale**: The `androidx.room:room-paging` artifact (which enables Room to auto-generate PagingSource from DAO return types) does NOT support KMP/iOS. It only works on Android. Since this project targets both Android and iOS from commonMain, we must implement our own PagingSource that calls Room DAO suspend functions with LIMIT/OFFSET parameters.
- **Alternatives considered**: (1) Using `room-paging` in androidMain only with expect/actual -- rejected because it would require duplicating the entire paging pipeline for each platform. (2) Abandoning paging and using simple chunked loading -- rejected because PagingData + LazyPagingItems provides superior UX with automatic prefetch, placeholder support, and Compose integration.

### Decision 3: Do NOT page the Favorites screen

- **Decision**: Keep FavoritesScreen using the current `Flow<List<ChannelEntity>>` approach without paging.
- **Rationale**: Favorites are a user-curated subset, typically 10-100 channels even for large playlists. Paging adds complexity (custom PagingSource, invalidation tracking) for no meaningful benefit. If a user somehow has thousands of favorites, we can add paging later.
- **Alternatives considered**: Paging Favorites too -- rejected for simplicity.

### Decision 4: Do NOT page the Home screen search results

- **Decision**: Keep HomeViewModel search results as `Flow<List<ChannelEntity>>`.
- **Rationale**: Home search results are already debounced and the result set is typically small (users type specific queries). The Home screen categories use small fixed-size subsets (`getRandomChannelsByGroupId` with limits). No paging needed.

### Decision 5: Page size of 50 items

- **Decision**: Use a page size of 50 channels with prefetchDistance of 25.
- **Rationale**: Each channel card in the 2-column grid takes roughly half the screen width. A page of 50 fills ~25 rows, which is well beyond a single viewport. A prefetchDistance of 25 means the next page loads when the user is halfway through the current page, providing seamless scrolling. This is a standard configuration for grid-based content.
- **Alternatives considered**: 20 (too small, frequent loads), 100 (too large initial load for the same problem we're solving).

### Decision 6: Keep ChannelsFilter and ChannelsUseCase pattern, add a paged variant

- **Decision**: Create a new `GetPagedChannelsUseCase` that returns `Flow<PagingData<ChannelEntity>>` instead of modifying the existing `GetChannelsUseCase`. The existing use case stays unchanged for HomeViewModel search.
- **Rationale**: The existing `GetChannelsUseCase` is used by HomeViewModel for search, which does not need paging. Creating a separate use case follows the single-responsibility principle and avoids breaking existing consumers.
- **Alternatives considered**: Modifying `GetChannelsUseCase` to return `PagingData` -- rejected because it would break HomeViewModel which needs `Flow<List<ChannelEntity>>`.

### Decision 7: Remove channels from ChannelsState, use LazyPagingItems directly

- **Decision**: Remove the `channels: List<ChannelEntity>` field from `ChannelsState`. Instead, expose `Flow<PagingData<ChannelEntity>>` from ChannelsViewModel as a separate flow that the Screen observes via `collectAsLazyPagingItems()`.
- **Rationale**: PagingData cannot be stored in a data class state (it's a stream, not a snapshot). The standard Paging 3 pattern is to expose it as a separate Flow from the ViewModel and collect it in the Composable. The rest of the state (playlists, groups, search query, etc.) remains in ChannelsState.

## Current State

### PlaylistDao (DAO layer)
- **File**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/PlaylistDao.kt`
- All channel queries return `Flow<List<ChannelEntity>>` (lines 47-48, 73-74, 188-196)
- Search queries also return `Flow<List<ChannelEntity>>` (lines 86-124)
- No LIMIT/OFFSET queries for channels exist currently
- No PagingSource return types

### ChannelRepository (interface)
- **File**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/repository/ChannelRepository.kt`
- All methods return `Flow<List<ChannelEntity>>` (lines 14-16, 22-23, 28-32)
- No paged variants exist

### ChannelRepositoryImpl (implementation)
- **File**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/repository/ChannelRepositoryImpl.kt`
- Directly delegates to PlaylistDao methods (lines 21-26, 41-44, 56-69)

### GetChannelsUseCase
- **File**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/usecase/GetChannelsUseCase.kt`
- Returns `Flow<List<ChannelEntity>>` (line 18)
- Dispatches to different repository methods based on `ChannelsFilter` and search query
- Used by both ChannelsViewModel and HomeViewModel

### ChannelsViewModel
- **File**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/ChannelsViewModel.kt`
- Uses `getChannels(filter, data.query)` in a `flatMapLatest` chain (line 91)
- Collects results into `viewState.channels` (lines 92-101)
- Filter chain: playlist selection -> group selection -> search query -> channels

### ChannelsState (MVI state)
- **File**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/mvi/ChannelsMvi.kt`
- `channels: List<ChannelEntity> = emptyList()` (line 9)
- Also holds playlists, groups, selectedGroup, searchQuery, isSearchActive, isLoading, error

### ChannelsScreen (UI)
- **File**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/ChannelsScreen.kt`
- Uses `LazyVerticalGrid` with `items(state.channels)` (lines 177-189)
- ChannelsContent receives state.channels as parameter

### ChannelsFilter (domain model)
- **File**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/model/ChannelsFilter.kt`
- Sealed interface: All, ByPlaylist(playlistId), ByGroup(groupId)

### KoinModule
- **File**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/di/KoinModule.kt`
- `GetChannelsUseCase` registered as factory (line 90)

### Build config
- **File**: `shared/build.gradle.kts`
- Room 2.8.4, commonMain dependencies at lines 52-80
- **File**: `gradle/libs.versions.toml`
- Room version: 2.8.4 (line 23)

## Changes Required

### New Dependencies (gradle/libs.versions.toml)

Add paging version and library entries:

```toml
# In [versions]
paging = "3.4.1"

# In [libraries]
paging-common = { module = "androidx.paging:paging-common", version.ref = "paging" }
paging-compose = { module = "androidx.paging:paging-compose", version.ref = "paging" }
```

### New Dependencies (shared/build.gradle.kts)

Add to `commonMain.dependencies`:
```kotlin
implementation(libs.paging.common)
implementation(libs.paging.compose)
```

### New Files

#### 1. DAO LIMIT/OFFSET queries (PlaylistDao.kt -- modified, see below)
No new DAO file, but new query methods are added.

#### 2. Custom PagingSource
- **Path**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/paging/ChannelPagingSource.kt`
- **Purpose**: Custom `PagingSource<Int, ChannelEntity>` that loads channels page-by-page using Room LIMIT/OFFSET queries. Handles invalidation when the database changes.
- **Key contents**:
  ```kotlin
  class ChannelPagingSource(
      private val queryExecutor: suspend (limit: Int, offset: Int) -> List<ChannelEntity>,
      private val countExecutor: suspend () -> Int,
  ) : PagingSource<Int, ChannelEntity>() {
      override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ChannelEntity>
      override fun getRefreshKey(state: PagingState<Int, ChannelEntity>): Int?
  }
  ```
  - Uses `LoadParams.key` as page index (0-based)
  - Computes OFFSET from page * pageSize
  - Returns `LoadResult.Page` with prevKey/nextKey
  - `getRefreshKey` returns the page closest to the last accessed anchor position

#### 3. GetPagedChannelsUseCase
- **Path**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/usecase/GetPagedChannelsUseCase.kt`
- **Purpose**: Creates a `Pager` with the correct `PagingSource` based on `ChannelsFilter` and search query. Returns `Flow<PagingData<ChannelEntity>>`.
- **Key contents**:
  ```kotlin
  class GetPagedChannelsUseCase(
      private val channelRepository: ChannelRepository,
  ) {
      operator fun invoke(
          filter: ChannelsFilter = ChannelsFilter.All,
          query: String = "",
      ): Flow<PagingData<ChannelEntity>>
  }
  ```
  - Creates a new `Pager` with `PagingConfig(pageSize = 50, prefetchDistance = 25)`
  - Factory lambda creates a `ChannelPagingSource` with appropriate query/count executors based on filter and query

### Modified Files

#### 1. PlaylistDao.kt
- **Path**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/PlaylistDao.kt`
- **What changes**: Add 8 new suspend queries with LIMIT/OFFSET and their corresponding COUNT queries:
  - `getChannelsPaged(limit: Int, offset: Int): List<ChannelEntity>` + `getChannelsCount(): Int`
  - `getChannelsByPlaylistIdPaged(playlistId: Long, limit: Int, offset: Int): List<ChannelEntity>` + `getChannelsByPlaylistIdCount(playlistId: Long): Int`
  - `getChannelsByGroupIdPaged(groupId: Long, limit: Int, offset: Int): List<ChannelEntity>` + `getChannelsByGroupIdCount(groupId: Long): Int`
  - `searchChannelsPaged(query: String, limit: Int, offset: Int): List<ChannelEntity>` + `searchChannelsCount(query: String): Int`
  - `searchChannelsByPlaylistIdPaged(query: String, playlistId: Long, limit: Int, offset: Int)` + count
  - `searchChannelsByGroupIdPaged(query: String, groupId: Long, limit: Int, offset: Int)` + count
- **Why**: PagingSource needs suspend functions that return `List<T>` with LIMIT/OFFSET, not `Flow<List<T>>`.

#### 2. ChannelRepository.kt (interface)
- **Path**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/repository/ChannelRepository.kt`
- **What changes**: Add paged query methods matching the new DAO queries:
  - `suspend fun getChannelsPaged(limit: Int, offset: Int): List<ChannelEntity>`
  - `suspend fun getChannelsCount(): Int`
  - `suspend fun getChannelsByPlaylistIdPaged(playlistId: Long, limit: Int, offset: Int): List<ChannelEntity>`
  - `suspend fun getChannelsByPlaylistIdCount(playlistId: Long): Int`
  - `suspend fun getChannelsByGroupIdPaged(groupId: Long, limit: Int, offset: Int): List<ChannelEntity>`
  - `suspend fun getChannelsByGroupIdCount(groupId: Long): Int`
  - `suspend fun searchChannelsPaged(query: String, limit: Int, offset: Int): List<ChannelEntity>`
  - `suspend fun searchChannelsCount(query: String): Int`
  - `suspend fun searchChannelsByPlaylistIdPaged(query: String, playlistId: Long, limit: Int, offset: Int): List<ChannelEntity>`
  - `suspend fun searchChannelsByPlaylistIdCount(query: String, playlistId: Long): Int`
  - `suspend fun searchChannelsByGroupIdPaged(query: String, groupId: Long, limit: Int, offset: Int): List<ChannelEntity>`
  - `suspend fun searchChannelsByGroupIdCount(query: String, groupId: Long): Int`
- **Why**: Repository must expose paged access for the use case.

#### 3. ChannelRepositoryImpl.kt
- **Path**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/repository/ChannelRepositoryImpl.kt`
- **What changes**: Implement all new paged methods by delegating to the corresponding DAO methods.
- **Why**: Standard repository pattern delegation.

#### 4. ChannelsMvi.kt (state)
- **Path**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/mvi/ChannelsMvi.kt`
- **What changes**: Remove `channels: List<ChannelEntity> = emptyList()` from `ChannelsState`. The channel data will flow separately as `Flow<PagingData<ChannelEntity>>`.
- **Why**: PagingData is a reactive stream, not a snapshot suitable for data class state.

#### 5. ChannelsViewModel.kt
- **Path**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/ChannelsViewModel.kt`
- **What changes**:
  - Replace `getChannels: GetChannelsUseCase` with `getPagedChannels: GetPagedChannelsUseCase` in the constructor.
  - Expose `val pagedChannels: Flow<PagingData<ChannelEntity>>` as a public property.
  - In `loadData()`, instead of collecting channels into state, produce a `Flow<PagingData<ChannelEntity>>` using `flatMapLatest` that calls `getPagedChannels(filter, query)` and caches it with `.cachedIn(viewModelScope)`.
  - The rest of the state (playlists, groups, selection, searchQuery) continues to be set in viewState.
- **Why**: PagingData must be collected at the Composable level via `collectAsLazyPagingItems()`.

#### 6. ChannelsScreen.kt
- **Path**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/ChannelsScreen.kt`
- **What changes**:
  - Import `androidx.paging.compose.collectAsLazyPagingItems` and `androidx.paging.compose.itemKey`.
  - In `ChannelsScreen`, collect paged items: `val pagedItems = viewModel.pagedChannels.collectAsLazyPagingItems()`.
  - Pass `pagedItems` to `ChannelsContent` instead of `state.channels`.
  - In `ChannelsContent`:
    - Change `LazyVerticalGrid` from `items(state.channels)` to `items(count = pagedItems.itemCount, key = pagedItems.itemKey { it.id })`.
    - Access items via `pagedItems[index]` inside the item lambda.
    - Handle loading/empty states using `pagedItems.loadState` instead of `state.isLoading` / `state.channels.isEmpty()`.
    - Show `CircularProgressIndicator` for `LoadState.Loading` as append indicator.
  - Remove `ChannelEntity` import if no longer needed directly in state-dependent code.
- **Why**: This is the standard Paging 3 Compose integration pattern.

#### 7. KoinModule.kt
- **Path**: `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/di/KoinModule.kt`
- **What changes**: Add `factoryOf(::GetPagedChannelsUseCase)` to `useCaseModule`.
- **Why**: New use case needs DI registration.

### Database Changes

No schema migration needed. The new DAO methods are just SELECT queries with LIMIT/OFFSET on existing tables. No new tables, columns, or indices are added.

### DI Changes

- Add `factoryOf(::GetPagedChannelsUseCase)` to `useCaseModule` in `KoinModule.kt`.

## Implementation Order

1. **Add dependencies**: Add `paging` version, `paging-common`, and `paging-compose` library entries to `gradle/libs.versions.toml`. Add `implementation(libs.paging.common)` and `implementation(libs.paging.compose)` to `commonMain.dependencies` in `shared/build.gradle.kts`.

2. **Add DAO queries**: Add the 12 new suspend methods (6 paged queries + 6 count queries) to `PlaylistDao.kt`. These are pure SQL with LIMIT/OFFSET and COUNT(*).

3. **Update ChannelRepository interface**: Add the 12 new method signatures to `ChannelRepository.kt`.

4. **Update ChannelRepositoryImpl**: Implement the 12 new methods by delegating to DAO in `ChannelRepositoryImpl.kt`.

5. **Create ChannelPagingSource**: Create `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/paging/ChannelPagingSource.kt` with the custom PagingSource implementation.

6. **Create GetPagedChannelsUseCase**: Create `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/usecase/GetPagedChannelsUseCase.kt`. Register in `KoinModule.kt`.

7. **Update ChannelsMvi.kt**: Remove `channels` field from `ChannelsState`.

8. **Update ChannelsViewModel.kt**: Replace `GetChannelsUseCase` with `GetPagedChannelsUseCase`, expose `pagedChannels: Flow<PagingData<ChannelEntity>>`, update `loadData()` to produce paged flow with `cachedIn(viewModelScope)`.

9. **Update ChannelsScreen.kt**: Use `collectAsLazyPagingItems()`, update `LazyVerticalGrid` to use paged items, handle load states from `LazyPagingItems.loadState`.

10. **Verify build**: Run `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` and `./gradlew :androidApp:assembleDebug` to confirm KMP compatibility.

## Testing Strategy

### What to test

- **ChannelPagingSource unit test**: Verify that `load()` returns correct pages with proper prevKey/nextKey, handles empty results, handles errors from the query executor.
- **GetPagedChannelsUseCase test**: Verify that the correct query/count executors are selected based on ChannelsFilter and query string.
- **Integration verification**: Load the demo playlist (~10k channels) and verify smooth scrolling in the Channels grid without memory spikes or UI jank.

### Edge cases

- Empty playlist (0 channels): PagingSource returns empty first page, UI shows empty state.
- Single page (< 50 channels): PagingSource returns one page with `nextKey = null`.
- Search with no results: PagingSource returns empty page, UI shows search empty state.
- Filter change while scrolling: `flatMapLatest` cancels the previous paged flow and starts a new one with the new filter.
- Database invalidation after favorite toggle: The PagingSource does not auto-invalidate (we rely on re-creation when filter/query changes via `flatMapLatest`). Favorite toggle updates the DB but the current page is not refreshed -- this is acceptable since toggling only changes the star icon state, not the channel list membership on the Channels screen.

### Key assertions

- First page loaded returns up to 50 items.
- Last page has `nextKey = null`.
- Page with offset beyond total count returns empty list.
- Count queries match the expected filter criteria.

## Doc Updates Required

- `docs/features/channel-browsing.md`: Add a note that the channel grid uses paged loading for performance with large playlists.
- `docs/constraints/current-limitations.md`: No change needed (paging was not listed as a limitation).
