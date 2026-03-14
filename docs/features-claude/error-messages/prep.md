# Improve Network Error Messages During Playlist Import -- Implementation Plan

## Summary

Replace generic error messages during playlist import with specific, user-friendly messages that tell the user what went wrong and what they can do about it. Currently, most errors surface as raw exception messages like "Network error occurred" or "Unexpected error during playlist import". The goal is to map each `PlaylistException` subtype to a clear, localized string resource displayed in the Onboarding screen.

## Decisions Made

### Decision 1: Error message mapping lives in the ViewModel, not in a separate mapper class
- **Decision**: The `OnboardingViewModel.importPlaylistFromSource()` method will use a `when` expression over `PlaylistException` subtypes to select user-friendly messages.
- **Rationale**: This is a simple mapping with 7 cases. A separate class would be over-engineering. The ViewModel already handles the error path.
- **Alternatives considered**: A dedicated `ErrorMessageMapper` class -- unnecessary complexity for a flat sealed class with no nesting.

### Decision 2: Use Compose string resources (not hardcoded strings)
- **Decision**: All error messages will be defined in `strings.xml` and referenced via `Res.string.*` identifiers. However, since `stringResource()` is a `@Composable` function, the ViewModel will store a sealed interface `ImportError` on the state instead of a raw `String`, and the Screen composable will resolve it to a localized string.
- **Rationale**: Proper i18n support. The existing `invalid_playlist_error` string resource shows this pattern is already in use. Storing a typed error on state (rather than a String) keeps the ViewModel free of Compose resource dependencies and makes testing straightforward.
- **Alternatives considered**: (a) Hardcoded English strings in ViewModel -- works but blocks future localization. (b) Pass `StringResource` IDs from ViewModel -- Compose resources use generated `Res.string.*` which are objects, not simple IDs, and accessing them from ViewModel without `@Composable` context requires workarounds. The sealed interface approach is cleaner.

### Decision 3: NetworkError gets sub-categorized based on cause exception
- **Decision**: `PlaylistException.NetworkError` will be further inspected by checking its `message` and `cause` to distinguish between: connection timeout, server not found (DNS), connection refused, HTTP error codes, and generic network failure.
- **Rationale**: The `ImportPlaylistUseCase` already sets specific messages on `NetworkError` (e.g., "Request timeout", "Connection failed", "Failed to download playlist: HTTP 404"). We can pattern-match on these messages in the ViewModel.
- **Alternatives considered**: Adding more `PlaylistException` subtypes (e.g., `TimeoutError`, `DnsError`) -- more invasive change that requires modifying the UseCase layer. The message-based approach works with the existing code.

### Decision 4: HTTP status codes get human-readable descriptions
- **Decision**: HTTP errors like 404, 403, 500 will be mapped to messages like "Playlist not found (404)", "Access denied (403)", "Server error (500)".
- **Rationale**: Users need to understand whether the issue is on their side (wrong URL) vs server side.
- **Alternatives considered**: Showing raw HTTP codes only -- not user-friendly enough.

## Current State

### Error flow today

1. `ImportPlaylistUseCase` (line 53-136) catches exceptions and wraps them as `PlaylistException` subtypes with technical messages.
2. `OnboardingViewModel.importPlaylistFromSource()` (line 58-73) catches errors and does:
   - `PlaylistException` -> `e.message ?: "Unknown error occurred"` (surfaces raw technical message)
   - Other exceptions -> `"Unexpected error: ${e.message}"` (completely generic)
3. `OnboardingState.error` is a `String?` displayed as red text in `OnboardingScreen` (line 222-226).

### Error messages currently produced by ImportPlaylistUseCase

| Exception | Message | File:Line |
|---|---|---|
| `NetworkError` | "Request timeout" | ImportPlaylistUseCase.kt:119 |
| `NetworkError` | "Connection failed. Check your internet connection" | ImportPlaylistUseCase.kt:131 |
| `NetworkError` | "Failed to download playlist: HTTP {code}" | ImportPlaylistUseCase.kt:150 |
| `InvalidUrl` | "Invalid playlist URL: {url}" | PlaylistException.kt:19 |
| `ParseError` | "Invalid M3U format: missing #EXTM3U header" | ImportPlaylistUseCase.kt:65 |
| `ParseError` | "Failed to parse M3U content" | ImportPlaylistUseCase.kt:75 |
| `EmptyPlaylist` | "Playlist is empty or contains no valid channels" | PlaylistException.kt:31 |
| `DatabaseError` | "Failed to check existing playlist" / "Failed to save playlist" | ImportPlaylistUseCase.kt:96,112 |
| `Unknown` | "Unexpected error during playlist import" | ImportPlaylistUseCase.kt:135 |

### String resources

File: `shared/src/commonMain/composeResources/values/strings.xml`
- Only one error string exists: `invalid_playlist_error` (line 11), and it is not currently used anywhere in the codebase.

## Changes Required

### New Files

#### 1. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/onboarding/mvi/ImportError.kt`

Purpose: Typed sealed interface representing user-facing import errors, stored in `OnboardingState` instead of raw `String`.

```kotlin
package com.simplevideo.whiteiptv.feature.onboarding.mvi

/**
 * Represents user-facing import error types.
 * Resolved to localized strings in the UI layer.
 */
sealed interface ImportError {
    /** No internet connection or connection refused */
    data object NoConnection : ImportError

    /** DNS resolution failed -- server/domain not found */
    data object ServerNotFound : ImportError

    /** Request timed out */
    data object Timeout : ImportError

    /** HTTP error with status code (e.g., 404, 403, 500) */
    data class HttpError(val statusCode: Int) : ImportError

    /** Invalid URL format */
    data object InvalidUrl : ImportError

    /** File is not valid M3U/M3U8 format */
    data object InvalidFormat : ImportError

    /** Playlist parsed but contains no channels */
    data object EmptyPlaylist : ImportError

    /** Database/storage failure */
    data object StorageError : ImportError

    /** Catch-all for unexpected errors */
    data class Unknown(val detail: String? = null) : ImportError
}
```

### Modified Files

#### 1. `shared/src/commonMain/composeResources/values/strings.xml`

**What changes**: Add error message string resources.

**New strings to add** (after the existing `invalid_playlist_error` line):

```xml
<string name="error_no_connection">No internet connection. Check your network settings and try again.</string>
<string name="error_server_not_found">Server not found. Check the playlist URL and try again.</string>
<string name="error_timeout">Connection timed out. The server took too long to respond.</string>
<string name="error_http_404">Playlist not found. The URL may be incorrect or the playlist was removed.</string>
<string name="error_http_403">Access denied. The server rejected the request.</string>
<string name="error_http_500">Server error. Try again later.</string>
<string name="error_http_generic">Server returned error %d. Try again later.</string>
<string name="error_invalid_url">Invalid URL. Enter a valid playlist link starting with http:// or https://</string>
<string name="error_invalid_format">Invalid playlist format. The file is not a valid M3U playlist.</string>
<string name="error_empty_playlist">The playlist contains no channels.</string>
<string name="error_storage">Failed to save playlist. Please try again.</string>
<string name="error_unknown">Something went wrong. Please try again.</string>
```

**Why**: Localized, user-friendly error messages. The existing `invalid_playlist_error` string is unused and can be kept for backward compatibility or removed (decision: keep it, zero risk).

#### 2. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/onboarding/mvi/OnboardingMvi.kt`

**What changes**: Change `OnboardingState.error` from `String?` to `ImportError?`.

```kotlin
// Before
data class OnboardingState(
    val isLoading: Boolean = false,
    val error: String? = null,
    ...
)

// After
data class OnboardingState(
    val isLoading: Boolean = false,
    val error: ImportError? = null,
    ...
)
```

**Why**: Typed errors enable the UI to resolve localized strings via `stringResource()` and allow ViewModel tests to assert on error types without depending on string content.

#### 3. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/onboarding/OnboardingViewModel.kt`

**What changes**: Replace the `onFailure` block in `importPlaylistFromSource()` with a `when` expression that maps `PlaylistException` subtypes to `ImportError` values.

Replace lines 66-71 with:

```kotlin
.onFailure { e ->
    val importError = mapToImportError(e)
    viewState = viewState.copy(isLoading = false, error = importError)
}
```

Add new private method:

```kotlin
private fun mapToImportError(e: Throwable): ImportError {
    return when (e) {
        is PlaylistException.InvalidUrl -> ImportError.InvalidUrl
        is PlaylistException.ParseError -> ImportError.InvalidFormat
        is PlaylistException.EmptyPlaylist -> ImportError.EmptyPlaylist
        is PlaylistException.DatabaseError -> ImportError.StorageError
        is PlaylistException.NotFound -> ImportError.StorageError
        is PlaylistException.NetworkError -> mapNetworkError(e)
        is PlaylistException.Unknown -> ImportError.Unknown(e.message)
        else -> ImportError.Unknown(e.message)
    }
}

private fun mapNetworkError(e: PlaylistException.NetworkError): ImportError {
    val message = e.message ?: ""
    return when {
        message.contains("timeout", ignoreCase = true) -> ImportError.Timeout
        message.contains("HTTP") -> {
            val code = Regex("HTTP (\\d{3})").find(message)?.groupValues?.get(1)?.toIntOrNull()
            if (code != null) ImportError.HttpError(code) else ImportError.NoConnection
        }
        else -> ImportError.NoConnection
    }
}
```

Also update `handleEnterPlaylistUrl()` and `handleFileSelected()` to clear error as `error = null` (no type change needed since it is already nullable).

**Why**: Maps technical exceptions to typed user-facing errors. The `mapNetworkError` inspects the message string set by `ImportPlaylistUseCase` to sub-categorize network errors without modifying the UseCase.

#### 4. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/onboarding/OnboardingScreen.kt`

**What changes**: Replace the raw `state.error` text display with a composable that resolves `ImportError` to a localized string.

Replace lines 222-226:

```kotlin
// Before
} else if (state.error != null) {
    Text(
        text = state.error,
        color = MaterialTheme.colorScheme.error,
    )
}

// After
} else if (state.error != null) {
    Text(
        text = resolveErrorMessage(state.error),
        color = MaterialTheme.colorScheme.error,
    )
}
```

Add a new `@Composable` function in the same file:

```kotlin
@Composable
private fun resolveErrorMessage(error: ImportError): String {
    return when (error) {
        is ImportError.NoConnection -> stringResource(Res.string.error_no_connection)
        is ImportError.ServerNotFound -> stringResource(Res.string.error_server_not_found)
        is ImportError.Timeout -> stringResource(Res.string.error_timeout)
        is ImportError.HttpError -> when (error.statusCode) {
            404 -> stringResource(Res.string.error_http_404)
            403 -> stringResource(Res.string.error_http_403)
            500 -> stringResource(Res.string.error_http_500)
            else -> stringResource(Res.string.error_http_generic, error.statusCode)
        }
        is ImportError.InvalidUrl -> stringResource(Res.string.error_invalid_url)
        is ImportError.InvalidFormat -> stringResource(Res.string.error_invalid_format)
        is ImportError.EmptyPlaylist -> stringResource(Res.string.error_empty_playlist)
        is ImportError.StorageError -> stringResource(Res.string.error_storage)
        is ImportError.Unknown -> stringResource(Res.string.error_unknown)
    }
}
```

Add new imports for the string resources (Res.string.error_*).

Also update the Preview composables that pass `error = "Invalid playlist format"` to use `error = ImportError.InvalidFormat` instead.

**Why**: Composable function has access to `stringResource()` for proper localization.

### Database Changes

None.

### DI Changes

None.

## Implementation Order

1. **Add string resources** -- `shared/src/commonMain/composeResources/values/strings.xml`. Add all error_* string entries. No dependencies.

2. **Create `ImportError` sealed interface** -- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/onboarding/mvi/ImportError.kt`. No dependencies.

3. **Update `OnboardingState`** -- Change `error: String?` to `error: ImportError?` in `OnboardingMvi.kt`.

4. **Update `OnboardingViewModel`** -- Replace error mapping logic in `importPlaylistFromSource()`. Add `mapToImportError()` and `mapNetworkError()` methods. Import `ImportError`.

5. **Update `OnboardingScreen`** -- Add `resolveErrorMessage()` composable. Update error display. Update Preview composables. Add string resource imports.

6. **Build and verify** -- Run `./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug` to confirm compilation.

7. **Write tests** for ViewModel error mapping.

## Testing Strategy

### Unit Tests: `OnboardingViewModelTest`

File: `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/feature/onboarding/OnboardingViewModelTest.kt`

Test the `mapToImportError` logic indirectly through `importPlaylistFromSource`:

- **Test: network timeout maps to `ImportError.Timeout`** -- Mock `ImportPlaylistUseCase` to throw `PlaylistException.NetworkError("Request timeout")`. Assert `viewState.error` is `ImportError.Timeout`.
- **Test: connection error maps to `ImportError.NoConnection`** -- Throw `PlaylistException.NetworkError("Connection failed. Check your internet connection")`. Assert `ImportError.NoConnection`.
- **Test: HTTP 404 maps to `ImportError.HttpError(404)`** -- Throw `PlaylistException.NetworkError("Failed to download playlist: HTTP 404")`. Assert `ImportError.HttpError(404)`.
- **Test: invalid URL maps to `ImportError.InvalidUrl`** -- Throw `PlaylistException.InvalidUrl("bad-url")`. Assert `ImportError.InvalidUrl`.
- **Test: parse error maps to `ImportError.InvalidFormat`** -- Throw `PlaylistException.ParseError()`. Assert `ImportError.InvalidFormat`.
- **Test: empty playlist maps to `ImportError.EmptyPlaylist`** -- Throw `PlaylistException.EmptyPlaylist()`. Assert `ImportError.EmptyPlaylist`.
- **Test: database error maps to `ImportError.StorageError`** -- Throw `PlaylistException.DatabaseError()`. Assert `ImportError.StorageError`.
- **Test: unknown exception maps to `ImportError.Unknown`** -- Throw `RuntimeException("something broke")`. Assert `ImportError.Unknown`.
- **Test: successful import clears error** -- Set error state first, then successful import. Assert `error` is null.

**Coroutine test pattern**: Use `runTest` with `Dispatchers.setMain(UnconfinedTestDispatcher())` for ViewModel tests. The `ImportPlaylistUseCase` is a suspend function injected via constructor, so mock it directly (e.g., create a fake implementation or use a lambda-based approach).

### Edge Cases

- `PlaylistException.NetworkError` with null message -- should fall through to `ImportError.NoConnection`.
- `PlaylistException.NetworkError` with message "HTTP" but no valid code -- should map to `ImportError.NoConnection`.
- Non-`PlaylistException` throwable (e.g., `CancellationException`) -- note that `runCatching` catches `CancellationException` which is a known issue. This plan does not address that (separate concern), but `Unknown` is the fallback.

## Doc Updates Required

- `docs/constraints/current-limitations.md` -- No update needed. Error messages are not listed as a current limitation there.
- `docs/features/onboarding.md` -- Does not exist currently, no update needed.

> [!NOTE] Implementation pending -- No doc updates until the feature is implemented and verified.

## Build & Test Commands

```bash
# Compile check
./gradlew :shared:testAndroidHostTest :androidApp:assembleDebug

# Run specific test class (after tests are written)
./gradlew :shared:testAndroidHostTest --tests "com.simplevideo.whiteiptv.feature.onboarding.OnboardingViewModelTest"

# Full lint check
./gradlew formatAll
```
