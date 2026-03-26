# Code Report: Home Performance Optimization

## Files Created
None.

## Files Modified
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/mvi/HomeMvi.kt` -- Removed `playlists`, `continueWatchingItems`, `favoriteChannels`, `categories` from HomeState. State now contains only UI flags (selection, search, loading, dialogs, errors).
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeViewModel.kt` -- Replaced single `combine` block with four independent `StateFlow` properties (`playlists`, `continueWatchingItems`, `favoriteChannels`, `categories`) using `stateIn(WhileSubscribed(5_000L))`. Selection-dependent flows use `flatMapLatest`. `getSelectedPlaylist()` now reads from `playlists.value`.
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/home/HomeScreen.kt` -- HomeScreen now collects from four separate ViewModel StateFlows via `collectAsState()`. HomeTopAppBar parameter type changed from `List<PlaylistEntity>` to `ImmutableList<PlaylistEntity>`.
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/PlaylistDao.kt` -- Added `getTopValidGroups(limit)` and `getTopValidGroupsByPlaylist(playlistId, limit)` queries that filter out invalid group names (empty, 'undefined', 'unknown', 'other') at the SQL level.
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/repository/ChannelRepository.kt` -- Added `getTopValidGroups` and `getTopValidGroupsByPlaylist` interface methods.
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/repository/ChannelRepositoryImpl.kt` -- Implemented new valid-group methods delegating to DAO. Changed `getRandomChannelsForGroups` to use per-group queries instead of bulk fetch + Kotlin shuffle.
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/usecase/GetHomeCategoriesUseCase.kt` -- Switched from `getTopGroups`/`getTopGroupsByPlaylist` + Kotlin filtering to `getTopValidGroups`/`getTopValidGroupsByPlaylist` (SQL-level filtering). Removed `invalidNames` set.
- `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/feature/home/HomeViewModelTest.kt` -- Updated `init loads playlists` test to read from `viewModel.playlists.value`. Added `backgroundScope.launch { viewModel.playlists.collect {} }` in tests that need `getSelectedPlaylist()` to work (since `WhileSubscribed` requires active subscribers).
- `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/data/repository/StubChannelRepository.kt` -- Added stubs for `getTopValidGroups` and `getTopValidGroupsByPlaylist`.
- `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/data/local/FakePlaylistDao.kt` -- Added stubs for `getTopValidGroups` and `getTopValidGroupsByPlaylist`.
- `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/data/repository/FakeChannelRepository.kt` -- Added implementations for `getTopValidGroups` and `getTopValidGroupsByPlaylist` with filtering logic matching SQL behavior.

## Deviations from Plan
- Tests required `backgroundScope.launch { viewModel.playlists.collect {} }` to activate `WhileSubscribed` StateFlows. Without a subscriber, `stateIn(WhileSubscribed)` never starts collecting, so `playlists.value` stays empty and `getSelectedPlaylist()` returns null. This is expected behavior -- in the real app, Compose's `collectAsState()` acts as the subscriber.

## Build Status
Compiles and tests pass (327 tests, 0 failures).

## Notes
- The `PlaylistDropdown` composable parameter type in `HomeTopAppBar` was changed from `List<PlaylistEntity>` to `ImmutableList<PlaylistEntity>` for consistency with the new flow types.
- `getRandomChannelsForGroups` now makes N individual queries (one per group) instead of one bulk query. This is simpler and avoids loading all channels for all groups into memory at once, though it does make more DB roundtrips. For the typical case of 5 groups, this is negligible.
