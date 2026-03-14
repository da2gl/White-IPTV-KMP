# Code Report: App Version Single Source of Truth

## Files Created
None in source tree. `AppConfig.kt` is generated into `shared/build/generated/source/appconfig/commonMain/kotlin/` by Gradle at build time.

## Files Modified
- `gradle/libs.versions.toml` -- added `app-versionName = "1.0.0"` and `app-versionCode = "1"` to `[versions]`
- `shared/build.gradle.kts` -- added `generateAppConfig` task and registered output as commonMain source directory
- `androidApp/build.gradle.kts` -- replaced hardcoded `versionCode`/`versionName` with catalog reads
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/settings/SettingsViewModel.kt` -- replaced `APP_VERSION` constant with `AppConfig.VERSION_NAME`, removed the constant
- `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/feature/settings/SettingsViewModelTest.kt` -- replaced hardcoded `"1.0"` assertion with `AppConfig.VERSION_NAME`, added `FakeCacheManager` dependency
- `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/data/cache/FakeCacheManager.kt` -- fixed JVM declaration clash by renaming `cacheSizeBytes` property to `fakeCacheSizeBytes`

## Deviations from Plan
- **FakeCacheManager fix**: Another agent added a `CacheManager` dependency to `SettingsViewModel` (clear-cache feature). The `FakeCacheManager` had a JVM declaration clash (`cacheSizeBytes` property getter conflicted with `getCacheSizeBytes()` method). Renamed the property to `fakeCacheSizeBytes` and constructor parameter to `initialCacheSizeBytes` to fix the clash.
- **Test file changes**: The test file was already modified by another agent (clear-cache feature) with additional cache-related tests. Added `FakeCacheManager` import and wired it into `createViewModel()`.

## Build Status
Build and tests pass: `./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug`

## Notes
The generated `AppConfig.kt` is not checked into git -- it lives in `shared/build/`. The Gradle task dependency is wired via `srcDir(generateAppConfig.map { ... })`, ensuring the file is generated before compilation.
