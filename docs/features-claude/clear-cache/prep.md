# Clear Cache -- Implementation Plan

## Summary

Implement real cache clearing functionality in the Settings screen. Replace the hardcoded "0 MB" subtitle with the actual Coil image cache size (disk + memory), and clear both caches when the user taps "Clear Cache". The only meaningful cache in the app is Coil's image cache (channel logos). ExoPlayer uses RAM-only buffering and Ktor has no disk cache configured.

## Decisions Made

### 1. Use Coil's commonMain API directly -- no expect/actual needed

- **Decision**: Access the Coil singleton `ImageLoader` via `SingletonImageLoader.get(PlatformContext)` from commonMain code. Create a `CacheManager` interface + `CoilCacheManager` implementation in commonMain.
- **Rationale**: Coil 3.4.0 is fully KMP-compatible. `SingletonImageLoader`, `PlatformContext`, `DiskCache`, and `MemoryCache` are all available in commonMain. No expect/actual needed.
- **Alternatives considered**: (a) expect/actual `CacheManager` with platform implementations -- unnecessary complexity. (b) Accessing ImageLoader from the Composable layer only -- would break MVI pattern since cache size needs to be in state.

### 2. PlatformContext injection strategy

- **Decision**: Add a `coil3.PlatformContext` binding to each platform's `platformModule()`. On Android, `PlatformContext` is a typealias for `android.content.Context`, so use `androidContext()`. On iOS, use `PlatformContext.INSTANCE`.
- **Rationale**: The ViewModel needs to access the ImageLoader outside of Compose. PlatformContext is the only dependency needed to get the singleton ImageLoader. Koin does not automatically resolve typealiases, so an explicit binding is required on Android even though `Context` is already in the graph.
- **Alternatives considered**: Passing PlatformContext from the Screen to the ViewModel via an event -- violates MVI by leaking platform details into events.

### 3. Cache size includes both disk and memory cache

- **Decision**: Display the combined size of disk cache + memory cache, formatted as human-readable (KB/MB/GB).
- **Rationale**: Users care about total space used, not the breakdown.
- **Alternatives considered**: Show only disk cache size -- would be slightly misleading since memory cache also uses resources.

### 4. Cache size updates on init and after clearing

- **Decision**: Calculate cache size in ViewModel `init` and recalculate after clearing. No periodic refresh.
- **Rationale**: Cache size only changes when images are loaded (browsing channels) or cleared. Polling is wasteful for a settings screen.
- **Alternatives considered**: Real-time updates via Flow -- overkill for a settings screen.

### 5. No confirmation dialog for cache clearing

- **Decision**: Clear cache immediately on tap, show a snackbar confirmation. No "Are you sure?" dialog.
- **Rationale**: Cache clearing is non-destructive (images will be re-downloaded as needed). The existing code already uses immediate clearing without a dialog. Consistent with current `OnClearCacheClick` -> `ShowCacheCleared` pattern.
- **Alternatives considered**: Adding a confirmation dialog -- unnecessary for a safe, reversible operation.

### 6. Interface + implementation for testability

- **Decision**: Define `CacheManager` as an interface with `CoilCacheManager` as the concrete implementation. This allows `FakeCacheManager` in tests.
- **Rationale**: The existing `SettingsViewModelTest` (in `commonTest`) uses concrete fakes (e.g., `FakeBackgroundScheduler`, `FakeChannelRepository`) and constructs the ViewModel manually. An interface is needed since `CoilCacheManager` depends on Coil's singleton which is unavailable in unit tests.
- **Alternatives considered**: Making `CoilCacheManager` open -- less clean than an interface.

## Current State

### Existing files involved:

- **`shared/src/commonMain/.../feature/settings/mvi/SettingsMvi.kt`** (lines 7-15): `SettingsState` has no `cacheSize` field. `SettingsEvent.OnClearCacheClick` exists (line 22). `SettingsAction.ShowCacheCleared` exists (line 33).
- **`shared/src/commonMain/.../feature/settings/SettingsViewModel.kt`** (lines 76-78): `OnClearCacheClick` handler is a no-op that just emits `ShowCacheCleared`.
- **`shared/src/commonMain/.../feature/settings/SettingsScreen.kt`** (lines 194-213): `DataStorageSection` takes only `onEvent` parameter. Passes hardcoded `"0 MB"` as subtitle for "Clear Cache".
- **`shared/src/commonMain/.../di/KoinModule.kt`** (lines 113-123): `settingsModule` has no cache-related registrations.
- **`shared/src/commonTest/.../feature/settings/SettingsViewModelTest.kt`**: Existing test with `createViewModel()` helper (line 88-96) using fakes. Has one cache test (lines 272-281) that only verifies the action is emitted.
- **Coil dependency** (`gradle/libs.versions.toml` line 28): `coil = "3.4.0"`, used via `coil-compose` and `coil-network-ktor3` in `commonMain.dependencies`.
- **No custom ImageLoader setup**: The project uses Coil's default singleton (no `setSingletonImageLoaderFactory` call found).

### Platform modules:

- **`shared/src/androidMain/.../di/PlatformModule.kt`**: Provides `AppDatabase`, `FileReader`, `VideoPlayerFactory`, `FilePickerFactory`, `BackgroundScheduler`, `DataStore`. Context accessed via `get<Context>()`.
- **`shared/src/iosMain/.../di/PlatformModule.kt`**: Same bindings, iOS implementations.

## Changes Required

### New Files

#### 1. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/cache/CacheManager.kt`

- **Purpose**: Interface defining cache operations.
- **Key contents**:

```kotlin
package com.simplevideo.whiteiptv.data.cache

/**
 * Manages image cache operations.
 * Provides cache size calculation and clearing functionality.
 */
interface CacheManager {
    /** Returns total cache size in bytes (disk + memory). */
    fun getCacheSizeBytes(): Long

    /** Returns formatted cache size string (e.g., "12.3 MB", "0 B"). */
    fun getFormattedCacheSize(): String

    /** Clears both disk and memory image caches. */
    fun clearCache()
}
```

#### 2. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/cache/CoilCacheManager.kt`

- **Purpose**: Concrete `CacheManager` implementation backed by Coil's ImageLoader.
- **Key contents**:

```kotlin
package com.simplevideo.whiteiptv.data.cache

import coil3.PlatformContext
import coil3.SingletonImageLoader

/**
 * [CacheManager] implementation that delegates to Coil's singleton ImageLoader
 * for disk and memory cache operations.
 */
class CoilCacheManager(
    private val platformContext: PlatformContext,
) : CacheManager {

    private val imageLoader
        get() = SingletonImageLoader.get(platformContext)

    override fun getCacheSizeBytes(): Long {
        val diskSize = imageLoader.diskCache?.size ?: 0L
        val memorySize = imageLoader.memoryCache?.size?.toLong() ?: 0L
        return diskSize + memorySize
    }

    override fun getFormattedCacheSize(): String = formatBytes(getCacheSizeBytes())

    override fun clearCache() {
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
    }

    companion object {
        private const val BYTES_PER_KB = 1024L
        private const val BYTES_PER_MB = 1024L * 1024L
        private const val BYTES_PER_GB = 1024L * 1024L * 1024L

        fun formatBytes(bytes: Long): String = when {
            bytes <= 0L -> "0 B"
            bytes < BYTES_PER_KB -> "$bytes B"
            bytes < BYTES_PER_MB -> {
                val kb = bytes.toDouble() / BYTES_PER_KB
                "${formatNumber(kb)} KB"
            }
            bytes < BYTES_PER_GB -> {
                val mb = bytes.toDouble() / BYTES_PER_MB
                "${formatNumber(mb)} MB"
            }
            else -> {
                val gb = bytes.toDouble() / BYTES_PER_GB
                "${formatNumber(gb)} GB"
            }
        }

        private fun formatNumber(value: Double): String {
            val rounded = (value * 10).toLong() / 10.0
            return if (rounded == rounded.toLong().toDouble()) {
                rounded.toLong().toString()
            } else {
                rounded.toString()
            }
        }
    }
}
```

#### 3. `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/data/cache/CoilCacheManagerTest.kt`

- **Purpose**: Unit tests for the `formatBytes` static method.
- **Key contents**: Test class with cases for 0, bytes, KB, MB, GB formatting.

#### 4. `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/data/cache/FakeCacheManager.kt`

- **Purpose**: Test double for `CacheManager` used by `SettingsViewModelTest`.
- **Key contents**:

```kotlin
package com.simplevideo.whiteiptv.data.cache

class FakeCacheManager(
    var cacheSizeBytes: Long = 0L,
) : CacheManager {
    var clearCalled = false
        private set

    override fun getCacheSizeBytes(): Long = cacheSizeBytes

    override fun getFormattedCacheSize(): String = CoilCacheManager.formatBytes(cacheSizeBytes)

    override fun clearCache() {
        clearCalled = true
        cacheSizeBytes = 0L
    }
}
```

### Modified Files

#### 1. `shared/src/commonMain/.../feature/settings/mvi/SettingsMvi.kt`

- **What changes**: Add `cacheSize: String = "0 B"` field to `SettingsState`.
- **Why**: UI needs to display the actual cache size from state.

Add after `autoUpdateEnabled`:
```kotlin
val cacheSize: String = "0 B",
```

#### 2. `shared/src/commonMain/.../feature/settings/SettingsViewModel.kt`

- **What changes**:
  1. Add `CacheManager` import and constructor parameter.
  2. In `init` block, add `cacheSize = cacheManager.getFormattedCacheSize()` to the `viewState.copy()` call.
  3. Replace the `OnClearCacheClick` handler body with real cache clearing + size update.
- **Why**: ViewModel needs to manage cache operations through the `CacheManager`.

Constructor change -- add after `refreshCoordinator`:
```kotlin
private val cacheManager: CacheManager,
```

Init block -- add to `viewState.copy()`:
```kotlin
cacheSize = cacheManager.getFormattedCacheSize(),
```

`OnClearCacheClick` handler -- replace lines 76-78:
```kotlin
is SettingsEvent.OnClearCacheClick -> {
    cacheManager.clearCache()
    viewState = viewState.copy(cacheSize = cacheManager.getFormattedCacheSize())
    viewAction = SettingsAction.ShowCacheCleared
}
```

#### 3. `shared/src/commonMain/.../feature/settings/SettingsScreen.kt`

- **What changes**:
  1. Change `DataStorageSection` call site to pass `state` (line 111).
  2. Change `DataStorageSection` function signature to accept `SettingsState`.
  3. Replace hardcoded `"0 MB"` with `state.cacheSize`.
- **Why**: Display the real cache size from state.

Call site change (line 111):
```kotlin
// Before:
item { DataStorageSection(viewModel::obtainEvent) }
// After:
item { DataStorageSection(state, viewModel::obtainEvent) }
```

Function signature change (lines 193-213):
```kotlin
@Composable
private fun DataStorageSection(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
) {
    SettingsSection(title = "Data & Storage") {
        SettingsItem(
            title = "Clear Cache",
            subtitle = state.cacheSize,
            onClick = { onEvent(SettingsEvent.OnClearCacheClick) },
        )
        // Clear Favorites and Reset items remain unchanged
    }
}
```

#### 4. `shared/src/commonMain/.../di/KoinModule.kt`

- **What changes**: Add `CoilCacheManager` registration in `settingsModule`, bound to `CacheManager` interface. Add imports.
- **Why**: ViewModel gets `CacheManager` injected via Koin.

Add import:
```kotlin
import com.simplevideo.whiteiptv.data.cache.CacheManager
import com.simplevideo.whiteiptv.data.cache.CoilCacheManager
```

Add to `settingsModule` (after `singleOf(::SettingsPreferences)`):
```kotlin
single<CacheManager> { CoilCacheManager(get()) }
```

#### 5. `shared/src/androidMain/.../di/PlatformModule.kt`

- **What changes**: Add `PlatformContext` binding. Since `PlatformContext` is a typealias for `android.content.Context` on Android, bind it to `androidContext()`.
- **Why**: `CoilCacheManager` constructor needs `PlatformContext`.

Add import:
```kotlin
import coil3.PlatformContext
```

Add binding in the module block:
```kotlin
single<PlatformContext> { get<Context>().applicationContext }
```

Note: `get<Context>()` is already available via Koin's `androidContext()`. Since `PlatformContext` is a typealias for `Context`, this cast works directly.

#### 6. `shared/src/iosMain/.../di/PlatformModule.kt`

- **What changes**: Add `PlatformContext` binding using the iOS singleton.
- **Why**: Same as Android -- `CoilCacheManager` needs `PlatformContext`.

Add import:
```kotlin
import coil3.PlatformContext
```

Add binding in the module block:
```kotlin
single<PlatformContext> { PlatformContext.INSTANCE }
```

#### 7. `shared/src/commonTest/.../feature/settings/SettingsViewModelTest.kt`

- **What changes**:
  1. Add `FakeCacheManager` field and initialization in `setUp()`.
  2. Pass `fakeCacheManager` to `createViewModel()`.
  3. Update existing cache test to also verify `cacheSize` state update.
  4. Add new test for cache size on init.
- **Why**: Existing `createViewModel()` will fail without the new `CacheManager` parameter. New behavior needs test coverage.

Add field:
```kotlin
private lateinit var fakeCacheManager: FakeCacheManager
```

In `setUp()`:
```kotlin
fakeCacheManager = FakeCacheManager(cacheSizeBytes = 1048576L) // 1 MB
```

In `createViewModel()`:
```kotlin
return SettingsViewModel(
    themeRepository = themeRepository,
    settingsPreferences = settingsPreferences,
    clearFavoritesUseCase = clearFavoritesUseCase,
    backgroundScheduler = fakeBackgroundScheduler,
    refreshCoordinator = refreshCoordinator,
    cacheManager = fakeCacheManager,
)
```

New test:
```kotlin
@Test
fun `init loads cache size from CacheManager`() = runTest {
    val viewModel = createViewModel()
    advanceUntilIdle()
    assertEquals("1 MB", viewModel.viewStates().value.cacheSize)
}
```

Update existing cache test (lines 272-281):
```kotlin
@Test
fun `OnClearCacheClick clears cache and updates size`() = runTest {
    val viewModel = createViewModel()
    advanceUntilIdle()
    assertEquals("1 MB", viewModel.viewStates().value.cacheSize)

    viewModel.obtainEvent(SettingsEvent.OnClearCacheClick)

    assertTrue(fakeCacheManager.clearCalled)
    assertEquals("0 B", viewModel.viewStates().value.cacheSize)
    val action = viewModel.viewActions().first()
    assertIs<SettingsAction.ShowCacheCleared>(action)
}
```

### Database Changes

None.

### DI Changes

- Register `CoilCacheManager` as `single` bound to `CacheManager` in `settingsModule`.
- Register `PlatformContext` in both `platformModule()` implementations (Android and iOS).

## Implementation Order

1. **Create `CacheManager` interface** -- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/cache/CacheManager.kt`

2. **Create `CoilCacheManager` implementation** -- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/cache/CoilCacheManager.kt`

3. **Register `PlatformContext` in platform DI modules** -- Update both `shared/src/androidMain/.../di/PlatformModule.kt` and `shared/src/iosMain/.../di/PlatformModule.kt`.

4. **Register `CoilCacheManager` in DI** -- Add `single<CacheManager> { CoilCacheManager(get()) }` to `settingsModule` in `shared/src/commonMain/.../di/KoinModule.kt`.

5. **Update `SettingsMvi.kt`** -- Add `cacheSize: String = "0 B"` field to `SettingsState`.

6. **Update `SettingsViewModel.kt`** -- Add `CacheManager` parameter, load cache size in `init`, implement real clearing in `OnClearCacheClick`.

7. **Update `SettingsScreen.kt`** -- Pass `state` to `DataStorageSection`, use `state.cacheSize` instead of hardcoded `"0 MB"`.

8. **Create test files** -- `FakeCacheManager.kt` and `CoilCacheManagerTest.kt` in `commonTest`.

9. **Update `SettingsViewModelTest.kt`** -- Add `FakeCacheManager`, update `createViewModel()`, add/update cache tests.

10. **Run `formatAll` and build** -- `./gradlew formatAll && ./gradlew :shared:testAndroidHostTest && ./gradlew :androidApp:assembleDebug`

## Testing Strategy

### Unit Tests

#### `CoilCacheManagerTest` (new: `shared/src/commonTest/.../data/cache/CoilCacheManagerTest.kt`)

Tests for `CoilCacheManager.formatBytes()` static method:

| Input | Expected Output |
|-------|----------------|
| `0` | `"0 B"` |
| `-1` | `"0 B"` |
| `500` | `"500 B"` |
| `1023` | `"1023 B"` |
| `1024` | `"1 KB"` |
| `1536` | `"1.5 KB"` |
| `1048576` (1 MB) | `"1 MB"` |
| `1572864` (1.5 MB) | `"1.5 MB"` |
| `10485760` (10 MB) | `"10 MB"` |
| `1073741824` (1 GB) | `"1 GB"` |

#### `SettingsViewModelTest` (existing: `shared/src/commonTest/.../feature/settings/SettingsViewModelTest.kt`)

Updated/new tests:
- **`init loads cache size from CacheManager`**: Verify `viewState.cacheSize == "1 MB"` (from `FakeCacheManager` initialized with 1 MB).
- **`OnClearCacheClick clears cache and updates size`**: Verify `clearCalled == true`, `cacheSize == "0 B"`, and `ShowCacheCleared` action is emitted (replaces existing test at line 273).

### Edge Cases

- Coil singleton not yet initialized (diskCache/memoryCache null) -- handled by `?: 0L` fallback in `CoilCacheManager`.
- Cache size is 0 on first app launch -- displays "0 B".
- Very large caches (GB range) -- handled by `formatBytes`.

### Coroutine Test Patterns

No special coroutine handling needed for `CacheManager` -- all methods are synchronous. The existing `SettingsViewModelTest` pattern with `StandardTestDispatcher` + `advanceUntilIdle()` applies since the `init` block uses `viewModelScope.launch`.

## Doc Updates Required

Update AFTER implementation:

1. **`docs/constraints/current-limitations.md`** -- Remove the "Settings: Clear Cache is a placeholder" section (lines 15-19).
2. **`docs/features/settings.md`** -- Update line 53 to remove the placeholder note. Add implementation note: "Clear Cache shows actual Coil image cache size (disk + memory) and clears both caches when tapped."

## Build & Test Commands

```bash
# Format code
./gradlew formatAll

# Run shared tests (includes CacheManager and SettingsViewModel tests)
./gradlew :shared:testAndroidHostTest

# Build Android app to verify compilation
./gradlew :androidApp:assembleDebug

# Build iOS framework to verify cross-platform compilation
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```
