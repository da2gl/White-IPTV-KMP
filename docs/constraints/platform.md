# Platform

The application targets Android and iOS using Kotlin Multiplatform with Compose Multiplatform for shared UI.

## Supported Platforms

- **Android**: minSdk 24 (Android 7.0), targetSdk 36. All device form factors.
- **iOS**: ARM64 devices and Simulator ARM64 (M1+ Macs). iPhone and iPad.

## Implementation Status

- **Android**: Fully functional with ExoPlayer for video playback.
- **iOS**: UI and data layer complete. Video player is a placeholder (AVPlayer integration planned).

## Technology Stack

- Kotlin 2.2.21
- Compose Multiplatform 1.9.3
- Gradle 9.2.1 / AGP 9.0.1
- Room Database (bundled SQLite)
- Ktor HTTP client
- Koin DI

## Platform-Specific Components

| Component | Android | iOS |
|-----------|---------|-----|
| Video Player | ExoPlayer (Media3) with Cronet | AVPlayer (planned) |
| File Picker | System file picker via Intent | UIDocumentPickerViewController |
| File Reader | ContentResolver | NSFileManager |
| Keep Screen On | WindowManager flags | UIApplication.isIdleTimerDisabled |
| System Controls | AudioManager, WindowManager | MPVolumeView, UIScreen |
| Database | Room with applicationContext | Room with NSHomeDirectory |

## Design

- Dark-first design with light theme and system theme support.
- Material 3 design system.
- Portrait and landscape orientation support.
