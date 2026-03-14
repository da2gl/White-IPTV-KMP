# Search Enhancement — Implementation Plan

## Summary

Add database-driven, context-aware channel search to Home, Channels, and Favorites screens. Search is an inline UI overlay within each screen (not a separate navigation route) — each screen gets `searchQuery`/`isSearchActive` state in its MVI, with a shared `SearchTopBar` component, and search queries delegated to Room DAO `LIKE` queries. The Favorites screen already has an in-memory search implementation that will be refactored to use the same database-driven approach.

## Decisions Made

### 1. Search scope: channel name only via DB LIKE query
- **Decision**: Search matches on `ChannelEntity.name` field only, using `LIKE '%' || :query || '%' COLLATE NOCASE` in Room DAO.
- **Rationale**: The spec explicitly requires database-driven search for 10k+ channel performance. Channel name is the primary user-facing identifier.
- **Alternatives considered**: In-memory filtering (current Favorites approach — too slow for large playlists), full-text search with FTS4/5 (overkill for substring match).

### 2. Inline search overlay, not a separate route
- **Decision**: Search is embedded in each screen's MVI state (`isSearchActive`, `searchQuery`). When active, the TopAppBar swaps to a `SearchTopBar`. No `Route.Search` is added.
- **Rationale**: The Favorites screen already implements this exact pattern. Each screen already has the context it needs (playlist selection, group selection, favorites filter) in its ViewModel. A separate route would require duplicating all context parameters as navigation arguments and would lose reactive state from the current ViewModel.
- **Alternatives considered**: Separate `Route.Search` with context parameters (over-engineered, loses reactive state), shared SearchViewModel (unnecessary indirection when each screen already manages its own context).

### 3. Extend existing UseCases with query parameter (no new SearchChannelsUseCase)
- **Decision**: Add `query: String = ""` parameter to `GetChannelsUseCase` and `GetFavoritesUseCase`. When query is empty, return all results (existing behavior). When non-empty, use search DAO methods.
- **Rationale**: The search scoping logic already lives in these use cases (ChannelsFilter, PlaylistSelection). A parallel SearchChannelsUseCase would duplicate filter logic. Adding a query parameter keeps the code DRY and follows the existing pattern. The default value `""` means existing callers don't need changes.
- **Alternatives considered**: Dedicated `SearchChannelsUseCase` (would duplicate ChannelsFilter/PlaylistSelection branching logic).

### 4. Extract SearchTopBar to shared component
- **Decision**: Move `SearchTopBar` and `SearchEmptyState` from `FavoritesScreen.kt` to `common/components/SearchComponents.kt` for reuse across Home, Channels, and Favorites screens.
- **Rationale**: All three screens need identical search UI (back arrow, text input, clear button, auto-focus). Placeholder text is parameterized.
- **Alternatives considered**: Duplicate in each screen (violates DRY).

### 5. No database index on name field
- **Decision**: Do not add an index on `ChannelEntity.name`. No database migration needed.
- **Rationale**: SQLite LIKE with leading wildcard (`%query%`) cannot use a B-tree index. Room's query execution on a background thread is sufficient for 10k channels.

### 6. Debounce 300ms for all screens
- **Decision**: All screens use 300ms debounce on the search query `MutableStateFlow`, consistent with the existing Favorites implementation.
- **Rationale**: Standard debounce for search-as-you-type. Avoids hammering the database on every keystroke.

### 7. Home screen search shows flat channel list when searching
- **Decision**: When search is active on Home, show a flat list of matching channels (same as Channels screen grid) instead of the category-based layout.
- **Rationale**: The spec says "Results update as the user types." For Home, showing categorized results during search would be confusing and technically complex. A flat grid of results is more useful for "find a specific channel."

## Current State

### Already implemented (Favorites)
- `FavoritesScreen.kt:147-189` — `SearchTopBar` composable (will be extracted to shared)
- `FavoritesScreen.kt:191-222` — `SearchEmptyState` composable (will be extracted to shared)
- `FavoritesScreen.kt:52-58` — Auto-focus with `FocusRequester`
- `FavoritesScreen.kt:60-79` — Conditional TopAppBar switching
- `FavoritesMvi.kt:11-12` — `searchQuery: String` and `isSearchActive: Boolean` in state
- `FavoritesMvi.kt:21-22` — `OnSearchQueryChanged` and `OnToggleSearch` events
- `FavoritesViewModel.kt:27` — `MutableStateFlow("")` for debounced search
- `FavoritesViewModel.kt:33` — `debounce(300)` on search query
- `FavoritesViewModel.kt:58-64` — In-memory `filterChannels()` (**to be replaced with DB query**)

### Not yet implemented
- `HomeScreen.kt:117` — Search icon `onClick = { /* TODO */ }`
- `ChannelsScreen.kt:83` — Search icon `onClick = { /* TODO: Search */ }`
- No search DAO queries in `PlaylistDao.kt`
- No search methods in `ChannelRepository` interface or implementation
- `GetChannelsUseCase` and `GetFavoritesUseCase` have no query parameter

## Changes Required

### New Files

#### 1. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/components/SearchComponents.kt`
- **Purpose**: Shared search UI components extracted from FavoritesScreen
- **Key contents**:
  ```kotlin
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun SearchTopBar(
      query: String,
      onQueryChange: (String) -> Unit,
      onClose: () -> Unit,
      placeholder: String = "Search channels...",
      focusRequester: FocusRequester,
  ) {
      TopAppBar(
          navigationIcon = {
              IconButton(onClick = onClose) {
                  Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close search")
              }
          },
          title = {
              TextField(
                  value = query,
                  onValueChange = onQueryChange,
                  placeholder = { Text(placeholder) },
                  singleLine = true,
                  colors = TextFieldDefaults.colors(
                      focusedContainerColor = Color.Transparent,
                      unfocusedContainerColor = Color.Transparent,
                      focusedIndicatorColor = Color.Transparent,
                      unfocusedIndicatorColor = Color.Transparent,
                  ),
                  modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
              )
          },
          actions = {
              if (query.isNotEmpty()) {
                  IconButton(onClick = { onQueryChange("") }) {
                      Icon(Icons.Default.Close, contentDescription = "Clear search")
                  }
              }
          },
      )
  }

  @Composable
  fun SearchEmptyState(query: String) {
      Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
      ) {
          Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center,
              modifier = Modifier.padding(16.dp),
          ) {
              Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = null,
                  modifier = Modifier.size(72.dp),
                  tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
              )
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                  text = "No results found",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.SemiBold,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                  text = "No channels match \"$query\"",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
              )
          }
      }
  }
  ```

### Modified Files

#### 2. `PlaylistDao.kt` — Add 5 search query methods
- **Path**: `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/PlaylistDao.kt`
- **What changes**: Add 5 new `@Query` methods after `getFavoriteChannelsByPlaylist` (line 69):
  ```kotlin
  @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%' COLLATE NOCASE ORDER BY name ASC")
  fun searchChannels(query: String): Flow<List<ChannelEntity>>

  @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%' COLLATE NOCASE AND playlistId = :playlistId ORDER BY name ASC")
  fun searchChannelsByPlaylistId(query: String, playlistId: Long): Flow<List<ChannelEntity>>

  @Query("""
      SELECT c.* FROM channels c
      INNER JOIN channel_group_cross_ref cgr ON c.id = cgr.channelId
      WHERE cgr.groupId = :groupId AND c.name LIKE '%' || :query || '%' COLLATE NOCASE
      ORDER BY c.name ASC
  """)
  fun searchChannelsByGroupId(query: String, groupId: Long): Flow<List<ChannelEntity>>

  @Query("SELECT * FROM channels WHERE isFavorite = 1 AND name LIKE '%' || :query || '%' COLLATE NOCASE ORDER BY name ASC")
  fun searchFavoriteChannels(query: String): Flow<List<ChannelEntity>>

  @Query("SELECT * FROM channels WHERE isFavorite = 1 AND playlistId = :playlistId AND name LIKE '%' || :query || '%' COLLATE NOCASE ORDER BY name ASC")
  fun searchFavoriteChannelsByPlaylist(query: String, playlistId: Long): Flow<List<ChannelEntity>>
  ```
- **Why**: Database-driven search per spec. Empty query `''` matches all — `LIKE '%%'` returns everything, which is the desired behavior for "empty query shows all channels in scope."

#### 3. `ChannelRepository.kt` — Add 5 search interface methods
- **Path**: `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/repository/ChannelRepository.kt`
- **What changes**: Add search section after Favorites (after line 24):
  ```kotlin
  // Search
  fun searchChannels(query: String): Flow<List<ChannelEntity>>
  fun searchChannelsByPlaylistId(query: String, playlistId: Long): Flow<List<ChannelEntity>>
  fun searchChannelsByGroupId(query: String, groupId: Long): Flow<List<ChannelEntity>>
  fun searchFavoriteChannels(query: String): Flow<List<ChannelEntity>>
  fun searchFavoriteChannelsByPlaylist(query: String, playlistId: Long): Flow<List<ChannelEntity>>
  ```
- **Why**: Repository interface must expose search methods for the domain layer.

#### 4. `ChannelRepositoryImpl.kt` — Implement 5 search methods
- **Path**: `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/repository/ChannelRepositoryImpl.kt`
- **What changes**: Add implementations after Favorites section (after line 49), delegating to PlaylistDao:
  ```kotlin
  // Search
  override fun searchChannels(query: String): Flow<List<ChannelEntity>> =
      playlistDao.searchChannels(query)
  override fun searchChannelsByPlaylistId(query: String, playlistId: Long): Flow<List<ChannelEntity>> =
      playlistDao.searchChannelsByPlaylistId(query, playlistId)
  override fun searchChannelsByGroupId(query: String, groupId: Long): Flow<List<ChannelEntity>> =
      playlistDao.searchChannelsByGroupId(query, groupId)
  override fun searchFavoriteChannels(query: String): Flow<List<ChannelEntity>> =
      playlistDao.searchFavoriteChannels(query)
  override fun searchFavoriteChannelsByPlaylist(query: String, playlistId: Long): Flow<List<ChannelEntity>> =
      playlistDao.searchFavoriteChannelsByPlaylist(query, playlistId)
  ```
- **Why**: Simple delegation, matching existing pattern.

#### 5. `GetChannelsUseCase.kt` — Add query parameter
- **Path**: `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/usecase/GetChannelsUseCase.kt`
- **What changes**: Add `query: String = ""` parameter to `invoke()`:
  ```kotlin
  operator fun invoke(
      filter: ChannelsFilter = ChannelsFilter.All,
      query: String = "",
  ): Flow<List<ChannelEntity>> {
      val trimmedQuery = query.trim()
      if (trimmedQuery.isEmpty()) {
          return when (filter) {
              is ChannelsFilter.All -> channelRepository.getAllChannels()
              is ChannelsFilter.ByPlaylist -> channelRepository.getChannelsByPlaylistId(filter.playlistId)
              is ChannelsFilter.ByGroup -> channelRepository.getChannelsByGroupId(filter.groupId)
          }
      }
      return when (filter) {
          is ChannelsFilter.All -> channelRepository.searchChannels(trimmedQuery)
          is ChannelsFilter.ByPlaylist -> channelRepository.searchChannelsByPlaylistId(trimmedQuery, filter.playlistId)
          is ChannelsFilter.ByGroup -> channelRepository.searchChannelsByGroupId(trimmedQuery, filter.groupId)
      }
  }
  ```
- **Why**: Extends existing filter logic with search. Default `""` preserves backward compatibility.

#### 6. `GetFavoritesUseCase.kt` — Add query parameter
- **Path**: `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/usecase/GetFavoritesUseCase.kt`
- **What changes**: Add `query: String = ""` parameter to `invoke()`:
  ```kotlin
  operator fun invoke(
      selection: PlaylistSelection = PlaylistSelection.All,
      query: String = "",
  ): Flow<List<ChannelEntity>> {
      val trimmedQuery = query.trim()
      if (trimmedQuery.isEmpty()) {
          return when (selection) {
              is PlaylistSelection.Selected -> channelRepository.getFavoriteChannelsByPlaylist(selection.id)
              is PlaylistSelection.All -> channelRepository.getFavoriteChannels()
          }
      }
      return when (selection) {
          is PlaylistSelection.Selected -> channelRepository.searchFavoriteChannelsByPlaylist(trimmedQuery, selection.id)
          is PlaylistSelection.All -> channelRepository.searchFavoriteChannels(trimmedQuery)
      }
  }
  ```
- **Why**: Replaces in-memory filtering in FavoritesViewModel with database queries.

#### 7. `HomeMvi.kt` — Add search state and events
- **Path**: `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/mvi/HomeMvi.kt`
- **What changes**:
  - Add to `HomeState`:
    ```kotlin
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val searchResults: List<ChannelEntity> = emptyList(),
    ```
  - Add to `HomeEvent`:
    ```kotlin
    data class OnSearchQueryChanged(val query: String) : HomeEvent
    data object OnToggleSearch : HomeEvent
    data class OnSearchResultClick(val channelId: Long) : HomeEvent
    ```
- **Why**: MVI pattern requires state and events for search functionality.

#### 8. `HomeViewModel.kt` — Add search logic
- **Path**: `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeViewModel.kt`
- **What changes**:
  - Add `GetChannelsUseCase` dependency to constructor
  - Add `private val searchQuery = MutableStateFlow("")`
  - Add a separate search flow combining `searchQuery.debounce(300)` with `currentPlaylistRepository.selection`:
    ```kotlin
    combine(
        searchQuery.debounce(300),
        currentPlaylistRepository.selection,
    ) { query, selection ->
        query to selection
    }.flatMapLatest { (query, selection) ->
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            flowOf(emptyList())
        } else {
            val filter = when (selection) {
                is PlaylistSelection.Selected -> ChannelsFilter.ByPlaylist(selection.id)
                is PlaylistSelection.All -> ChannelsFilter.All
            }
            getChannels(filter, trimmedQuery)
        }
    }.onEach { results ->
        viewState = viewState.copy(searchResults = results)
    }.launchIn(viewModelScope)
    ```
  - Handle new events in `obtainEvent()`:
    ```kotlin
    is HomeEvent.OnSearchQueryChanged -> {
        searchQuery.value = viewEvent.query
        viewState = viewState.copy(searchQuery = viewEvent.query)
    }
    is HomeEvent.OnToggleSearch -> {
        val newIsActive = !viewState.isSearchActive
        if (!newIsActive) searchQuery.value = ""
        viewState = viewState.copy(
            isSearchActive = newIsActive,
            searchQuery = if (!newIsActive) "" else viewState.searchQuery,
            searchResults = if (!newIsActive) emptyList() else viewState.searchResults,
        )
    }
    is HomeEvent.OnSearchResultClick -> {
        viewAction = HomeAction.NavigateToPlayer(viewEvent.channelId)
    }
    ```
- **Why**: Search must be contextual (respects selected playlist). Separate search flow avoids disrupting the main data flow.

#### 9. `HomeScreen.kt` — Add search UI
- **Path**: `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeScreen.kt`
- **What changes**:
  - Import `SearchTopBar`, `SearchEmptyState` from `common.components`
  - Add `FocusRequester` and auto-focus `LaunchedEffect` (same as Favorites)
  - In `Scaffold.topBar`: when `state.isSearchActive`, show `SearchTopBar(placeholder = "Search channels...")`; otherwise show current `HomeTopAppBar`
  - Wire search icon in `HomeTopAppBar` (line 117): `onClick = { viewModel.obtainEvent(HomeEvent.OnToggleSearch) }`
  - Add `onSearchClick` parameter to `HomeTopAppBar` composable
  - In content area: when `state.isSearchActive`, show search results as a simple `LazyColumn` of channels (or `SearchEmptyState` if empty with query); otherwise show current `HomeContent`
  - Handle `HomeAction.NavigateToPlayer` for search result clicks (already handled via existing action)
- **Why**: Spec requires search icon to open full-screen search overlay with results.

#### 10. `ChannelsMvi.kt` — Add search state and events
- **Path**: `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/mvi/ChannelsMvi.kt`
- **What changes**:
  - Add to `ChannelsState`:
    ```kotlin
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    ```
  - Add to `ChannelsEvent`:
    ```kotlin
    data class OnSearchQueryChanged(val query: String) : ChannelsEvent
    data object OnToggleSearch : ChannelsEvent
    ```
- **Why**: MVI state for search. Note: Channels screen already shows a flat channel list, so no separate `searchResults` needed — the existing `channels` list will contain search results when search is active because the query is passed to `getChannels()`.

#### 11. `ChannelsViewModel.kt` — Add search logic
- **Path**: `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/ChannelsViewModel.kt`
- **What changes**:
  - Add `private val searchQuery = MutableStateFlow("")`
  - In `loadData()`, add `searchQuery.debounce(300)` to the initial `combine`:
    ```kotlin
    combine(
        getPlaylists(),
        currentPlaylistRepository.selection,
        selectedGroupIdFlow,
        searchQuery.debounce(300),  // NEW
    ) { playlists, selection, selectedGroupId, query ->
        // ... existing logic plus query
    }
    ```
  - Pass query to `getChannels(filter, query)` call (currently line 80)
  - Update state emission to include `searchQuery`:
    ```kotlin
    viewState = ChannelsState(
        channels = channels,
        // ... existing fields ...
        searchQuery = query,  // NEW
    )
    ```
  - Handle new events in `obtainEvent()`:
    ```kotlin
    is ChannelsEvent.OnSearchQueryChanged -> {
        searchQuery.value = viewEvent.query
        viewState = viewState.copy(searchQuery = viewEvent.query)
    }
    is ChannelsEvent.OnToggleSearch -> {
        val newIsActive = !viewState.isSearchActive
        if (!newIsActive) searchQuery.value = ""
        viewState = viewState.copy(
            isSearchActive = newIsActive,
            searchQuery = if (!newIsActive) "" else viewState.searchQuery,
        )
    }
    ```
- **Why**: Search integrates into the existing reactive data flow. The query is another filter dimension alongside playlist and group selection.

#### 12. `ChannelsScreen.kt` — Add search UI
- **Path**: `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/channels/ChannelsScreen.kt`
- **What changes**:
  - Import `SearchTopBar`, `SearchEmptyState` from `common.components`
  - Add `FocusRequester` and auto-focus `LaunchedEffect`
  - In `Scaffold.topBar`: when `state.isSearchActive`, show `SearchTopBar(placeholder = "Search channels...")`; otherwise show current `ChannelsTopAppBar`
  - Wire search icon (line 83) to: `onClick = { viewModel.obtainEvent(ChannelsEvent.OnToggleSearch) }`
  - Pass `onSearchClick` callback to `ChannelsTopAppBar`
  - In `ChannelsContent`: when `state.isSearchActive && state.channels.isEmpty() && state.searchQuery.isNotEmpty()`, show `SearchEmptyState(state.searchQuery)` instead of the generic "No channels found" text
- **Why**: Same search overlay pattern as Favorites.

#### 13. `FavoritesViewModel.kt` — Refactor to DB search
- **Path**: `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/favorites/FavoritesViewModel.kt`
- **What changes**:
  - Remove `filterChannels()` private method (lines 58-64)
  - In the `flatMapLatest` block, pass `query` to `getFavorites(selection, query)` instead of calling `filterChannels(channels, query)`:
    ```kotlin
    // Before:
    combine(getPlaylists(), getFavorites(selection)) { playlists, channels ->
        val filteredChannels = filterChannels(channels, query)
        // ...
    }
    // After:
    combine(getPlaylists(), getFavorites(selection, query)) { playlists, channels ->
        viewState.copy(
            playlists = playlists,
            channels = channels,  // already filtered by DB
            // ...
        )
    }
    ```
- **Why**: Replaces in-memory filtering with database-driven search per spec. No other changes needed — the existing `searchQuery` MutableStateFlow, debounce, and MVI events remain the same.

#### 14. `FavoritesScreen.kt` — Use shared search components
- **Path**: `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/favorites/FavoritesScreen.kt`
- **What changes**:
  - Remove private `SearchTopBar` composable (lines 147-189)
  - Remove private `SearchEmptyState` composable (lines 191-222)
  - Import `SearchTopBar` and `SearchEmptyState` from `com.simplevideo.whiteiptv.common.components`
  - Update `SearchTopBar` call to pass `placeholder = "Search favorites..."`
  - Update `SearchEmptyState` reference in content area (unchanged — signature matches)
- **Why**: DRY — shared components reused across all three screens. The Favorites screen continues to work identically but uses the shared components.

### Database Changes
- **No migration needed**: Only adding new `@Query` methods to the DAO. No schema changes, no new entities, no new tables.

### DI Changes
- **No changes to `KoinModule.kt`**: No new UseCase, ViewModel, or Repository registrations needed. The existing registrations for `GetChannelsUseCase`, `GetFavoritesUseCase`, `HomeViewModel`, `ChannelsViewModel`, and `FavoritesViewModel` remain unchanged. The only constructor change is adding `GetChannelsUseCase` to `HomeViewModel`, which Koin resolves automatically via `viewModelOf(::HomeViewModel)`.

## Implementation Order

Execute in this order to maintain compilability at each step:

1. **Data layer — DAO search queries** (`PlaylistDao.kt`)
   Add 5 search query methods. No dependencies.

2. **Data layer — Repository interface** (`ChannelRepository.kt`)
   Add 5 search method declarations.

3. **Data layer — Repository implementation** (`ChannelRepositoryImpl.kt`)
   Implement 5 search methods delegating to DAO.

4. **Domain layer — GetChannelsUseCase** (`GetChannelsUseCase.kt`)
   Add `query` parameter with empty-string default.

5. **Domain layer — GetFavoritesUseCase** (`GetFavoritesUseCase.kt`)
   Add `query` parameter with empty-string default.

6. **UI layer — Extract shared components** (`SearchComponents.kt` — NEW FILE)
   Extract `SearchTopBar` and `SearchEmptyState` from FavoritesScreen.

7. **Favorites — Refactor to DB search** (`FavoritesViewModel.kt`, `FavoritesScreen.kt`)
   Replace in-memory filtering with DB queries, use shared components.

8. **Channels — Add search** (`ChannelsMvi.kt`, `ChannelsViewModel.kt`, `ChannelsScreen.kt`)
   Add MVI state/events, integrate search into existing flow, add UI.

9. **Home — Add search** (`HomeMvi.kt`, `HomeViewModel.kt`, `HomeScreen.kt`)
   Add MVI state/events, add search flow, add UI with flat results layout.

10. **Build verification** — `./gradlew :composeApp:assembleDebug`

## Testing Strategy

### Unit tests for UseCase query routing
- `GetChannelsUseCase` with empty query calls `getAllChannels()` / `getChannelsByPlaylistId()` / `getChannelsByGroupId()`
- `GetChannelsUseCase` with non-empty query calls `searchChannels()` / `searchChannelsByPlaylistId()` / `searchChannelsByGroupId()`
- `GetFavoritesUseCase` with empty query calls `getFavoriteChannels()` / `getFavoriteChannelsByPlaylist()`
- `GetFavoritesUseCase` with non-empty query calls `searchFavoriteChannels()` / `searchFavoriteChannelsByPlaylist()`
- Query trimming: whitespace-only query treated as empty

### Edge cases
- Empty search query returns all channels in scope (not empty list)
- Very long search query doesn't crash (SQL injection safe via Room parameter binding)
- Search query with special characters (`%`, `_`, `'`) works correctly (Room handles parameterized queries)
- Toggle search off clears query and shows unfiltered results
- Switching playlist/group while search is active re-runs search in new scope

### Manual testing
- Verify 300ms debounce is perceptible (type quickly, results shouldn't flash)
- Verify search is case-insensitive ("CNN" matches "cnn", "Cnn")
- Verify back arrow dismisses search and restores previous view
- Verify auto-focus on search activation (keyboard appears)
- Verify performance with large playlist (10k+ channels from demo playlist)

## Doc Updates Required

1. **`docs/features/search.md`** — Already accurate. The spec matches the implementation plan. No changes needed.
2. **`docs/constraints/open-questions.md`** — No search-related open questions exist. No changes needed.
3. **`docs/constraints/current-limitations.md`** — Remove "Search not implemented" section (lines 31-35) after implementation is complete. **Do not remove now** — remove only after code is merged.
