# DataStore Migration -- Implementation Plan

## Summary

Replace the `multiplatform-settings` (russhwolf) library with Jetpack DataStore Preferences for all app settings persistence. This migrates `SettingsPreferences` and `ThemePreferences` from synchronous `Settings` API to the asynchronous, Flow-based DataStore API. DataStore provides type-safe preference keys, atomic transactions via `dataStore.edit {}`, and native coroutine/Flow integration that aligns with the existing MVI architecture. The DataStore instance requires platform-specific file path resolution (expect/actual pattern), similar to the existing Room database setup.

## Decisions Made

### 1. DataStore instance creation pattern
- **Decision**: Use a common `createDataStore(producePath: () -> String)` factory function (not expect/actual) with platform-specific callers in Koin `platformModule()`.
- **Rationale**: This matches the official Android developer documentation pattern for DataStore KMP. The `PreferenceDataStoreFactory.createWithPath()` API lives in commonMain; only the file path differs per platform. This avoids unnecessary expect/actual declarations.
- **Alternatives considered**: (a) expect/actual `fun createDataStore()` -- adds boilerplate for no benefit since only the path differs. (b) Passing path string directly in Koin modules -- less encapsulated but simpler; rejected because the factory function is trivial and matches official docs.

### 2. Keep synchronous getters alongside Flow-based reads
- **Decision**: Convert `SettingsPreferences` and `ThemePreferences` to expose `Flow`-based reads only. Callers that need a one-shot value will use `dataStore.data.first()` (suspend). The `SettingsViewModel.init` block and `ThemeRepositoryImpl` constructor will switch to coroutine-based initialization.
- **Rationale**: DataStore is fundamentally async. Fighting it with `runBlocking` would negate all benefits and risk ANRs. The SettingsViewModel already runs in a ViewModel scope. ThemeRepositoryImpl can launch a coroutine to load the initial value.
- **Alternatives considered**: (a) `runBlocking` for initial reads -- defeats the purpose of migration, blocks main thread. (b) Keep both sync and async APIs -- not possible with DataStore, it has no sync API.

### 3. SettingsViewModel initialization strategy
- **Decision**: Load preferences in `init` via `viewModelScope.launch` and populate state when data arrives. The initial state will use default values, then update reactively. Since settings load from local disk, the latency is sub-millisecond and users will not see a flash.
- **Rationale**: This is the standard pattern for DataStore consumption in ViewModels. The data is local so the async read is effectively instant.
- **Alternatives considered**: SplashScreen pre-loading -- over-engineered for local preferences.

### 4. ThemeRepositoryImpl initialization strategy
- **Decision**: Change `ThemeRepositoryImpl` to accept `DataStore<Preferences>` directly (instead of `ThemePreferences`) and observe `dataStore.data` as a `Flow<ThemeMode>`. The `themeMode` property becomes a `StateFlow` backed by `stateIn()` with `SharingStarted.Eagerly` and a default of `ThemeMode.System`.
- **Rationale**: This eliminates the need for ThemePreferences as a separate class. However, to keep the migration minimal and preserve the existing layering, ThemePreferences will be kept as an intermediary that wraps DataStore operations.
- **Alternatives considered**: Merging ThemePreferences into ThemeRepositoryImpl -- cleaner but a larger refactor outside the migration scope.

### 5. Test strategy for DataStore
- **Decision**: Use `PreferenceDataStoreFactory.create()` with `InMemoryStorage()` for tests (available in `datastore-preferences` test utilities). This replaces `MapSettings` from `multiplatform-settings-test`.
- **Rationale**: DataStore provides `InMemoryStorage` for testing that requires no filesystem. This is the recommended approach.
- **Alternatives considered**: Using temp files -- flaky, platform-dependent.

### 6. DataStore file name
- **Decision**: Use `"whiteiptv.preferences_pb"` as the DataStore file name.
- **Rationale**: Follows the DataStore convention of `*.preferences_pb` suffix. Namespaced to the app.
- **Alternatives considered**: `"settings.preferences_pb"` -- too generic.

### 7. Single vs multiple DataStore instances
- **Decision**: Use a single `DataStore<Preferences>` instance shared between `SettingsPreferences` and `ThemePreferences`.
- **Rationale**: DataStore guarantees single-process single-instance access. Multiple DataStore files for a handful of preferences would be unnecessary overhead. Both preference classes will use distinct key prefixes to avoid collisions.
- **Alternatives considered**: Separate DataStore files per preference class -- unnecessary for the small number of keys (4 total).

### 8. autoUpdateEnabledFlow handling
- **Decision**: Replace the manual `MutableStateFlow` + sync read pattern in `SettingsPreferences.autoUpdateEnabledFlow` with a mapped `Flow` from `dataStore.data`. The flow will be: `dataStore.data.map { it[AUTO_UPDATE_KEY] ?: false }.distinctUntilChanged()`.
- **Rationale**: DataStore natively provides reactive Flows. The manual StateFlow was a workaround for the synchronous multiplatform-settings API.
- **Alternatives considered**: Keep MutableStateFlow and update it on writes -- redundant with DataStore's built-in reactivity.

## Current State

### Source Files

- **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/SettingsPreferences.kt`** (lines 1-56): Wraps `com.russhwolf.settings.Settings`. Manages 3 preferences: `accent_color` (String/enum), `channel_view_mode` (String/enum), `auto_update_playlists` (Boolean). Exposes `autoUpdateEnabledFlow: StateFlow<Boolean>` via manual `MutableStateFlow`.

- **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/ThemePreferences.kt`** (lines 1-35): Wraps `com.russhwolf.settings.Settings`. Manages 1 preference: `theme_mode` (String mapped to `ThemeMode` enum).

- **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/di/KoinModule.kt`** (lines 112-124): `settingsModule` creates `Settings()` as singleton, then `ThemePreferences`, `ThemeRepositoryImpl`, `SettingsPreferences`, and `PlaylistAutoRefreshScheduler`.

- **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/SettingsViewModel.kt`** (lines 14-106): Consumes `SettingsPreferences` synchronously in `init` block (lines 23-29) and in event handlers (lines 39-48, 76).

- **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/repository/ThemeRepositoryImpl.kt`** (lines 1-21): Consumes `ThemePreferences` synchronously in constructor (line 14) and `setThemeMode` (line 18).

- **`shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/scheduler/PlaylistAutoRefreshScheduler.kt`** (lines 1-112): Consumes `SettingsPreferences.autoUpdateEnabledFlow` (line 39).

### Test Files

- **`shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/data/local/SettingsPreferencesTest.kt`** (lines 1-159): Uses `MapSettings` for in-memory testing. 16 test cases.

- **`shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/data/local/ThemePreferencesTest.kt`** (lines 1-71): Uses `MapSettings` for in-memory testing. 7 test cases.

- **`shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/data/repository/ThemeRepositoryImplTest.kt`** (lines 1-73): Uses `MapSettings` -> `ThemePreferences`. 6 test cases.

- **`shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/feature/settings/SettingsViewModelTest.kt`** (lines 1-353): Uses `MapSettings` -> `SettingsPreferences` + `ThemePreferences`. 20 test cases.

- **`shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/data/scheduler/PlaylistAutoRefreshSchedulerTest.kt`** (lines 1-316): Entirely commented out. Uses `MapSettings` -> `SettingsPreferences`.

### Build Configuration

- **`gradle/libs.versions.toml`** (line 33): `multiplatformSettings = "1.3.0"`
- **`gradle/libs.versions.toml`** (lines 76-77): `multiplatform-settings-no-arg` and `multiplatform-settings-test` library declarations
- **`shared/build.gradle.kts`** (line 79): `implementation(libs.multiplatform.settings.no.arg)` in commonMain
- **`shared/build.gradle.kts`** (line 83): `implementation(libs.multiplatform.settings.test)` in commonTest

### Platform Modules

- **`shared/src/androidMain/kotlin/com/simplevideo/whiteiptv/di/PlatformModule.kt`** (lines 1-40): No settings-related code. DataStore path will be added here.
- **`shared/src/iosMain/kotlin/com/simplevideo/whiteiptv/di/PlatformModule.kt`** (lines 1-53): Has `documentDirectory()` helper already. DataStore path will reuse this.

## Changes Required

### New Files

#### 1. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/DataStoreFactory.kt`
- **Purpose**: Common factory function to create the DataStore instance.
- **Key contents**:
```kotlin
package com.simplevideo.whiteiptv.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import okio.Path.Companion.toPath

fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

internal const val DATA_STORE_FILE_NAME = "whiteiptv.preferences_pb"
```

### Modified Files

#### 2. `gradle/libs.versions.toml`
- **What changes**:
  - Add version: `datastore = "1.2.1"` (latest stable KMP-compatible release)
  - Add libraries: `androidx-datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }`
  - Remove: `multiplatformSettings = "1.3.0"` version entry
  - Remove: `multiplatform-settings-no-arg` and `multiplatform-settings-test` library entries
- **Why**: Replace the dependency.

#### 3. `shared/build.gradle.kts`
- **What changes**:
  - In `commonMain.dependencies`: Replace `implementation(libs.multiplatform.settings.no.arg)` with `implementation(libs.androidx.datastore.preferences)`
  - In `commonTest.dependencies`: Remove `implementation(libs.multiplatform.settings.test)`
- **Why**: Swap dependencies. DataStore tests use `PreferenceDataStoreFactory.create()` with in-memory storage from the main artifact; no separate test artifact needed.

#### 4. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/SettingsPreferences.kt`
- **What changes**: Complete rewrite.
  - Constructor takes `DataStore<Preferences>` instead of `Settings`
  - Define type-safe keys: `val ACCENT_COLOR_KEY = stringPreferencesKey("accent_color")`, `val CHANNEL_VIEW_MODE_KEY = stringPreferencesKey("channel_view_mode")`, `val AUTO_UPDATE_KEY = booleanPreferencesKey("auto_update_playlists")`
  - Getters become `suspend fun` returning values from `dataStore.data.first()`
  - Setters become `suspend fun` using `dataStore.edit { prefs -> ... }`
  - `autoUpdateEnabledFlow` becomes `val autoUpdateEnabledFlow: Flow<Boolean> = dataStore.data.map { it[AUTO_UPDATE_KEY] ?: false }.distinctUntilChanged()`
  - `resetAll()` becomes `suspend fun` using `dataStore.edit { it.clear() }`
- **Why**: Core migration to DataStore API.

#### 5. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/ThemePreferences.kt`
- **What changes**: Complete rewrite.
  - Constructor takes `DataStore<Preferences>` instead of `Settings`
  - Define key: `val THEME_MODE_KEY = stringPreferencesKey("theme_mode")`
  - `getThemeMode()` becomes `suspend fun` reading from `dataStore.data.first()`
  - `setThemeMode()` becomes `suspend fun` using `dataStore.edit { ... }`
  - Add `themeModeFlow: Flow<ThemeMode>` for reactive observation
- **Why**: Core migration to DataStore API.

#### 6. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/repository/ThemeRepositoryImpl.kt`
- **What changes**:
  - Constructor initializes `_themeMode` via `viewModelScope`/`CoroutineScope` launch that collects `themePreferences.themeModeFlow`
  - Accept a `CoroutineScope` parameter for launching the collection (or create one internally with `Dispatchers.Default`)
  - `setThemeMode()` becomes `suspend fun` (or launches internally)
  - Since `ThemeRepository.setThemeMode()` is called from ViewModel event handlers, it needs to remain callable from a coroutine. Check the interface.
- **Why**: ThemePreferences methods are now suspend functions.

#### 7. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/domain/repository/ThemeRepository.kt`
- **What changes**: `setThemeMode(mode: ThemeMode)` becomes `suspend fun setThemeMode(mode: ThemeMode)`.
- **Why**: The underlying DataStore write is asynchronous.

#### 8. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/SettingsViewModel.kt`
- **What changes**:
  - `init` block: wrap preference reads in `viewModelScope.launch { ... }` and use suspend calls
  - Event handlers for `OnAccentColorChanged`, `OnChannelViewModeChanged`, `OnAutoUpdateChanged`: wrap `settingsPreferences.set*()` calls in `viewModelScope.launch { ... }`
  - `OnResetConfirm`: wrap `settingsPreferences.resetAll()` and `themeRepository.setThemeMode()` in `viewModelScope.launch { ... }`
- **Why**: All preference operations are now suspend functions.

#### 9. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/di/KoinModule.kt`
- **What changes**:
  - In `settingsModule`: Remove `single { Settings() }`. Replace with nothing (DataStore provided by platformModule).
  - Update `ThemePreferences` and `SettingsPreferences` constructors -- they now take `DataStore<Preferences>` from `get()`.
  - `ThemeRepositoryImpl` may need a `CoroutineScope` injected if it launches coroutines internally.
- **Why**: Settings() is no longer used. DataStore instance comes from platformModule.

#### 10. `shared/src/androidMain/kotlin/com/simplevideo/whiteiptv/di/PlatformModule.kt`
- **What changes**: Add DataStore singleton:
```kotlin
single<DataStore<Preferences>> {
    createDataStore(
        producePath = {
            get<Context>().filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath
        }
    )
}
```
- **Why**: Android-specific file path for DataStore.

#### 11. `shared/src/iosMain/kotlin/com/simplevideo/whiteiptv/di/PlatformModule.kt`
- **What changes**: Add DataStore singleton:
```kotlin
single<DataStore<Preferences>> {
    createDataStore(
        producePath = {
            documentDirectory() + "/$DATA_STORE_FILE_NAME"
        }
    )
}
```
- **Why**: iOS-specific file path for DataStore.

#### 12. `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/data/local/SettingsPreferencesTest.kt`
- **What changes**: Complete rewrite.
  - Replace `MapSettings` with `PreferenceDataStoreFactory.create(storage = InMemoryStorage())` (from `androidx.datastore.core.InMemoryStorage` or use `PreferenceDataStoreFactory.create(scope = testScope, produceFile = { ... })` with a temp path)
  - All test methods become `suspend` / use `runTest`
  - Assertions remain the same logically but use suspend calls
  - Flow assertions for `autoUpdateEnabledFlow` use `turbine` or `first()`
- **Why**: DataStore API is async; tests must adapt.

#### 13. `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/data/local/ThemePreferencesTest.kt`
- **What changes**: Same pattern as SettingsPreferencesTest -- replace `MapSettings` with in-memory DataStore.
- **Why**: DataStore API is async.

#### 14. `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/data/repository/ThemeRepositoryImplTest.kt`
- **What changes**: Replace `MapSettings` -> `ThemePreferences(settings)` chain with in-memory DataStore -> `ThemePreferences(dataStore)`. Tests become suspend/runTest.
- **Why**: Constructor changed.

#### 15. `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/feature/settings/SettingsViewModelTest.kt`
- **What changes**: Replace `MapSettings` setup with in-memory DataStore. Since SettingsViewModel now loads preferences asynchronously in init, tests may need `advanceUntilIdle()` after construction to let the init coroutine complete.
- **Why**: Constructor dependency changed.

#### 16. `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/data/scheduler/PlaylistAutoRefreshSchedulerTest.kt` (commented out)
- **What changes**: Update commented-out test code to use DataStore-based SettingsPreferences. No urgency since tests are already disabled.
- **Why**: Keep commented code in sync if it's ever re-enabled.

### Database Changes

None. DataStore is a separate persistence mechanism from Room.

### DI Changes

- **Remove** from `settingsModule`: `single { Settings() }`
- **Add** to Android `platformModule()`: `single<DataStore<Preferences>> { createDataStore(...) }`
- **Add** to iOS `platformModule()`: `single<DataStore<Preferences>> { createDataStore(...) }`
- **Keep** in `settingsModule`: `singleOf(::ThemePreferences)`, `singleOf(::SettingsPreferences)` (constructor parameters change but Koin resolves automatically)

## Implementation Order

1. **Update `gradle/libs.versions.toml`**: Add DataStore dependency, remove multiplatform-settings dependency.

2. **Update `shared/build.gradle.kts`**: Swap dependency declarations in commonMain and commonTest.

3. **Create `DataStoreFactory.kt`**: Add the `createDataStore()` common factory function and file name constant.

4. **Update Android `PlatformModule.kt`**: Add `DataStore<Preferences>` singleton with Android file path.

5. **Update iOS `PlatformModule.kt`**: Add `DataStore<Preferences>` singleton with iOS file path.

6. **Rewrite `ThemePreferences.kt`**: Convert to DataStore API with suspend functions and `themeModeFlow`.

7. **Rewrite `SettingsPreferences.kt`**: Convert to DataStore API with suspend functions and Flow-based `autoUpdateEnabledFlow`.

8. **Update `ThemeRepository.kt` interface**: Make `setThemeMode` a suspend function.

9. **Update `ThemeRepositoryImpl.kt`**: Adapt to suspend-based ThemePreferences. Add coroutine-based initialization for `themeMode` StateFlow.

10. **Update `SettingsViewModel.kt`**: Wrap all preference reads/writes in `viewModelScope.launch`.

11. **Update `KoinModule.kt`**: Remove `Settings()` singleton, adjust `settingsModule`.

12. **Rewrite `ThemePreferencesTest.kt`**: Adapt to in-memory DataStore and suspend API.

13. **Rewrite `SettingsPreferencesTest.kt`**: Adapt to in-memory DataStore and suspend API.

14. **Rewrite `ThemeRepositoryImplTest.kt`**: Adapt to new ThemePreferences constructor.

15. **Update `SettingsViewModelTest.kt`**: Adapt to new constructors, add `advanceUntilIdle()` where needed.

16. **Update commented `PlaylistAutoRefreshSchedulerTest.kt`**: Update references in commented code.

17. **Verify build**: Run `./gradlew :shared:testAndroidHostTest` and `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`.

## Testing Strategy

### Unit Tests

**SettingsPreferencesTest** (rewrite):
- Default values: accent color = Teal, channel view mode = List, auto update = false
- Set and read back each preference (suspend)
- Invalid/corrupt values fall back to defaults
- `resetAll()` clears everything to defaults
- `autoUpdateEnabledFlow` emits updates reactively
- Cross-instance persistence (same DataStore, new SettingsPreferences)

**ThemePreferencesTest** (rewrite):
- Default theme mode = System
- Set and read back Light, Dark, System
- Invalid stored values fall back to System
- `themeModeFlow` emits updates reactively
- Cross-instance persistence

**ThemeRepositoryImplTest** (adapt):
- Initial themeMode is System
- `setThemeMode()` updates StateFlow and persists
- Multiple rapid switches preserve last value

**SettingsViewModelTest** (adapt):
- Init loads defaults when no preferences stored
- Init loads previously stored preferences (requires `advanceUntilIdle()`)
- Event handlers persist and update state
- Reset clears all settings

### Edge Cases
- DataStore accessed before any writes (all defaults)
- Concurrent writes from multiple coroutines (DataStore handles this atomically)
- DataStore corruption (DataStore auto-recovers by clearing data)

### Key Assertions
- All preference reads return correct defaults when no data exists
- All preference writes are persisted and observable via Flows
- `autoUpdateEnabledFlow` in `PlaylistAutoRefreshScheduler` still works correctly
- SettingsViewModel state updates reflect preference changes
- Theme changes propagate through ThemeRepository to the UI

## Doc Updates Required

- **`docs/feature-review.md`**: Mark item #2 (SharedPreferences -> DataStore) as resolved/completed.
- **`docs/constraints/current-limitations.md`**: No changes needed (this migration is not listed as a limitation).
