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
- Android Gradle Plugin 8.13.1

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

### Commands

**Format all Kotlin code and auto-fix issues (including unused imports):**

```bash
./gradlew formatAll
```

**Individual tools:**

```bash
# ktlint
./gradlew ktlintCheck        # Check code style
./gradlew ktlintFormat       # Auto-format code

# Detekt
./gradlew detekt             # Run static analysis
./gradlew detektFormat       # Auto-fix issues (unused imports, etc.)
./gradlew detektBaseline     # Create baseline for existing issues
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

### Clean Architecture

The project follows Clean Architecture principles with clear separation of concerns:

**Domain Layer** (`domain/`):

- **Models**: Pure business objects (Channel, Playlist, PlaylistSource)
- **UseCases**: Business logic encapsulation (ImportPlaylistUseCase)
- **Repository Interfaces**: Abstract data access contracts
- **Exceptions**: Business-specific exceptions (PlaylistException)

**Data Layer** (`data/`):

- **Repository Implementations**: Concrete data access (PlaylistRepositoryImpl)
- **Mappers**: Transform between domain and data models (ChannelMapper, PlaylistMapper)
- **Local**: Room database entities and DAOs
- **Parser**: M3U playlist parsing logic
- **Network**: HTTP client configuration

**Presentation Layer** (`feature/`):

- **ViewModels**: MVI pattern with State/Action/Event
- **Screens**: Compose UI components
- **MVI**: State management pattern files

**Platform Layer** (`platform/`):

- **Interfaces**: Platform-specific contracts (FilePicker, FileReader)
- **Implementations**: expect/actual platform code

### UseCase Pattern

UseCases encapsulate business logic and are reusable across the app:

```kotlin
class ImportPlaylistUseCase(
    private val repository: PlaylistRepository,
    private val httpClient: HttpClient,
    private val fileReader: FileReader,
    private val channelMapper: ChannelMapper,
    private val playlistMapper: PlaylistMapper,
) {
    suspend operator fun invoke(source: PlaylistSource) {
        // Business logic here
    }
}
```

Guidelines:

- One UseCase per business operation
- Accept domain models as parameters
- Return domain models or throw domain exceptions
- Use `Dispatchers.Default` for CPU-intensive operations (KMP-compatible)
- Network I/O: Ktor manages threads internally
- Database I/O: Room manages threads internally

### Sealed Interface Pattern

Use sealed interfaces for polymorphic business logic:

```kotlin
sealed interface PlaylistSource {
    data class Url(val url: String) : PlaylistSource
    data class LocalFile(val uri: String, val fileName: String) : PlaylistSource
}

// Usage in UseCase
when (source) {
    is PlaylistSource.Url -> downloadFromUrl(source.url)
    is PlaylistSource.LocalFile -> fileReader.readFile(source.uri)
}
```

Benefits:

- Type-safe polymorphism
- Exhaustive when expressions
- Reduces code duplication
- Clear business intent

### MVI Architecture

ViewModels follow the MVI (Model-View-Intent) pattern:

**State**: Immutable data class representing UI state

```kotlin
data class OnboardingState(
    val playlistUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
```

**Event**: User interactions from UI

```kotlin
sealed interface OnboardingEvent {
    data class EnterPlaylistUrl(val url: String) : OnboardingEvent
    data object ImportPlaylist : OnboardingEvent
}
```

**Action**: One-time side effects (navigation, show picker)

```kotlin
sealed interface OnboardingAction {
    data object NavigateToMain : OnboardingAction
    data object ShowFilePicker : OnboardingAction
}
```

Guidelines:

- State is observed continuously via `collectAsState()`
- Events are sent from UI via `viewModel.obtainEvent()`
- Actions are consumed once via `LaunchedEffect`
- Keep business logic in UseCases, not ViewModels

### Mapper Pattern

Mappers transform between layers:

```kotlin
class ChannelMapper {
    fun toEntityList(
        playlistId: Long,
        channels: List<Channel>, // Domain model
        favoritesTvgIds: Set<String?> = emptySet(),
        favoritesUrls: Set<String> = emptySet()
    ): List<ChannelEntity> { // Data model
        // Transformation logic
    }
}
```

Guidelines:

- One mapper per domain model
- Use `Dispatchers.Default` for large collections (1000+ items)
- Preserve user preferences (favorites) during updates
- Registered as `factory` in Koin (stateless)

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

For platform-specific implementations that need lifecycle awareness, use Composable functions:

```kotlin
// commonMain
@Composable
expect fun rememberFilePicker(): FilePicker

// androidMain - uses rememberLauncherForActivityResult
@Composable
actual fun rememberFilePicker(): FilePicker {
    val launcher = rememberLauncherForActivityResult(...)
    return remember { AndroidFilePicker(launcher) }
}

// iosMain - uses UIDocumentPickerViewController
@Composable
actual fun rememberFilePicker(): FilePicker {
    return remember { IOSFilePicker() }
}
```

When adding platform-specific functionality:

1. Define the `expect` declaration in `commonMain`
2. Provide `actual` implementations in each platform's source set
3. Call from common code without platform checks
4. Use Composable expect/actual for lifecycle-aware components

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

## Dependency Injection

The project uses Koin for dependency injection with multiple modules:

### Module Structure

```kotlin
// viewModelModule - ViewModel instances
viewModelOf(::SplashViewModel)
viewModelOf(::OnboardingViewModel)

// repositoryModule - Repository implementations (singleton)
singleOf(::PlaylistRepositoryImpl) bind PlaylistRepository::class

// mapperModule - Data mappers (factory)
factoryOf(::ChannelMapper)
factoryOf(::PlaylistMapper)

// useCaseModule - Business logic (factory)
factoryOf(::ImportPlaylistUseCase)

// networkModule - HTTP client (singleton)
single { HttpClientFactory.create() }

// databaseModule - Room database and DAOs (singleton)
single { get<AppDatabase>().playlistDao() }

// platformModule() - Platform-specific dependencies
expect fun platformModule(): Module
```

### Scopes

- **single**: Singleton instance (repositories, database, network client)
- **factory**: New instance per injection (UseCases, mappers)
- **viewModel**: Lifecycle-aware ViewModel instance

### Platform Module

Platform-specific dependencies are defined via expect/actual:

**Android** (`androidMain/di/PlatformModule.kt`):

```kotlin
actual fun platformModule(): Module = module {
    single<AppDatabase> { getDatabaseBuilder(get()) }
    single<FileReader> { AndroidFileReader(get()) }
}
```

**iOS** (`iosMain/di/PlatformModule.kt`):

```kotlin
actual fun platformModule(): Module = module {
    single<AppDatabase> { getDatabaseBuilder() }
    single<FileReader> { IOSFileReader() }
}
```

### Adding New Dependencies

1. Determine the appropriate module (business logic → useCaseModule, data access → repositoryModule)
2. Choose scope: `single` for stateful, `factory` for stateless
3. Add to the relevant module in `KoinModule.kt`
4. For platform-specific: add to `platformModule()` in both Android and iOS

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

### Comments and Documentation

**Class-level KDoc** - Required for:

- UseCases: Explain business purpose and usage
- Public interfaces: Document contract and platform considerations
- Complex algorithms: M3U parser, batch processing, etc.

**Inline comments** - Only for:

- Business logic explanation (EPG, catchup URLs, User-Agent overrides)
- Non-obvious technical decisions (why Dispatchers.Default, batch size reasoning)
- Platform-specific workarounds

**Avoid**:

- Decorative separators (`// ═══════════`)
- Obvious comments (`// Get channel name` above `channel.name`)
- Implementation details (`// Use repository to insert`)
- Section headers for small files

**Example - Good comments**:

```kotlin
/**
 * Use case for importing IPTV playlist from URL or local file
 *
 * Handles:
 * - URL validation and download OR file reading
 * - M3U parsing
 * - Batch insertion for large playlists (50K+ channels)
 * - Preserving user favorites during updates
 */
class ImportPlaylistUseCase { ... }

// CPU-intensive: parse M3U with 50K+ channels, move to Default dispatcher
withContext(Dispatchers.Default) {
    M3uParser.parse(m3uString)
}

// Preserve favorites by matching tvgId or URL
val favoritesTvgIds = existingChannels.filter { it.isFavorite }
```

**Example - Bad comments**:

```kotlin
// ═══════════ Playlist Operations ═══════════

// Insert playlist
repository.insertPlaylist(playlist)

// Get channels
val channels = repository.getChannels(id)
```

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

**Add new UseCase:**

1. Create class in `domain/usecase/`:

```kotlin
class MyBusinessUseCase(
    private val repository: MyRepository,
    // other dependencies
) {
    suspend operator fun invoke(param: DomainModel): Result {
        // Business logic
    }
}
```

2. Register in `useCaseModule`:

```kotlin
val useCaseModule = module {
    factoryOf(::MyBusinessUseCase)
}
```

3. Inject into ViewModel constructor

**Add new MVI screen:**

1. Create MVI structure in `feature/myfeature/mvi/`:

```kotlin
// State
data class MyFeatureState(
    val data: String = "",
    val isLoading: Boolean = false
)

// Event
sealed interface MyFeatureEvent {
    data class OnAction(val value: String) : MyFeatureEvent
}

// Action
sealed interface MyFeatureAction {
    data object Navigate : MyFeatureAction
}
```

2. Create ViewModel extending `BaseViewModel`:

```kotlin
class MyFeatureViewModel(
    private val useCase: MyBusinessUseCase
) : BaseViewModel<MyFeatureState, MyFeatureAction, MyFeatureEvent>(
    initialState = MyFeatureState()
) {
    override fun obtainEvent(viewEvent: MyFeatureEvent) {
        when (viewEvent) {
            is MyFeatureEvent.OnAction -> handleAction(viewEvent.value)
        }
    }
}
```

3. Create Composable screen observing state and actions
4. Register ViewModel in `viewModelModule`

**Add new domain model:**

1. Create sealed interface/data class in `domain/model/`:

```kotlin
sealed interface MyDomainModel {
    data class TypeA(val data: String) : MyDomainModel
    data class TypeB(val value: Int) : MyDomainModel
}
```

2. Create corresponding entity in `data/local/model/`
3. Create mapper in `data/mapper/`:

```kotlin
class MyDomainModelMapper {
    fun toEntity(model: MyDomainModel): MyEntity {
        ...
    }
    fun toDomain(entity: MyEntity): MyDomainModel {
        ...
    }
}
```

4. Register mapper in `mapperModule`

**Add platform-specific feature:**

For simple functions:

1. Define `expect` function in `commonMain/platform/`
2. Implement `actual` in `androidMain/platform/` and `iosMain/platform/`

For lifecycle-aware components:

1. Define interface in `commonMain/platform/`:

```kotlin
interface MyPlatformFeature {
    fun doSomething()
}

@Composable
expect fun rememberMyPlatformFeature(): MyPlatformFeature
```

2. Implement `actual` Composable in both platforms
3. Use lifecycle hooks (`rememberLauncherForActivityResult`, etc.)

**Add sealed interface for polymorphism:**

When you have similar logic with different data sources:

1. Create sealed interface in `domain/model/`:

```kotlin
sealed interface DataSource {
    data class Remote(val url: String) : DataSource
    data class Local(val path: String) : DataSource
    data class Cache(val key: String) : DataSource
}
```

2. Use in UseCase with `when` expression:

```kotlin
when (source) {
    is DataSource.Remote -> fetchFromNetwork(source.url)
    is DataSource.Local -> readFromFile(source.path)
    is DataSource.Cache -> getFromCache(source.key)
}
```

**Update dependencies:**

1. Modify `gradle/libs.versions.toml`
2. Update version numbers
3. Sync Gradle
4. Run `./gradlew build` to verify

**Code style guidelines:**

1. Run `./gradlew ktlintFormat` before committing
2. Keep comments focused on business logic or complex operations
3. Remove decorative comments (separators, obvious descriptions)
4. Use sealed interfaces over enums for polymorphism
5. Use `runCatching` for functional error handling
6. Prefer `Dispatchers.Default` over `Dispatchers.IO` (KMP compatibility)
