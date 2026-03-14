# Background Playlist Refresh -- Implementation Plan

## Summary

Migrate the existing `PlaylistAutoRefreshScheduler` from a foreground-only coroutine loop (tied to `App.kt` composition) to true background execution using platform-native schedulers: WorkManager on Android and BGTaskScheduler on iOS. A new `BackgroundScheduler` expect/actual interface in `shared/src/commonMain/platform/` will abstract the scheduling. The Android `actual` implementation uses `PeriodicWorkRequest` with a `CoroutineWorker`. The iOS `actual` uses `BGAppRefreshTaskRequest` via Kotlin/Native interop. The existing coroutine loop in `PlaylistAutoRefreshScheduler` will be removed and replaced with a thin coordinator that delegates scheduling to `BackgroundScheduler` and contains the shared refresh logic. The `SettingsViewModel` will trigger schedule/cancel when the auto-update toggle changes. WorkManager initialization happens in the `androidApp` module's `WhiteIPTVApplication`.

## Decisions Made

### 1. Where to place the CoroutineWorker -- shared module vs androidApp module

- **Decision**: Place `PlaylistRefreshWorker` (the `CoroutineWorker` subclass) in `shared/src/androidMain/platform/` alongside other Android actual implementations. The worker needs access to Koin dependencies (`PlaylistRepository`, `ImportPlaylistUseCase`, `SettingsPreferences`) which all live in the shared module.
- **Rationale**: The worker is a platform-specific implementation detail, analogous to `ExoPlayerFactory` or `AndroidFileReader`. It belongs in the same source set that has access to both Android APIs and shared domain code. The `androidApp` module only needs to call `WorkManager.initialize()` and does not need to know about the worker class directly.
- **Alternatives considered**: (1) Place worker in `androidApp` -- would require duplicating or exposing refresh logic from shared module. (2) Place refresh logic in a UseCase called from androidApp -- adds unnecessary indirection since the logic already exists in shared.

### 2. WorkManager initialization approach

- **Decision**: Use the default `WorkManager.getInstance(context)` approach. Do not use custom `WorkManager.Configuration` or `WorkManagerInitializer`. WorkManager auto-initializes via its `ContentProvider` on Android.
- **Rationale**: The project has no custom WorkManager configuration needs. Auto-initialization is the simplest approach and avoids boilerplate. The `PeriodicWorkRequest` is enqueued from the `BackgroundScheduler` actual implementation when the toggle is enabled.
- **Alternatives considered**: Custom `Configuration.Builder` with Koin-based `WorkerFactory` -- needed only if workers require constructor injection. Since we can use `KoinComponent` or `get()` from the global Koin context inside `doWork()`, custom factory is unnecessary.

### 3. How the worker obtains Koin dependencies

- **Decision**: Use `org.koin.core.component.KoinComponent` interface on the worker, then call `get<T>()` to resolve dependencies inside `doWork()`. This avoids the need for a custom `WorkerFactory`.
- **Rationale**: Koin is initialized in `WhiteIPTVApplication.onCreate()` which runs before any WorkManager work. The global Koin context is available. This is the standard Koin + WorkManager integration pattern and avoids the complexity of a `DelegatingWorkerFactory`.
- **Alternatives considered**: (1) Custom `WorkerFactory` with constructor injection -- more correct architecturally but requires `Configuration.Builder`, disabling auto-init, and significantly more boilerplate for a single worker. (2) Passing data via `WorkData` -- not possible for complex dependencies.

### 4. WorkManager minimum interval vs user-configured interval

- **Decision**: Use the maximum of WorkManager's minimum (15 minutes) and the playlist's `refreshInterval`. The `PeriodicWorkRequest` interval will be set to the minimum `refreshInterval` across all URL-based playlists, clamped to at least 15 minutes. If no playlists exist or none have a `refreshInterval`, use the default 6-hour interval.
- **Rationale**: WorkManager enforces a 15-minute minimum for `PeriodicWorkRequest`. The existing scheduler already clamps to 15 minutes (`MIN_REFRESH_INTERVAL_SECONDS = 900`). This is a natural fit. The interval is computed at schedule time; if playlists change, the work will be re-enqueued with the updated interval.
- **Alternatives considered**: Always use 6 hours regardless of playlist config -- simpler but ignores the M3U `refresh` attribute that the project already parses and stores.

### 5. iOS BGTaskScheduler implementation scope

- **Decision**: Implement a minimal iOS background refresh using `BGAppRefreshTaskRequest`. The task will be registered in the Kotlin `IOSBackgroundScheduler` and submitted via `BGTaskScheduler.shared.submitTaskRequest`. The iOS app's `Info.plist` needs a `BGTaskSchedulerPermittedIdentifiers` entry and the `iOSApp.swift` needs to register the task handler on launch.
- **Rationale**: `BGAppRefreshTaskRequest` is the correct API for periodic data refresh on iOS. It provides system-managed scheduling with battery and network awareness. The alternative `BGProcessingTaskRequest` is for long-running tasks (>30 seconds) which is unnecessary here since playlist refresh is a quick network call + parse.
- **Alternatives considered**: (1) `BGProcessingTaskRequest` -- overkill, meant for maintenance tasks. (2) Silent push notifications -- requires server infrastructure. (3) Skip iOS background refresh entirely -- leaves iOS users with foreground-only behavior, defeating the purpose.

### 6. How to handle the re-scheduling when interval changes

- **Decision**: When the auto-update toggle is enabled, compute the interval and enqueue a unique periodic work (Android) or submit a task request (iOS). Use `ExistingPeriodicWorkPolicy.UPDATE` on Android to replace any existing work with the new interval. On iOS, cancel and resubmit. When playlists are imported/deleted (which could change the minimum interval), do NOT automatically re-schedule -- the current interval persists until the next toggle or app restart.
- **Rationale**: Simplicity. Dynamically re-scheduling on every playlist change adds complexity. The interval only matters at the granularity of hours, so slight staleness is acceptable. The user can toggle off/on to force a re-schedule if needed.
- **Alternatives considered**: Observing playlist changes and re-scheduling -- too complex for the benefit, could cause excessive WorkManager churn.

### 7. Removing vs preserving the foreground coroutine loop

- **Decision**: Remove the foreground coroutine loop entirely. The `PlaylistAutoRefreshScheduler` class will be refactored into a `BackgroundRefreshCoordinator` that contains only the `refreshAllPlaylists()` logic (shared between platforms) and the `calculateIntervalSeconds()` helper. The scheduling responsibility moves to `BackgroundScheduler`.
- **Rationale**: Keeping both foreground and background schedulers creates confusion about which one is active. Background schedulers (WorkManager, BGTaskScheduler) handle all cases including foreground. WorkManager executes work even when the app is in the foreground.
- **Alternatives considered**: Keep both and use foreground loop as a supplement -- redundant and could cause double-refreshes.

### 8. Network constraint

- **Decision**: Require network connectivity as a constraint for the background work. On Android, use `Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)`. On iOS, BGTaskScheduler handles this implicitly (system won't schedule if offline).
- **Rationale**: Playlist refresh requires downloading M3U files from URLs. Running without network would always fail. Adding the constraint lets the OS defer work until connectivity is available.

### 9. Settings toggle integration

- **Decision**: When `SettingsViewModel` handles `OnAutoUpdateChanged`, it will also call `BackgroundScheduler.schedule()` or `BackgroundScheduler.cancel()` in addition to persisting the preference. On app startup (`App.kt`), check the persisted preference and schedule if enabled.
- **Rationale**: The toggle must immediately take effect. Relying solely on the persisted preference checked at startup would mean toggling on doesn't schedule until next app launch. The startup check handles the case where the app was killed and restarted with auto-update already enabled.
- **Alternatives considered**: (1) Keep the reactive `autoUpdateEnabledFlow.collect` pattern -- works for foreground but background schedulers need imperative schedule/cancel calls. (2) Only schedule on startup -- toggle would not take effect immediately.

## Current State

### Files that exist and will be modified or removed:

| File | Path | Relevance |
|---|---|---|
| PlaylistAutoRefreshScheduler | `shared/src/commonMain/.../data/scheduler/PlaylistAutoRefreshScheduler.kt` | Will be refactored into `BackgroundRefreshCoordinator` (rename + simplify) |
| App.kt | `shared/src/commonMain/.../App.kt` (lines 20-24) | Remove `PlaylistAutoRefreshScheduler` injection and `LaunchedEffect`. Add `BackgroundScheduler` startup check |
| KoinModule.kt | `shared/src/commonMain/.../di/KoinModule.kt` (lines 117-124) | Update scheduler registration |
| SettingsPreferences | `shared/src/commonMain/.../data/local/SettingsPreferences.kt` | No changes needed (already has `autoUpdateEnabledFlow`) |
| SettingsViewModel | `shared/src/commonMain/.../feature/settings/SettingsViewModel.kt` (lines 49-52) | Add `BackgroundScheduler` call on toggle |
| WhiteIPTVApplication | `androidApp/src/main/.../WhiteIPTVApplication.kt` | No changes needed (WorkManager auto-initializes) |
| Android PlatformModule | `shared/src/androidMain/.../di/PlatformModule.kt` | Register `AndroidBackgroundScheduler` |
| iOS PlatformModule | `shared/src/iosMain/.../di/PlatformModule.kt` | Register `IOSBackgroundScheduler` |
| AndroidManifest.xml | `androidApp/src/main/AndroidManifest.xml` | No changes needed (WorkManager auto-adds permissions) |
| libs.versions.toml | `gradle/libs.versions.toml` | Add `work-runtime-ktx` dependency |
| shared/build.gradle.kts | `shared/build.gradle.kts` (line 41) | Add work-runtime to androidMain dependencies |
| PlaylistAutoRefreshSchedulerTest | `shared/src/commonTest/.../data/scheduler/PlaylistAutoRefreshSchedulerTest.kt` | Currently commented out. Will be rewritten for `BackgroundRefreshCoordinator` |
| Settings feature doc | `docs/features/settings.md` (line 55) | Update implementation notes |
| iOSApp.swift | `iosApp/iosApp/iOSApp.swift` | Register BGTask handler |
| Info.plist | `iosApp/iosApp/Info.plist` | Add BGTaskSchedulerPermittedIdentifiers |

### Files that exist and need no changes:

| File | Why no change |
|---|---|
| PlaylistRepository interface | Already has `getPlaylistsList()` |
| PlaylistDao | Already has the needed queries |
| ImportPlaylistUseCase | Called by coordinator, no interface change |
| SettingsPreferences | Already has `autoUpdateEnabledFlow` |
| AppLogger | Already has `AUTO_REFRESH` tag |

## Changes Required

### New Files

#### 1. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/platform/BackgroundScheduler.kt`

- **Purpose**: Expect declaration for platform background scheduling
- **Key contents**:

```kotlin
package com.simplevideo.whiteiptv.platform

/**
 * Platform-specific background task scheduler.
 *
 * Android: WorkManager PeriodicWorkRequest
 * iOS: BGTaskScheduler with BGAppRefreshTaskRequest
 */
interface BackgroundScheduler {
    /**
     * Schedule periodic playlist refresh at the given interval.
     * Replaces any existing schedule. Requires network connectivity.
     *
     * @param intervalSeconds refresh interval in seconds (minimum 900 = 15 min)
     */
    fun schedule(intervalSeconds: Long)

    /** Cancel any scheduled periodic refresh. */
    fun cancel()

    /** Returns true if a periodic refresh is currently scheduled. */
    fun isScheduled(): Boolean
}
```

#### 2. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/scheduler/BackgroundRefreshCoordinator.kt`

- **Purpose**: Shared refresh logic extracted from `PlaylistAutoRefreshScheduler`. Contains `refreshAllPlaylists()` and interval calculation. Used by both Android worker and iOS task handler.
- **Key contents**:

```kotlin
package com.simplevideo.whiteiptv.data.scheduler

/**
 * Coordinates playlist refresh operations for background execution.
 *
 * Contains the shared business logic for refreshing URL-based playlists.
 * Called by platform-specific background workers (WorkManager on Android,
 * BGTaskScheduler on iOS).
 */
class BackgroundRefreshCoordinator(
    private val playlistRepository: PlaylistRepository,
    private val refreshPlaylist: suspend (PlaylistSource) -> Unit,
) {
    private val log = Logger.withTag(AppLogger.Tags.AUTO_REFRESH)

    /**
     * Refresh all URL-based playlists. Local file playlists are skipped.
     * Errors for individual playlists are logged but do not halt the process.
     */
    suspend fun refreshAllPlaylists() { /* same logic as current scheduler */ }

    /**
     * Calculate the optimal refresh interval based on playlist configurations.
     * Returns the minimum refreshInterval across all URL playlists, clamped
     * to at least [MIN_REFRESH_INTERVAL_SECONDS], defaulting to [DEFAULT_REFRESH_INTERVAL_SECONDS].
     */
    suspend fun calculateIntervalSeconds(): Long { /* same logic */ }

    companion object {
        const val DEFAULT_REFRESH_INTERVAL_SECONDS = 21600L // 6 hours
        const val MIN_REFRESH_INTERVAL_SECONDS = 900L // 15 minutes
    }
}
```

#### 3. `shared/src/androidMain/kotlin/com/simplevideo/whiteiptv/platform/AndroidBackgroundScheduler.kt`

- **Purpose**: Android actual implementation using WorkManager
- **Key contents**:

```kotlin
package com.simplevideo.whiteiptv.platform

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Android [BackgroundScheduler] implementation using WorkManager.
 *
 * Enqueues a [PlaylistRefreshWorker] as a unique periodic work request.
 * WorkManager handles Doze mode, battery optimization, and persistence
 * across device reboots.
 */
class AndroidBackgroundScheduler(
    private val context: Context,
) : BackgroundScheduler {

    override fun schedule(intervalSeconds: Long) {
        val intervalMinutes = (intervalSeconds / 60).coerceAtLeast(15)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<PlaylistRefreshWorker>(
            intervalMinutes, TimeUnit.MINUTES,
        ).setConstraints(constraints).build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
    }

    override fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    override fun isScheduled(): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(WORK_NAME)
            .get()
        return workInfos.any { !it.state.isFinished }
    }

    companion object {
        const val WORK_NAME = "playlist_auto_refresh"
    }
}
```

#### 4. `shared/src/androidMain/kotlin/com/simplevideo/whiteiptv/platform/PlaylistRefreshWorker.kt`

- **Purpose**: CoroutineWorker that performs the actual playlist refresh in background
- **Key contents**:

```kotlin
package com.simplevideo.whiteiptv.platform

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.simplevideo.whiteiptv.data.scheduler.BackgroundRefreshCoordinator
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * WorkManager worker that refreshes all URL-based playlists in the background.
 *
 * Resolves [BackgroundRefreshCoordinator] from Koin to perform the refresh.
 * Network connectivity is guaranteed by the work constraints.
 */
class PlaylistRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {

    private val coordinator: BackgroundRefreshCoordinator by inject()

    override suspend fun doWork(): Result {
        return runCatching {
            coordinator.refreshAllPlaylists()
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }
}
```

#### 5. `shared/src/iosMain/kotlin/com/simplevideo/whiteiptv/platform/IOSBackgroundScheduler.kt`

- **Purpose**: iOS actual implementation using BGTaskScheduler
- **Key contents**:

```kotlin
package com.simplevideo.whiteiptv.platform

import co.touchlab.kermit.Logger
import com.simplevideo.whiteiptv.common.AppLogger
import com.simplevideo.whiteiptv.data.scheduler.BackgroundRefreshCoordinator
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTaskScheduler

/**
 * iOS [BackgroundScheduler] implementation using BGTaskScheduler.
 *
 * Submits [BGAppRefreshTaskRequest] for periodic playlist refresh.
 * The system determines the actual execution timing based on app usage
 * patterns, battery level, and network availability.
 *
 * Important: The BGTask handler must be registered in iOSApp.swift on launch,
 * and the task identifier must be declared in Info.plist under
 * BGTaskSchedulerPermittedIdentifiers.
 */
class IOSBackgroundScheduler(
    private val coordinator: BackgroundRefreshCoordinator,
) : BackgroundScheduler {

    private val log = Logger.withTag(AppLogger.Tags.AUTO_REFRESH)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var scheduled = false

    override fun schedule(intervalSeconds: Long) {
        submitTaskRequest(intervalSeconds)
        scheduled = true
    }

    override fun cancel() {
        BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(TASK_IDENTIFIER)
        scheduled = false
    }

    override fun isScheduled(): Boolean = scheduled

    /**
     * Called by the BGTask handler registered in iOSApp.swift.
     * Performs the refresh and resubmits the task for the next cycle.
     */
    fun handleBackgroundTask(task: platform.BackgroundTasks.BGTask) {
        scope.launch {
            runCatching {
                coordinator.refreshAllPlaylists()
            }.onFailure { e ->
                log.e(e) { "Background refresh failed" }
            }
            task.setTaskCompletedWithSuccess(true)
        }
        // Resubmit for next execution
        val interval = kotlinx.coroutines.runBlocking {
            coordinator.calculateIntervalSeconds()
        }
        submitTaskRequest(interval)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun submitTaskRequest(intervalSeconds: Long) {
        val request = BGAppRefreshTaskRequest(identifier = TASK_IDENTIFIER)
        request.earliestBeginDate = platform.Foundation.NSDate.dateWithTimeIntervalSinceNow(
            intervalSeconds.toDouble(),
        )
        runCatching {
            BGTaskScheduler.sharedScheduler.submitTaskRequest(request, error = null)
        }.onFailure { e ->
            log.e(e) { "Failed to submit background task request" }
        }
    }

    companion object {
        const val TASK_IDENTIFIER = "com.simplevideo.whiteiptv.playlistRefresh"
    }
}
```

### Modified Files

#### 1. `gradle/libs.versions.toml`

- **What changes**: Add `work-runtime-ktx` library entry
- **Why**: WorkManager dependency for Android background scheduling
- **Specific changes**:
  - Add version: `work = "2.10.1"` (latest stable as of early 2026)
  - Add library: `androidx-work-runtime = { module = "androidx.work:work-runtime-ktx", version.ref = "work" }`

#### 2. `shared/build.gradle.kts`

- **What changes**: Add `work-runtime-ktx` to `androidMain.dependencies`
- **Why**: Worker and WorkManager APIs needed in androidMain source set
- **Specific changes**:
  - Add in `androidMain.dependencies` block: `implementation(libs.androidx.work.runtime)`

#### 3. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/di/KoinModule.kt`

- **What changes**: Replace `PlaylistAutoRefreshScheduler` registration with `BackgroundRefreshCoordinator` as `single`. Remove the lambda-based scheduler registration.
- **Why**: The scheduler is being replaced; the coordinator provides refresh logic to platform workers.
- **Specific changes**:
  - Remove import `PlaylistAutoRefreshScheduler`
  - Add import `BackgroundRefreshCoordinator`
  - In `settingsModule`, replace the `PlaylistAutoRefreshScheduler` block (lines 117-123) with:
    ```kotlin
    single {
        BackgroundRefreshCoordinator(
            playlistRepository = get(),
            refreshPlaylist = { source -> get<ImportPlaylistUseCase>().invoke(source) },
        )
    }
    ```

#### 4. `shared/src/androidMain/kotlin/com/simplevideo/whiteiptv/di/PlatformModule.kt`

- **What changes**: Register `AndroidBackgroundScheduler` as `BackgroundScheduler`
- **Why**: Platform DI registration following existing pattern
- **Specific changes**:
  - Add: `single<BackgroundScheduler> { AndroidBackgroundScheduler(get()) }`

#### 5. `shared/src/iosMain/kotlin/com/simplevideo/whiteiptv/di/PlatformModule.kt`

- **What changes**: Register `IOSBackgroundScheduler` as `BackgroundScheduler`
- **Why**: Platform DI registration following existing pattern
- **Specific changes**:
  - Add: `single<BackgroundScheduler> { IOSBackgroundScheduler(get()) }`

#### 6. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/App.kt`

- **What changes**: Replace `PlaylistAutoRefreshScheduler` injection with `BackgroundScheduler` startup check. On launch, if auto-update is enabled, call `schedule()` with the computed interval.
- **Why**: Background scheduler needs to be (re)scheduled on app launch in case it was not previously set up (first install with toggle on, or OS cleared the scheduled work).
- **Specific changes**:
  - Remove `PlaylistAutoRefreshScheduler` import and injection
  - Add `BackgroundScheduler` and `SettingsPreferences` and `BackgroundRefreshCoordinator` injections
  - Replace `LaunchedEffect` block:
    ```kotlin
    val backgroundScheduler: BackgroundScheduler = koinInject()
    val settingsPreferences: SettingsPreferences = koinInject()
    val refreshCoordinator: BackgroundRefreshCoordinator = koinInject()

    LaunchedEffect(Unit) {
        if (settingsPreferences.getAutoUpdateEnabled() && !backgroundScheduler.isScheduled()) {
            val interval = refreshCoordinator.calculateIntervalSeconds()
            backgroundScheduler.schedule(interval)
        }
    }
    ```

#### 7. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/SettingsViewModel.kt`

- **What changes**: Inject `BackgroundScheduler` and `BackgroundRefreshCoordinator`. On auto-update toggle, call `schedule()` or `cancel()`.
- **Why**: Toggle must immediately activate/deactivate background scheduling.
- **Specific changes**:
  - Add constructor parameters: `private val backgroundScheduler: BackgroundScheduler`, `private val refreshCoordinator: BackgroundRefreshCoordinator`
  - In `OnAutoUpdateChanged` handler (lines 49-52), add:
    ```kotlin
    if (viewEvent.enabled) {
        viewModelScope.launch {
            val interval = refreshCoordinator.calculateIntervalSeconds()
            backgroundScheduler.schedule(interval)
        }
    } else {
        backgroundScheduler.cancel()
    }
    ```

#### 8. `iosApp/iosApp/Info.plist`

- **What changes**: Add `BGTaskSchedulerPermittedIdentifiers` array with the task identifier
- **Why**: Required by iOS to allow BGTaskScheduler task registration
- **Specific changes**:
  ```xml
  <key>BGTaskSchedulerPermittedIdentifiers</key>
  <array>
      <string>com.simplevideo.whiteiptv.playlistRefresh</string>
  </array>
  ```

#### 9. `iosApp/iosApp/iOSApp.swift`

- **What changes**: Register the BGTask handler on app launch and import BackgroundTasks framework
- **Why**: iOS requires task handlers to be registered before the app finishes launching
- **Specific changes**:
  ```swift
  import BackgroundTasks
  import ComposeApp

  @main
  struct iOSApp: App {
      init() {
          BGTaskScheduler.shared.register(
              forTaskWithIdentifier: "com.simplevideo.whiteiptv.playlistRefresh",
              using: nil
          ) { task in
              guard let refreshTask = task as? BGAppRefreshTask else { return }
              // Get the IOSBackgroundScheduler from Koin and handle the task
              let scheduler = KoinHelper.shared.getIOSBackgroundScheduler()
              scheduler.handleBackgroundTask(task: refreshTask)
          }
      }

      var body: some Scene {
          WindowGroup {
              ContentView()
          }
      }
  }
  ```

#### 10. `shared/src/iosMain/kotlin/com/simplevideo/whiteiptv/platform/KoinHelper.kt` (NEW)

- **Purpose**: Kotlin helper to expose Koin-resolved dependencies to Swift code
- **Key contents**:
  ```kotlin
  package com.simplevideo.whiteiptv.platform

  import org.koin.core.component.KoinComponent
  import org.koin.core.component.inject

  /**
   * Helper class to expose Koin dependencies to Swift code.
   * Swift cannot directly call Koin's generic resolution, so this
   * provides typed accessor methods.
   */
  class KoinHelper : KoinComponent {
      private val iosBackgroundScheduler: IOSBackgroundScheduler by inject()

      fun getIOSBackgroundScheduler(): IOSBackgroundScheduler = iosBackgroundScheduler

      companion object {
          val shared = KoinHelper()
      }
  }
  ```

### Files to Delete

#### `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/scheduler/PlaylistAutoRefreshScheduler.kt`

- **Why**: Replaced by `BackgroundRefreshCoordinator` + platform `BackgroundScheduler` implementations

### Database Changes

None. No new entities, DAOs, or migrations required.

### DI Changes

| Module | Change |
|---|---|
| `settingsModule` (KoinModule.kt) | Replace `PlaylistAutoRefreshScheduler` with `BackgroundRefreshCoordinator` as `single` |
| `platformModule` (Android) | Add `single<BackgroundScheduler> { AndroidBackgroundScheduler(get()) }` |
| `platformModule` (iOS) | Add `single<BackgroundScheduler> { IOSBackgroundScheduler(get()) }` |
| `viewModelModule` | No change (SettingsViewModel gets new params via Koin auto-resolution) |

## Implementation Order

1. **Add `work-runtime-ktx` dependency** -- `gradle/libs.versions.toml` + `shared/build.gradle.kts`
2. **Create `BackgroundScheduler` interface** -- `shared/src/commonMain/.../platform/BackgroundScheduler.kt`
3. **Create `BackgroundRefreshCoordinator`** -- `shared/src/commonMain/.../data/scheduler/BackgroundRefreshCoordinator.kt` (extract logic from `PlaylistAutoRefreshScheduler`)
4. **Create `PlaylistRefreshWorker`** -- `shared/src/androidMain/.../platform/PlaylistRefreshWorker.kt`
5. **Create `AndroidBackgroundScheduler`** -- `shared/src/androidMain/.../platform/AndroidBackgroundScheduler.kt`
6. **Create `IOSBackgroundScheduler`** -- `shared/src/iosMain/.../platform/IOSBackgroundScheduler.kt`
7. **Create `KoinHelper`** -- `shared/src/iosMain/.../platform/KoinHelper.kt`
8. **Update `KoinModule.kt`** -- Replace scheduler registration with coordinator
9. **Update Android `PlatformModule.kt`** -- Register `AndroidBackgroundScheduler`
10. **Update iOS `PlatformModule.kt`** -- Register `IOSBackgroundScheduler`
11. **Update `SettingsViewModel.kt`** -- Add `BackgroundScheduler` + coordinator injection, call schedule/cancel on toggle
12. **Update `App.kt`** -- Replace foreground scheduler startup with background scheduler check
13. **Delete `PlaylistAutoRefreshScheduler.kt`** -- Remove old foreground-only implementation
14. **Update `Info.plist`** -- Add BGTaskSchedulerPermittedIdentifiers
15. **Update `iOSApp.swift`** -- Register BGTask handler
16. **Rewrite tests** -- Update `PlaylistAutoRefreshSchedulerTest.kt` for `BackgroundRefreshCoordinator`
17. **Build and verify** -- `./gradlew :shared:testAndroidHostTest` + `./gradlew :androidApp:assembleDebug` + `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`

## Testing Strategy

### Unit Tests for `BackgroundRefreshCoordinator`

File: `shared/src/commonTest/.../data/scheduler/BackgroundRefreshCoordinatorTest.kt`

1. **Refreshes all URL-based playlists** -- Verify `refreshPlaylist` called for each `http://`/`https://` playlist
2. **Skips local file playlists** -- Add `file://` playlists, verify they are not refreshed
3. **Handles individual refresh errors gracefully** -- One playlist throws, others still refresh
4. **No-op when no playlists exist** -- Empty DB, no calls made
5. **No-op when all playlists are local files** -- Only `file://` playlists, no calls
6. **calculateIntervalSeconds uses minimum across playlists** -- Two playlists with different intervals, returns the smaller
7. **calculateIntervalSeconds clamps to minimum** -- Playlist with interval=10 returns 900
8. **calculateIntervalSeconds uses default when no intervals specified** -- Returns 21600

### Integration Points (Manual / E2E)

- Android: Toggle auto-update on, background the app, verify WorkManager work appears in `adb shell dumpsys jobscheduler`
- Android: Verify work executes after the interval (use `adb shell cmd jobscheduler run -f com.simplevideo.whiteiptv <job-id>` for testing)
- iOS: Toggle on, use Xcode BGTask debugger command `e -l objc -- (void)[[BGTaskScheduler sharedScheduler] _simulateLaunchForTaskWithIdentifier:@"com.simplevideo.whiteiptv.playlistRefresh"]`
- Both: Toggle off, verify scheduled work is cancelled
- Both: Kill app, relaunch, verify schedule is re-established if toggle is on

### Edge Cases

- Toggle on/off rapidly -- only latest state should matter (last schedule/cancel wins)
- No network when work fires -- WorkManager retries automatically; iOS task completes with success=false
- App killed during refresh -- WorkManager handles this via process death recovery; iOS task expires
- Koin not initialized when worker runs (race condition) -- `KoinComponent.inject()` will throw; worker returns `Result.retry()`
- No playlists in DB -- coordinator returns immediately, no error

### Key Assertions

- `BackgroundRefreshCoordinator.refreshAllPlaylists()` calls `refreshPlaylist` with `PlaylistSource.Url` for each URL playlist
- `BackgroundRefreshCoordinator.calculateIntervalSeconds()` returns correct clamped/defaulted value
- `AndroidBackgroundScheduler.schedule()` enqueues a `PeriodicWorkRequest` with correct constraints
- `AndroidBackgroundScheduler.cancel()` cancels the unique work
- `SettingsViewModel` calls `schedule()` when toggle is enabled and `cancel()` when disabled

## Doc Updates Required

1. **`docs/features/settings.md`** (line 55) -- Update "Auto Update" implementation note: change "Runs in foreground only" to "Uses WorkManager (Android) and BGTaskScheduler (iOS) for true background refresh. Minimum interval is 15 minutes per platform constraints."
2. **`docs/constraints/current-limitations.md`** -- No change needed (auto-refresh is not listed as a limitation)
3. **`docs/constraints/open-questions.md`** -- No change needed (no related open questions)
