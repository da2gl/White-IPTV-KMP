# Code Report: Error Messages

## Files Created
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/onboarding/mvi/ImportError.kt` -- Sealed interface representing user-facing import error types (9 subtypes)
- `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/feature/onboarding/OnboardingViewModelTest.kt` -- Unit tests for error mapping logic (10 tests)

## Files Modified
- `shared/src/commonMain/composeResources/values/strings.xml` -- Added 12 error message string resources (error_no_connection, error_server_not_found, error_timeout, error_http_404, error_http_403, error_http_500, error_http_generic, error_invalid_url, error_invalid_format, error_empty_playlist, error_storage, error_unknown)
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/onboarding/mvi/OnboardingMvi.kt` -- Changed `OnboardingState.error` type from `String?` to `ImportError?`
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/onboarding/OnboardingViewModel.kt` -- Replaced raw string error handling with `mapToImportError()` and `mapNetworkError()` methods that map PlaylistException subtypes to ImportError sealed interface values
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/feature/onboarding/OnboardingScreen.kt` -- Added `resolveErrorMessage()` composable that maps ImportError to localized strings; updated preview composables to use `ImportError.InvalidFormat` instead of raw string

## Deviations from Plan
- The linter changed the HTTP 500 case in `resolveErrorMessage` from exact match `500` to range `in 500..599`, which is a reasonable improvement.
- Tests use `FileSelected` event (local file import path) instead of URL-based import, because the test `FileReader` throws controlled exceptions. The URL path would attempt real HTTP calls via HttpClient.

## Build Status
Compiles and tests pass.

## Notes
- The `formatAll` task fails due to a pre-existing detekt config issue (misspelled Compose rule properties), unrelated to this feature.
- The existing `invalid_playlist_error` string resource was kept for backward compatibility as planned.
