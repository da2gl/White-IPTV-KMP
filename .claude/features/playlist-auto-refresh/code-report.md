# Code Report: Playlist Auto-Refresh

## Files Created
- `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/scheduler/PlaylistAutoRefreshScheduler.kt` — Core scheduler class with coroutine-based refresh loop, observes auto-update toggle via StateFlow

## Files Modified
- `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/PlaylistDao.kt` — Added `getPlaylistsList()` suspend function for one-shot playlist query
- `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/repository/PlaylistRepository.kt` — Added `getPlaylistsList()` to interface
- `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/repository/PlaylistRepositoryImpl.kt` — Implemented `getPlaylistsList()` delegating to DAO
- `composeApp/src/commonTest/kotlin/com/simplevideo/whiteiptv/data/repository/FakePlaylistRepository.kt` — Implemented `getPlaylistsList()` for test fake
- `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/SettingsPreferences.kt` — Added `autoUpdateEnabledFlow: StateFlow<Boolean>` for reactive toggle observation, updated `setAutoUpdateEnabled()` and `resetAll()` to emit to flow
- `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/AppLogger.kt` — Added `AUTO_REFRESH` log tag
- `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/di/KoinModule.kt` — Registered `PlaylistAutoRefreshScheduler` as `single` in `settingsModule`
- `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/App.kt` — Injected scheduler via `koinInject()`, starts in `LaunchedEffect(Unit)`
- `docs/features/settings.md` — Fixed `url-refresh` → `refresh` to match actual M3U parser attribute name
- `docs/domain/playlist.md` — Fixed `url-refresh` → `refresh` to match actual M3U parser attribute name

## Deviations from Plan
- The plan mentioned `scope.cancel()` in `stop()` but I used `stopRefreshLoop()` instead to only cancel the refresh job without killing the entire scope. This is safer since the scope also hosts the preference observer.

## Build Status
✅ Compiles successfully (`assembleDebug`)
✅ All unit tests pass (`testDebugUnitTest`)
✅ No detekt issues in changed files
✅ Formatting applied via `formatAll`

## Notes
- The scheduler uses `SupervisorJob` so a single failed playlist refresh doesn't cancel other refreshes
- Default refresh interval is 6 hours (21600s), minimum is 15 minutes (900s)
- Local file playlists (file:// URLs) are skipped
- The scheduler starts observing the toggle on app launch; if disabled, no refresh loop runs
- When toggle is flipped off, the refresh job is cancelled immediately
