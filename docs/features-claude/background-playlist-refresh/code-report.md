# Code Report: Background Playlist Refresh

## Files Created
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/platform/BackgroundScheduler.kt` -- interface with schedule/cancel/isScheduled methods
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/scheduler/BackgroundRefreshCoordinator.kt` -- shared refresh logic extracted from PlaylistAutoRefreshScheduler
- `shared/src/androidMain/kotlin/com/simplevideo/whiteiptv/platform/AndroidBackgroundScheduler.kt` -- WorkManager PeriodicWorkRequest implementation
- `shared/src/androidMain/kotlin/com/simplevideo/whiteiptv/platform/PlaylistRefreshWorker.kt` -- CoroutineWorker that delegates to BackgroundRefreshCoordinator
- `shared/src/iosMain/kotlin/com/simplevideo/whiteiptv/platform/IOSBackgroundScheduler.kt` -- BGTaskScheduler implementation
- `shared/src/iosMain/kotlin/com/simplevideo/whiteiptv/platform/KoinHelper.kt` -- exposes Koin dependencies to Swift code
- `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/data/scheduler/BackgroundRefreshCoordinatorTest.kt` -- 8 unit tests for coordinator

## Files Modified
- `gradle/libs.versions.toml` -- added work=2.10.1 version and androidx-work-runtime library entry
- `shared/build.gradle.kts` -- added work-runtime-ktx to androidMain dependencies
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/di/KoinModule.kt` -- replaced PlaylistAutoRefreshScheduler with BackgroundRefreshCoordinator in settingsModule
- `shared/src/androidMain/kotlin/com/simplevideo/whiteiptv/di/PlatformModule.kt` -- registered AndroidBackgroundScheduler as BackgroundScheduler
- `shared/src/iosMain/kotlin/com/simplevideo/whiteiptv/di/PlatformModule.kt` -- registered IOSBackgroundScheduler as BackgroundScheduler
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/SettingsViewModel.kt` -- added BackgroundScheduler and BackgroundRefreshCoordinator params, calls schedule/cancel on toggle
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/App.kt` -- replaced PlaylistAutoRefreshScheduler with BackgroundScheduler startup check
- `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/feature/settings/SettingsViewModelTest.kt` -- added FakeBackgroundScheduler and BackgroundRefreshCoordinator to test setup
- `iosApp/iosApp/Info.plist` -- added BGTaskSchedulerPermittedIdentifiers
- `iosApp/iosApp/iOSApp.swift` -- registered BGTask handler on launch

## Files Deleted
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/scheduler/PlaylistAutoRefreshScheduler.kt` -- replaced by BackgroundRefreshCoordinator + platform schedulers
- `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/data/scheduler/PlaylistAutoRefreshSchedulerTest.kt` -- replaced by BackgroundRefreshCoordinatorTest

## Deviations from Plan
- Added `@OptIn(ExperimentalForeignApi::class)` to `submitTaskRequest` in IOSBackgroundScheduler -- required by Kotlin/Native for NSDate.dateWithTimeIntervalSinceNow interop.
- Updated existing `SettingsViewModelTest.kt` to supply the two new constructor parameters (FakeBackgroundScheduler + BackgroundRefreshCoordinator) -- not mentioned in the plan but necessary for compilation.

## Build Status
- Android assembleDebug: Compiles successfully
- BackgroundRefreshCoordinator tests: All 8 pass
- SettingsViewModel tests: Pre-existing failures (15/23) due to missing Xcode/Looper environment -- not caused by this change
- iOS framework linking: Fails due to missing Xcode CLI tools on this machine -- not caused by this change; iOS Kotlin source compiles without errors

## Notes
- The KoinHelper.shared singleton is eagerly initialized; if Koin is not yet started when Swift code accesses it, it will throw. The plan accounts for this since Koin init happens in Application.onCreate (Android) / MainViewController init (iOS) before BGTask handlers fire.
- WorkManager's minimum interval is 15 minutes, matching the existing MIN_REFRESH_INTERVAL_SECONDS constant.
- The SettingsViewModel test failures are pre-existing and related to Android Looper not being available in host tests (Robolectric not configured).
