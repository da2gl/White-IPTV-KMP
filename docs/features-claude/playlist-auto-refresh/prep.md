# Playlist Auto-Refresh — Implementation Plan

## Summary

Implement automatic background refresh for URL-based playlists when the "Auto Update Playlists" toggle is enabled in Settings. The feature runs a coroutine-based periodic scheduler inside a Koin singleton that observes the auto-update preference reactively. Each playlist refreshes on its own `refreshInterval` (parsed from M3U `refresh` attribute), falling back to a default 6-hour interval. Refresh errors are logged silently — no crashes, no user-facing notifications. The scheduler only runs while the app is in foreground (requirement from task definition). Local file playlists are skipped.

## Decisions Made

### 1. Architecture: Coroutine-based singleton scheduler vs WorkManager/platform job

- **Decision**: Use a pure-Kotlin coroutine-based singleton (`PlaylistAutoRefreshScheduler`) registered in Koin as `single` scope. No WorkManager, no platform-specific background job APIs.
- **Rationale**: The task explicitly requires foreground-only refresh. WorkManager is Android-only and not KMP-compatible. A coroutine in a `single` Koin bean is the simplest KMP-compatible approach that matches existing patterns (e.g., `CurrentPlaylistRepository` is a `single` with a `StateFlow`). The scheduler's CoroutineScope is tied to the app lifecycle.
- **Alternatives considered**: (1) WorkManager + iOS BGTaskScheduler via expect/actual — overkill for foreground-only, complex to implement. (2) ViewModel scope — destroyed on screen transitions, doesn't persist across tabs. (3) Composable `LaunchedEffect` in `App.kt` — couples business logic to UI layer, violates clean architecture.

### 2. Where to initialize the scheduler

- **Decision**: Inject `PlaylistAutoRefreshScheduler` into `App.kt` via `koinInject()` and call `scheduler.start()` inside a `LaunchedEffect(Unit)` block. The scheduler itself lives in `data/` layer and is stateless from the caller's perspective — calling `start()` begins observing the preference and scheduling jobs.
- **Rationale**: `App.kt` is the only composable guaranteed to live for the entire app session (it wraps `AppNavGraph`). The `LaunchedEffect(Unit)` runs once when the composable enters composition and cancels when it leaves (app backgrounded/killed). This naturally ties the scheduler lifecycle to the foreground lifecycle. The actual business logic stays in the scheduler class, not in `App.kt`.
- **Alternatives considered**: (1) Initialize in `KoinModule.kt` via `single { ... }.also { it.start() }` — Koin modules shouldn't have side effects. (2) Initialize in `SplashViewModel` — splash screen is transient, ViewModel gets destroyed.

### 3. Default refresh interval

- **Decision**: 6 hours (21600 seconds) when the M3U playlist doesn't specify a `refresh` attribute.
- **Rationale**: 6 hours is a reasonable default for IPTV playlists — frequent enough to catch channel URL changes but not so frequent as to waste bandwidth. Most IPTV providers update their playlists a few times per day. Common values seen in M3U playlists range from 3600 (1 hour) to 86400 (24 hours).
- **Alternatives considered**: 1 hour (too aggressive), 24 hours (too stale), configurable per-playlist (over-engineering for MVP).

### 4. Error handling strategy

- **Decision**: Silent logging with Kermit. Failed refreshes are logged at `error` level but do not surface to the user, do not crash, and do not retry immediately. The playlist will be retried on the next scheduled interval.
- **Rationale**: Background refreshes should be invisible to the user. Exponential backoff adds complexity with minimal benefit since intervals are already measured in hours. The `ImportPlaylistUseCase` already has comprehensive error logging.
- **Alternatives considered**: (1) Exponential backoff — unnecessary complexity for hour-scale intervals. (2) User notification — spec doesn't require it, and failed background refreshes are not critical.

### 5. How to read auto-update toggle reactively

- **Decision**: Add a `Flow<Boolean>` property (`autoUpdateEnabledFlow`) to `SettingsPreferences` that emits the current value and updates when the toggle changes. The scheduler observes this flow to start/stop refresh jobs.
- **Rationale**: The current `getAutoUpdateEnabled()` is a one-shot read. We need reactive observation so the scheduler starts immediately when enabled and stops immediately when disabled, without polling. `multiplatform-settings` supports `FlowSettings` for reactive observation, but adding that dependency may be overkill. Instead, we'll use a simple `MutableStateFlow<Boolean>` that `SettingsPreferences` updates when `setAutoUpdateEnabled()` is called, initialized with the current stored value.
- **Alternatives considered**: (1) `FlowSettings` coroutines extension — adds a new dependency for a single boolean. (2) Polling the preference every N seconds — wasteful and introduces latency. (3) Having `SettingsViewModel` call `scheduler.start()/stop()` directly — couples ViewModel to infrastructure.

### 6. Skip local file playlists

- **Decision**: Only refresh playlists whose URL starts with `http://` or `https://`. Playlists with `file://` prefix (local imports) are skipped.
- **Rationale**: Local file playlists have no remote source to refresh from. The `ImportPlaylistUseCase` already has a `downloadFromUrl()` method that validates URLs.

### 7. Minimum refresh interval floor

- **Decision**: Enforce a minimum interval of 15 minutes (900 seconds). If a playlist specifies a shorter interval, clamp it to 15 minutes.
- **Rationale**: Prevents abusive or misconfigured playlists from hammering the server. 15 minutes is generous enough for any legitimate IPTV use case.

### 8. One-shot playlist list query

- **Decision**: Add a `suspend fun getPlaylistsList(): List<PlaylistEntity>` query to `PlaylistDao` and `PlaylistRepository` to get all playlists as a one-shot call (vs the existing `Flow`-based `getPlaylists()`).
- **Rationale**: The refresh scheduler needs a snapshot of all playlists at refresh time, not a continuous observation. Using `Flow.first()` works but is semantically less clear and creates an unnecessary collector. A dedicated suspend function is cleaner and matches the existing pattern (e.g., `getPlaylistById()`, `getPlaylistByUrl()`).

## Current State

### What already exists:

| Component | File | What it does |
|---|---|---|
| Auto-update toggle | `data/local/SettingsPreferences.kt:31-37` | Read/write `auto_update_playlists` boolean |
| Toggle UI | `feature/settings/SettingsScreen.kt:186-189` | Switch component in Settings |
| Toggle event | `feature/settings/SettingsViewModel.kt:49-52` | Persists preference on toggle |
| Refresh interval field | `data/local/model/PlaylistEntity.kt:48` | `refreshInterval: Int?` in seconds |
| M3U parsing | `data/parser/playlist/M3uParser.kt:~65` | Extracts `refresh` from `#EXTM3U` header |
| Header model | `data/parser/playlist/model/PlaylistHeader.kt:34` | `refresh: Int?` |
| Mapper | `data/mapper/PlaylistMapper.kt:~25` | Maps `header.refresh` → `entity.refreshInterval` |
| Manual refresh | `feature/home/HomeViewModel.kt:190-205` | Re-downloads via `ImportPlaylistUseCase` |
| Import use case | `domain/usecase/ImportPlaylistUseCase.kt` | Downloads, parses, upserts playlist + channels |
| Playlist repo | `domain/repository/PlaylistRepository.kt` | CRUD + `updatePlaylistData()` |
| Playlist DAO | `data/local/PlaylistDao.kt` | Room queries, `getPlaylists()` as Flow |
| DI | `di/KoinModule.kt` | All Koin modules |
| App root | `App.kt` | Root composable with `AppNavGraph()` |
| Logger tags | `common/AppLogger.kt` | Centralized log tags |

### What does NOT exist:

- No scheduler/service for periodic refresh
- No reactive flow for auto-update preference
- No suspend `getPlaylistsList()` query
- No `PlaylistAutoRefreshScheduler` class
- No log tag for auto-refresh

## Changes Required

### New Files

#### 1. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/scheduler/PlaylistAutoRefreshScheduler.kt`

- **Purpose**: Core auto-refresh logic. Observes auto-update preference, schedules per-playlist refresh jobs.
- **Key contents**:

```kotlin
class PlaylistAutoRefreshScheduler(
    private val settingsPreferences: SettingsPreferences,
    private val playlistRepository: PlaylistRepository,
    private val importPlaylistUseCase: ImportPlaylistUseCase,
) {
    private val log = Logger.withTag(AppLogger.Tags.AUTO_REFRESH)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var refreshJob: Job? = null

    fun start() {
        scope.launch {
            settingsPreferences.autoUpdateEnabledFlow.collect { enabled ->
                if (enabled) {
                    startRefreshLoop()
                } else {
                    stopRefreshLoop()
                }
            }
        }
    }

    private fun startRefreshLoop() {
        stopRefreshLoop()
        refreshJob = scope.launch {
            while (isActive) {
                refreshAllPlaylists()
                delay(calculateNextDelay())
            }
        }
    }

    private fun stopRefreshLoop() {
        refreshJob?.cancel()
        refreshJob = null
    }

    private suspend fun refreshAllPlaylists() {
        val playlists = playlistRepository.getPlaylistsList()
        playlists
            .filter { it.url.startsWith("http://") || it.url.startsWith("https://") }
            .forEach { playlist ->
                runCatching {
                    importPlaylistUseCase(PlaylistSource.Url(playlist.url))
                }.onFailure { e ->
                    log.e(e) { "Failed to refresh playlist: ${playlist.name} (id=${playlist.id})" }
                }
            }
    }

    private suspend fun calculateNextDelay(): Long {
        val playlists = playlistRepository.getPlaylistsList()
            .filter { it.url.startsWith("http://") || it.url.startsWith("https://") }
        val minInterval = playlists
            .mapNotNull { it.refreshInterval }
            .minOrNull()
            ?.coerceAtLeast(MIN_REFRESH_INTERVAL_SECONDS)
            ?: DEFAULT_REFRESH_INTERVAL_SECONDS
        return minInterval.toLong() * 1000L
    }

    fun stop() {
        scope.cancel()
    }

    companion object {
        const val DEFAULT_REFRESH_INTERVAL_SECONDS = 21600  // 6 hours
        const val MIN_REFRESH_INTERVAL_SECONDS = 900        // 15 minutes
    }
}
```

**Design note**: Uses `SupervisorJob` so a single failed playlist refresh doesn't cancel the entire scope. The scheduler observes `autoUpdateEnabledFlow` and starts/stops accordingly. A simple `while(isActive) { refresh; delay }` loop is sufficient — no need for per-playlist independent timers in MVP since the minimum interval across all playlists determines the loop period.

### Modified Files

#### 2. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/SettingsPreferences.kt`

- **What changes**: Add a `MutableStateFlow<Boolean>` for reactive auto-update observation. Update `setAutoUpdateEnabled()` to also emit to the flow.
- **Why**: The scheduler needs to reactively observe the toggle state.
- **Specific changes**:
  - Add property: `val autoUpdateEnabledFlow: StateFlow<Boolean>` backed by `MutableStateFlow(getAutoUpdateEnabled())`
  - In `setAutoUpdateEnabled()`: also update `_autoUpdateEnabledFlow.value = enabled`
  - In `resetAll()`: also update `_autoUpdateEnabledFlow.value = false`

#### 3. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/PlaylistDao.kt`

- **What changes**: Add a suspend function to get all playlists as a one-shot list.
- **Why**: The refresh scheduler needs a snapshot, not a Flow.
- **Specific changes**:
  - Add query: `@Query("SELECT * FROM playlists") suspend fun getPlaylistsList(): List<PlaylistEntity>`

#### 4. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/repository/PlaylistRepository.kt`

- **What changes**: Add `suspend fun getPlaylistsList(): List<PlaylistEntity>` to the interface.
- **Why**: Expose the one-shot query through the repository layer.

#### 5. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/repository/PlaylistRepositoryImpl.kt`

- **What changes**: Implement `getPlaylistsList()` by delegating to `playlistDao.getPlaylistsList()`.
- **Why**: Standard repository implementation pattern.

#### 6. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/di/KoinModule.kt`

- **What changes**: Register `PlaylistAutoRefreshScheduler` as `single` in a new or existing module.
- **Why**: The scheduler must be a singleton that lives for the app's lifetime.
- **Specific changes**:
  - Add import for `PlaylistAutoRefreshScheduler`
  - Add to `settingsModule` (or create a new `schedulerModule`): `singleOf(::PlaylistAutoRefreshScheduler)`
  - Add to `appModules` list if new module created

#### 7. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/App.kt`

- **What changes**: Inject and start the scheduler.
- **Why**: `App.kt` is the foreground lifecycle anchor.
- **Specific changes**:
  - Add: `val autoRefreshScheduler: PlaylistAutoRefreshScheduler = koinInject()`
  - Add: `LaunchedEffect(Unit) { autoRefreshScheduler.start() }` inside the `App()` composable, before `AppTheme`.

#### 8. `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/common/AppLogger.kt`

- **What changes**: Add `AUTO_REFRESH` tag.
- **Why**: Consistent logging pattern.
- **Specific changes**:
  - Add: `const val AUTO_REFRESH = "$BASE_TAG:AutoRefresh"` inside `Tags` object.

#### 9. `composeApp/src/commonTest/kotlin/com/simplevideo/whiteiptv/data/repository/FakePlaylistRepository.kt`

- **What changes**: Implement the new `getPlaylistsList()` method.
- **Why**: Test fake must conform to updated interface.
- **Specific changes**:
  - Add: `override suspend fun getPlaylistsList(): List<PlaylistEntity> = playlists.values.toList()`

## Implementation Order

1. **Add DAO query** — `PlaylistDao.getPlaylistsList()` (one line)
2. **Update repository interface** — `PlaylistRepository.getPlaylistsList()` (one line)
3. **Update repository implementation** — `PlaylistRepositoryImpl.getPlaylistsList()` (one line)
4. **Update fake repository** — `FakePlaylistRepository.getPlaylistsList()` (one line)
5. **Update SettingsPreferences** — Add `autoUpdateEnabledFlow` StateFlow
6. **Add logger tag** — `AppLogger.Tags.AUTO_REFRESH`
7. **Create PlaylistAutoRefreshScheduler** — Core scheduling logic in `data/scheduler/`
8. **Register in Koin** — `singleOf(::PlaylistAutoRefreshScheduler)` in `KoinModule.kt`
9. **Wire in App.kt** — `koinInject()` + `LaunchedEffect(Unit)` to start scheduler
10. **Build and verify** — `./gradlew :composeApp:assembleDebug`

## Testing Strategy

### Unit Tests

**PlaylistAutoRefreshScheduler tests** (`commonTest/.../data/scheduler/PlaylistAutoRefreshSchedulerTest.kt`):

1. **Does not refresh when auto-update is disabled** — Set `autoUpdateEnabled = false`, verify no `ImportPlaylistUseCase` calls
2. **Refreshes all URL-based playlists when enabled** — Set `autoUpdateEnabled = true`, verify `ImportPlaylistUseCase` called for each URL playlist
3. **Skips local file playlists** — Include a `file://` playlist, verify it's not refreshed
4. **Handles refresh errors gracefully** — Make `ImportPlaylistUseCase` throw, verify no crash, other playlists still refresh
5. **Stops refreshing when toggle disabled** — Enable, then disable, verify refresh loop stops
6. **Uses default interval when playlist has no refreshInterval** — Verify 6-hour default
7. **Clamps minimum interval** — Set `refreshInterval = 10`, verify delay is at least 900 seconds

### Edge Cases

- No playlists in database (no-op)
- All playlists are local files (no-op)
- Network errors during refresh (logged, continues to next)
- Toggle rapidly on/off (debounced by `collect` — only latest state matters)
- `refreshInterval = null` on all playlists (uses default)

### Key Assertions

- `ImportPlaylistUseCase` is called with correct `PlaylistSource.Url`
- Scheduler coroutine is cancelled when disabled
- Scheduler coroutine is re-created when re-enabled
- No crashes from any exception type

## Doc Updates Required

1. **`docs/constraints/current-limitations.md`** — Remove the "Playlist auto-refresh not implemented" section
2. **`docs/features/settings.md`** — Update Implementation Notes for Auto Update to reflect it's now fully wired
