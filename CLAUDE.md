# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Project Overview

WhiteIPTVKMP is a Kotlin Multiplatform Mobile (KMM) project targeting Android and iOS platforms
using Compose Multiplatform for 100% shared UI code. The project uses the expect/actual pattern for
platform-specific implementations while maintaining a common codebase.

**Key Technologies:**

- Kotlin 2.2.21
- Compose Multiplatform 1.9.3
- Gradle 9.2.0
- Android Gradle Plugin 8.13.0

**Target Platforms:**

- Android: minSdk 24, targetSdk 36
- iOS: ARM64 (devices) and Simulator ARM64 (M1+ Macs)

## Build Commands

### Android

Build the Android app:

```bash
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:assembleRelease
```

Install and run on a connected device:

```bash
./gradlew installDebug
```

Build and run tests:

```bash
./gradlew :composeApp:testDebugUnitTest
./gradlew :composeApp:connectedDebugAndroidTest  # Requires connected device/emulator
```

### iOS

Build iOS framework:

```bash
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64    # For simulator
./gradlew :composeApp:linkReleaseFrameworkIosArm64           # For device
```

Run iOS tests:

```bash
./gradlew :composeApp:iosSimulatorArm64Test
```

For running the iOS app, open the `iosApp` directory in Xcode and run from there.

### Cross-Platform

Build everything:

```bash
./gradlew build
```

Run all tests:

```bash
./gradlew allTests
./gradlew check
```

Clean build artifacts:

```bash
./gradlew clean
```

## Code Quality

The project uses multiple lint tools to ensure code quality and consistency across all platforms.

### Lint Tools

**ktlint** - Kotlin code formatting and style:

- Enforces official Kotlin code style
- Auto-formats code according to .editorconfig
- Version: 1.7.1 (Gradle plugin: 13.1.0)

**Detekt** - Static code analysis:

- Checks code complexity, potential bugs, and code smells
- Includes Compose-specific rules (io.nlopez.compose.rules 0.4.27)
- Configuration: `config/detekt/detekt.yml`
- Version: 1.23.8

**Compose Lint** - Compose-specific checks:

- Android Lint rules for Jetpack Compose
- Slack compose-lint-checks 1.4.2
- Configuration: `composeApp/lint.xml`

### Commands

**Run all lint checks:**

```bash
./gradlew lintAll
```

**Format all Kotlin code:**

```bash
./gradlew formatAll
```

**Auto-fix what's possible:**

```bash
./gradlew lintFix
```

**Individual tools:**

```bash
# ktlint
./gradlew ktlintCheck        # Check code style
./gradlew ktlintFormat       # Auto-format code

# Detekt
./gradlew detekt             # Run static analysis
./gradlew detektBaseline     # Create baseline for existing issues

# Android Lint
./gradlew :composeApp:lint   # Run Android lint checks
```

### Git Hooks

The project includes a pre-commit hook that automatically:

1. Formats staged Kotlin files with ktlint
2. Runs Detekt static analysis
3. Re-stages formatted files

**To enable the hook:**

```bash
git config core.hooksPath .git-hooks
```

**To bypass (not recommended):**

```bash
git commit --no-verify
```

### Configuration Files

- `.editorconfig` - Code style settings (120 char line length, trailing commas, etc.)
- `config/detekt/detekt.yml` - Detekt rules configuration
- `composeApp/lint.xml` - Compose-specific lint rules
- `.git-hooks/pre-commit` - Pre-commit hook script

### Baseline Files

If you need to suppress existing issues temporarily:

```bash
./gradlew detektBaseline
```

This creates `config/detekt/baseline.xml` with current issues that will be ignored.

## Project Structure

### Module Organization

The project uses a single-module structure with platform-specific source sets:

```
composeApp/
├── src/
│   ├── commonMain/     # Shared code for all platforms
│   ├── androidMain/    # Android-specific implementations
│   └── iosMain/        # iOS-specific implementations
```

### Source Sets

**commonMain** - Platform-agnostic code:

- Uses Compose Multiplatform for UI (Material3)
- Contains business logic and shared models
- Defines `expect` declarations for platform-specific APIs

**androidMain** - Android implementations:

- `MainActivity.kt` - Entry point extending `ComponentActivity`
- `Platform.android.kt` - Android `actual` implementations
- Android resources (manifests, drawables, strings)

**iosMain** - iOS implementations:

- `MainViewController.kt` - Creates `UIViewController` for Compose UI
- `Platform.ios.kt` - iOS `actual` implementations

**iosApp** (separate Swift module):

- Native iOS app shell that hosts the Kotlin framework
- `ContentView.swift` - Bridges SwiftUI to Compose via `UIViewControllerRepresentable`

## Architecture Patterns

### Expect/Actual Pattern

Platform-specific code uses Kotlin's expect/actual mechanism:

```kotlin
// commonMain/Platform.kt
expect fun getPlatform(): Platform

// androidMain/Platform.android.kt
actual fun getPlatform(): Platform = AndroidPlatform()

// iosMain/Platform.ios.kt
actual fun getPlatform(): Platform = IOSPlatform()
```

When adding platform-specific functionality:

1. Define the `expect` declaration in `commonMain`
2. Provide `actual` implementations in each platform's source set
3. Call from common code without platform checks

### Compose Multiplatform UI

All UI is written in `commonMain` using Compose:

- Use Material3 components for consistency
- Compose state management with `remember` and `mutableStateOf`
- Platform-specific UI tweaks should be minimal; prefer common solutions

### iOS Bridge Pattern

The iOS app uses `UIViewControllerRepresentable` to bridge SwiftUI to the Compose framework:

- The Kotlin framework exposes `MainViewController()` function
- Swift calls this to get a `UIViewController` containing the Compose UI
- Framework name is `ComposeApp` (static framework)

## Gradle Configuration

The project uses Gradle version catalogs (`gradle/libs.versions.toml`) for dependency management.

### Build Configuration

**Optimization Settings** (gradle.properties):

- Configuration cache enabled: `org.gradle.configuration-cache=true`
- Build caching enabled: `org.gradle.caching=true`
- JVM heap: 4GB (`org.gradle.jvmargs=-Xmx4096M`)

### Framework Configuration

iOS framework settings in `composeApp/build.gradle.kts`:

- Framework name: `ComposeApp`
- Type: Static framework (`isStatic = true`)
- Targets: `iosArm64()` and `iosSimulatorArm64()`

## Development Guidelines

### Adding Dependencies

Add dependencies in `composeApp/build.gradle.kts`:

- Common dependencies → `commonMain.dependencies`
- Android-only → `androidMain.dependencies`
- iOS-only → `iosMain.dependencies`

Prefer adding dependencies to version catalog in `gradle/libs.versions.toml` first.

### Testing

Testing infrastructure is configured but tests need to be implemented:

- Common tests → `composeApp/src/commonTest/`
- Android tests → `composeApp/src/androidTest/`
- iOS tests → `composeApp/src/iosTest/`

Framework: `kotlin-test` for common tests, JUnit for Android, XCTest for iOS.

### Package Structure

Package name: `com.simplevideo.whiteiptv`

- Keep this consistent across all source sets
- Android applicationId: `com.simplevideo.whiteiptv`
- iOS bundle ID: `com.simplevideo.whiteiptv.WhiteIPTVKMP`

### Code Style

Project uses official Kotlin code style (`kotlin.code.style=official` in gradle.properties).

## iOS-Specific Notes

### Xcode Integration

The iOS app in `iosApp/` requires:

- Team ID configuration for code signing
- Xcode integration via `embedAndSignAppleFrameworkForXcode` Gradle task
- Framework embedding handled automatically during Xcode builds

### Running iOS App

Use Xcode for iOS development:

1. Open `iosApp/` directory in Xcode
2. Select target device/simulator
3. Run directly from Xcode

Xcode automatically invokes Gradle to build the Kotlin framework.

## Common Tasks

**Add new shared screen:**

1. Create Composable function in `composeApp/src/commonMain/kotlin/`
2. Use Material3 components
3. No platform-specific code needed for pure UI

**Add platform-specific feature:**

1. Define `expect` function in `commonMain`
2. Implement `actual` in `androidMain` and `iosMain`
3. Call from common code

**Update dependencies:**

1. Modify `gradle/libs.versions.toml`
2. Update version numbers
3. Sync Gradle
4. Run `./gradlew build` to verify
