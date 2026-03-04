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
- Gradle 9.2.1
- Android Gradle Plugin 9.0.1

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

### Lint Tools

**ktlint** (1.7.1, Gradle plugin 14.0.1) - Kotlin code formatting per `.editorconfig`.

**Detekt** (1.23.8) - Static analysis with Compose-specific rules (io.nlopez.compose.rules
0.4.27). Config: `config/detekt/detekt.yml`.

### Commands

```bash
./gradlew formatAll            # Run ktlintFormat + detektFormat (use before committing)

./gradlew ktlintCheck          # Check code style
./gradlew ktlintFormat         # Auto-format code

./gradlew detekt               # Run static analysis
./gradlew detektFormat         # Auto-fix issues (unused imports, etc.)
./gradlew detektBaseline       # Create baseline for existing issues
```

### Git Hooks

Pre-commit hook formats staged Kotlin files with ktlint and runs Detekt.

```bash
git config core.hooksPath .git-hooks    # Enable
git commit --no-verify                  # Bypass (not recommended)
```

### Configuration Files

- `.editorconfig` - Code style (120 char line length, trailing commas, 4-space indent)
- `config/detekt/detekt.yml` - Detekt rules
- `config/detekt/baseline.xml` - Suppressed existing issues (regenerate with `./gradlew detektBaseline`)
- `.git-hooks/pre-commit` - Pre-commit hook script

## Project Structure

Single-module KMM project with platform-specific source sets:

```
composeApp/src/
├── commonMain/kotlin/com/simplevideo/whiteiptv/
│   ├── App.kt                     # Root composable
│   ├── di/                        # Koin modules (KoinModule.kt, KoinInitializer.kt)
│   ├── navigation/                # Type-safe routes (Route.kt, NavGraph.kt)
│   ├── domain/
│   │   ├── model/                 # Business objects (PlaylistSource, ChannelsFilter, ChannelGroup)
│   │   ├── repository/            # Repository interfaces
│   │   ├── usecase/               # Business logic (11 UseCases)
│   │   └── exception/             # PlaylistException sealed class
│   ├── data/
│   │   ├── local/                 # Room database, entities, DAOs
│   │   ├── repository/            # Repository implementations
│   │   ├── mapper/                # Domain <-> Entity transformations
│   │   ├── parser/playlist/       # M3U/M3U8 parser
│   │   └── network/               # Ktor HTTP client
│   ├── feature/                   # Presentation layer (MVI screens)
│   │   ├── splash/                # Initial loading
│   │   ├── onboarding/            # Playlist import
│   │   ├── main/                  # Tab container
│   │   ├── home/                  # Home tab
│   │   ├── channels/              # Channel list tab
│   │   ├── favorites/             # Favorites tab
│   │   ├── player/                # Video player + components/
│   │   └── settings/              # Settings tab
│   ├── platform/                  # expect declarations (FileReader, FilePicker, VideoPlayerFactory)
│   ├── designsystem/              # Material3 theme (Theme.kt, Color.kt, Typography.kt)
│   └── common/                    # BaseViewModel, AppLogger, shared UI components
├── androidMain/                   # Android actual implementations
│   ├── MainActivity.kt
│   ├── di/PlatformModule.kt
│   └── platform/
│       ├── AndroidFileReader.kt, AndroidFilePickerFactory.kt
│       ├── AndroidSystemControls.kt, KeepScreenOn.android.kt
│       └── exoplayer/             # ExoPlayer integration (5 files)
└── iosMain/                       # iOS actual implementations
    ├── MainViewController.kt
    ├── di/PlatformModule.kt
    └── platform/
        ├── IOSFileReader.kt, IOSFilePickerFactory.kt
        ├── IOSVideoPlayerFactory.kt, IOSSystemControls.kt
        └── KeepScreenOn.ios.kt
```

`iosApp/` - Native iOS app shell (SwiftUI bridge to Compose via `UIViewControllerRepresentable`).

## Architecture Patterns

### Clean Architecture

```
Presentation (feature/)  →  Domain (domain/)  →  Data (data/)
   ViewModels, Screens       UseCases, Models     Repositories, DB, Network
```

- **Domain layer** has no dependencies on Data or Presentation
- **UseCases** encapsulate business logic; ViewModels delegate to them
- **Repository interfaces** in domain/, implementations in data/
- **Mappers** transform between domain models and database entities

### MVI (Model-View-Intent)

Each feature follows the State/Event/Action pattern:

```kotlin
data class XxxState(...)          // Immutable UI state, observed via collectAsState()
sealed interface XxxEvent { ... } // User interactions, sent via viewModel.obtainEvent()
sealed interface XxxAction { ... }// One-time side effects, consumed via LaunchedEffect

class XxxViewModel(
    private val useCase: SomeUseCase
) : BaseViewModel<XxxState, XxxAction, XxxEvent>(initialState = XxxState()) {
    override fun obtainEvent(viewEvent: XxxEvent) { ... }
}
```

`BaseViewModel` manages `MutableStateFlow<State>` for continuous observation and
`MutableSharedFlow<Action?>` (replay=1) for one-time effects. Use `viewState` property to mutate
state and `viewAction` to emit actions. Call `clearAction()` after consuming.

### Navigation

Type-safe routes using Kotlin Serialization (`navigation/Route.kt`):

```
Splash → (Onboarding | Main)
Main contains tabs: Home | Favorites | Channels(groupId?) | Settings
Main → Player(channelId: Long)
```

`PopUpTo` clears back stack on Splash→Onboarding and Onboarding→Main transitions.

### UseCase Pattern

```kotlin
class ImportPlaylistUseCase(
    private val repository: PlaylistRepository,
    private val httpClient: HttpClient,
    private val fileReader: FileReader,
    private val channelMapper: ChannelMapper,
    private val playlistMapper: PlaylistMapper,
) {
    suspend operator fun invoke(source: PlaylistSource) { ... }
}
```

Guidelines:

- One UseCase per business operation
- Accept domain models, return domain models, throw domain exceptions
- Use `Dispatchers.Default` for CPU-intensive work (KMP-compatible)
- Never use `Dispatchers.IO` (not available in KMP); Ktor and Room manage threads internally
- Registered as `factory` in Koin (stateless, new instance per injection)

### Sealed Interface Pattern

Used for type-safe polymorphism with exhaustive `when` expressions:

```kotlin
sealed interface PlaylistSource {
    data class Url(val url: String) : PlaylistSource
    data class LocalFile(val uri: String, val fileName: String) : PlaylistSource
}
```

Prefer sealed interfaces over enums when subtypes carry different data.

### Error Handling

`PlaylistException` sealed class hierarchy for business-specific errors:

- `NetworkError` - HTTP/connectivity failures
- `InvalidUrl` - URL validation
- `ParseError` - M3U format issues
- `EmptyPlaylist` - no channels found
- `NotFound` - playlist lookup failures
- `DatabaseError` - persistence failures
- `Unknown(message, cause)` - catch-all

Use `runCatching` for functional error handling in UseCases.

### Expect/Actual Pattern

Platform-specific code uses Kotlin's expect/actual mechanism:

```kotlin
// commonMain - define expect
interface FileReader { suspend fun readFile(uri: String): String }

// androidMain - actual implementation
class AndroidFileReader(val context: Context) : FileReader { ... }

// iosMain - actual implementation
class IOSFileReader : FileReader { ... }
```

For lifecycle-aware components, use Composable expect/actual:

```kotlin
@Composable expect fun rememberFilePicker(): FilePicker
```

When adding platform-specific functionality:

1. Define the `expect` declaration in `commonMain/platform/`
2. Provide `actual` implementations in `androidMain/platform/` and `iosMain/platform/`
3. Register in `platformModule()` (both Android and iOS)

## Video Player Architecture

The player uses a platform-abstracted interface in `commonMain/platform/`:

```kotlin
interface VideoPlayer {
    fun play(), pause(), stop(), release()
    fun setMediaSource(url: String, userAgent: String?, referer: String?)
    fun isPlaying(): Boolean
    fun getCurrentLiveOffset(): Long
    fun seekToLiveEdge()
    fun addListener(listener: PlayerListener)
    fun getTracksInfo(): TracksInfo
    fun selectAudioTrack(trackId: String?)
    fun selectSubtitleTrack(trackId: String?)
    fun selectVideoQuality(qualityId: String?)
    @Composable fun PlayerView(modifier: Modifier)
}

interface PlayerListener {
    fun onPlaybackStateChanged(isPlaying: Boolean, isBuffering: Boolean)
    fun onError(errorCode: Int, errorMessage: String)
    fun onTracksChanged(tracksInfo: TracksInfo) {}
}

interface VideoPlayerFactory {
    fun createPlayer(): VideoPlayer
}
```

**Android:** ExoPlayer/Media3 implementation in `androidMain/platform/exoplayer/`:

- `ExoPlayerFactory` - creates optimized player with Cronet network stack (HTTP/2, QUIC)
- `ExoVideoPlayer` - implements VideoPlayer interface
- `ExoPlayerComponentFactory` - configures LoadControl, TrackSelector, AudioAttributes
- `DataSourceFactoryProvider` - HTTP datasources with custom User-Agent/Referer headers
- `TracksInfoMapper` - converts ExoPlayer tracks to unified TrackInfo format

**iOS:** AVPlayer implementation placeholder in `iosMain/platform/IOSVideoPlayerFactory.kt`.

**PlayerConfig** (`commonMain/platform/PlayerConfig.kt`) provides IPTV-optimized presets:

- `Default` - balanced buffering (10-30s)
- `LowLatency` - low-delay live (3s buffer)
- `HighBuffer` - unstable networks (30-60s)

## Database

Room database (version 2) with bundled SQLite for cross-platform support.

**Entities:**

- `PlaylistEntity` - playlist metadata (id, url, name, channelCount, importedAt)
- `ChannelEntity` - channel info (id, playlistId, name, url, logo, tvgId, isFavorite, etc.)
- `ChannelGroupEntity` - group metadata (id, name, displayOrder, channelCount)
- `ChannelGroupCrossRef` - many-to-many junction table (channelId ↔ groupId)

**Schema export:** `composeApp/schemas/` (for migration versioning)

**Key DAO patterns:**

- Flow-based queries for reactive UI updates
- Suspend functions for one-time operations
- `@Transaction` methods for atomic batch operations (importing channels + groups + cross-refs)

**Platform initialization:** `expect/actual` via `AppDatabaseConstructor`:

- Android: `getDatabaseBuilder(context)` with `applicationContext`
- iOS: `getDatabaseBuilder()` using `NSHomeDirectory()`

## Repository Interfaces

**PlaylistRepository** - playlist CRUD plus transactional import:

- `importPlaylistData()` - atomic insert of playlist + channels + groups + cross-refs
- `updatePlaylistData()` - atomic update preserving favorites
- Flow-based `getPlaylists()` for reactive playlist list

**ChannelRepository** - channel queries and favorites:

- Flow-based queries: `getAllChannels()`, `getChannelsByPlaylistId()`, `getChannelsByGroupId()`
- Favorites: `getFavoriteChannels()`, `toggleFavoriteStatus()`
- Groups: `getAllGroups()`, `getTopGroups()`, `getRandomChannelsByGroupId()`

**CurrentPlaylistRepository** - shared state via `MutableStateFlow<PlaylistSelection>`:

- Coordinates playlist selection between HomeScreen and ChannelsScreen
- Single source of truth for which playlist is currently active

## Dependency Injection

Koin DI with 7 modules defined in `di/KoinModule.kt`:

| Module | Scope | Contents |
|---|---|---|
| `viewModelModule` | viewModel | 6 ViewModels (Splash, Onboarding, Home, Favorites, Channels, Player) |
| `repositoryModule` | single | PlaylistRepositoryImpl, ChannelRepositoryImpl, CurrentPlaylistRepository |
| `mapperModule` | factory | ChannelMapper, ChannelGroupMapper, PlaylistMapper |
| `useCaseModule` | factory | 11 UseCases |
| `networkModule` | single | Ktor HttpClient |
| `databaseModule` | single | PlaylistDao (from AppDatabase) |
| `platformModule()` | mixed | expect/actual: AppDatabase, FileReader, VideoPlayerFactory, FilePickerFactory |

**Scope rules:** `single` for stateful (repositories, DB, network), `factory` for stateless
(UseCases, mappers), `viewModel` for lifecycle-aware ViewModels.

## Development Guidelines

### Adding Dependencies

1. Add version to `gradle/libs.versions.toml`
2. Add library/plugin reference in the same file
3. Use in `composeApp/build.gradle.kts`: `commonMain.dependencies`, `androidMain.dependencies`, or
   `iosMain.dependencies`

### Package Structure

Package: `com.simplevideo.whiteiptv` (consistent across all source sets)

- Android applicationId: `com.simplevideo.whiteiptv`
- iOS bundle ID: `com.simplevideo.whiteiptv.WhiteIPTVKMP`

### Code Style

- Official Kotlin code style (`kotlin.code.style=official`)
- Line length: 120 characters
- Trailing commas: enabled
- Run `./gradlew formatAll` before committing
- Use `Dispatchers.Default` over `Dispatchers.IO` (KMP compatibility)
- Use sealed interfaces over enums for polymorphism
- Use `runCatching` for functional error handling

### Comments and Documentation

**Class-level KDoc** - Required for UseCases, public interfaces, complex algorithms.

**Inline comments** - Only for business logic explanation, non-obvious technical decisions,
platform-specific workarounds.

**Avoid**: decorative separators, obvious comments, implementation detail comments, section headers
for small files.

### iOS Bridge

The iOS app uses `UIViewControllerRepresentable` to bridge SwiftUI to Compose:

- Kotlin exposes `MainViewController()` → Swift wraps it in `ComposeView`
- Framework name: `ComposeApp` (static framework)
- Xcode automatically invokes Gradle to build the Kotlin framework
- Run iOS app from Xcode (open `iosApp/` directory)

## Product Documentation

Product specification lives in `docs/` using a modular structure (rendered via Docsify):

- `docs/domain/` — business entities (Playlist, Channel, ChannelGroup)
- `docs/features/` — screen specs (Home, Favorites, Channels, Search, Player, EPG, Settings, Playlist Settings)
- `docs/flows/` — user flows (Import Playlist, Watch Channel)
- `docs/constraints/` — platform info, current limitations, open questions

**Read before implementing features.** Each feature doc is the source of truth for behavior. `docs/constraints/current-limitations.md` is the backlog of unimplemented features.

## Agent Pipeline

The project uses a multi-agent pipeline for feature development (`.claude/PIPELINE.md`):

Custom agents in `.claude/agents/`: preparer, doc-validator, coder, tester, linter, validator.

Feature state is tracked in `.claude/features/<feature-name>/` with artifacts from each pipeline step (prep.md, code-report.md, test-report.md, validation.md).

## Common Tasks

**Add new MVI screen:**

1. Create `feature/myfeature/mvi/MyFeatureMvi.kt` with State, Event, Action
2. Create `feature/myfeature/MyFeatureViewModel.kt` extending `BaseViewModel`
3. Create `feature/myfeature/MyFeatureScreen.kt` observing state and actions
4. Register ViewModel in `viewModelModule` (`viewModelOf(::MyFeatureViewModel)`)
5. Add route to `Route.kt` and navigation entry in `NavGraph.kt`

**Add new UseCase:**

1. Create class in `domain/usecase/` with `suspend operator fun invoke()`
2. Register in `useCaseModule` (`factoryOf(::MyUseCase)`)
3. Inject into ViewModel constructor

**Add new domain model:**

1. Create model in `domain/model/` (prefer sealed interfaces for polymorphism)
2. Create corresponding entity in `data/local/model/`
3. Create mapper in `data/mapper/`, register in `mapperModule` (`factoryOf(::MyMapper)`)

**Add platform-specific feature:**

1. Define interface + `expect` in `commonMain/platform/`
2. Implement `actual` in `androidMain/platform/` and `iosMain/platform/`
3. Register in `platformModule()` for both platforms
4. For lifecycle-aware components, use `@Composable expect fun remember...()`
