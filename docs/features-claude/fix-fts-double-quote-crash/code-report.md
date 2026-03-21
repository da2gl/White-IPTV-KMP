# Code Report: Fix FTS Double-Quote Crash

## Files Created
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/repository/FtsQuerySanitizer.kt` -- Extracted FTS query sanitizer as a standalone utility object. Strips all FTS4 operator characters (double quotes, asterisks, parentheses, colons, plus signs, etc.) and FTS keywords (AND, OR, NOT, NEAR) from user input. Returns null when sanitized query is empty, allowing callers to skip the FTS query entirely.
- `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/data/repository/FtsQuerySanitizerTest.kt` -- 26 unit tests covering: plain text passthrough, individual special character stripping, FTS keyword removal, empty/null handling, edge cases (whitespace-only, keyword-only, complex malicious input).

## Files Modified
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/repository/ChannelRepositoryImpl.kt` -- Replaced inline `sanitizeFtsQuery()` private method with calls to `FtsQuerySanitizer.sanitize()`. All 11 search methods now handle null returns (empty after sanitization) by returning `flowOf(emptyList())`, `emptyList()`, or `0` instead of passing empty strings to FTS MATCH which would crash.
- `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/feature/settings/SettingsViewModelTest.kt` -- Fixed pre-existing compilation errors: removed references to `FakeCacheManager` and `cacheSize` (not yet in SettingsViewModel), simplified cache test to match current ViewModel behavior.
- `docs/features/search.md` -- Updated line 41 to accurately describe that special characters are stripped/sanitized by FtsQuerySanitizer before the MATCH expression, not "treated as literals due to double-quote escaping".

## Deviations from Plan
No prep.md existed for this feature. Implementation was based on:
1. Analyzing the existing `sanitizeFtsQuery()` in ChannelRepositoryImpl
2. Identifying the root causes: (a) incomplete character stripping, (b) no handling of empty results after sanitization, (c) missing FTS keyword stripping
3. Extracting to a testable utility object with comprehensive coverage

The SettingsViewModelTest fix was necessary to get the test suite to compile -- it was broken on master with references to a `cacheManager` parameter and `cacheSize` state field that don't exist in the current SettingsViewModel.

## Build Status
Build passes and all tests pass (330 tests, 0 failures).

## Notes
- The sanitizer uses a character Set instead of regex for stripping operator characters, avoiding regex escaping issues in Kotlin raw strings
- FTS keywords (AND, OR, NOT, NEAR) are stripped with word-boundary regex to avoid removing substrings from normal words (e.g., "android" and "notification" are preserved)
- When sanitization produces an empty string (e.g., user types only `"`), the repository returns empty results instead of crashing with a malformed MATCH expression
