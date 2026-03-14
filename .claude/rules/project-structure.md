# Project Structure Rules

## Module Names

- `shared/` — KMP shared library (NOT `composeApp/`)
- `androidApp/` — Android app entry point
- `iosApp/` — iOS Xcode project

**NEVER** reference `composeApp/` — it was renamed to `shared/`.

## Build Commands

```bash
./gradlew :androidApp:assembleDebug              # Build Android app
./gradlew :shared:testAndroidHostTest             # Run unit tests
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64  # Build iOS framework
./gradlew formatAll                               # Lint + format
```

## Source Sets (shared module)

| Source Set | Purpose |
|-----------|---------|
| `commonMain` | Shared code for all platforms |
| `androidMain` | Android actual implementations |
| `iosMain` | iOS actual implementations |
| `commonTest` | Shared tests |
| `androidHostTest` | Android-only unit tests |
| `iosTest` | iOS-only tests |

## Feature Plans & Reports

All implementation plans and reports go to `docs/features-claude/<feature-name>/`.
