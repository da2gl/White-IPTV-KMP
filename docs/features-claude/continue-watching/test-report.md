# Test Report: Continue Watching

## Test Summary
- Tests written: 13 (5 RecordWatchEventUseCase + 8 GetContinueWatchingUseCase)
- Tests passing: 13
- Tests failing: 0
- Pre-existing tests fixed: 1 (flaky ordering test in WatchHistoryRepositoryImplTest)

## Test Coverage
| Component | Tests | Key Scenarios |
|-----------|-------|--------------|
| RecordWatchEventUseCase | 5 | delegates to repository, default duration=0, records retrievable entry, multiple channels, upsert same channel |
| GetContinueWatchingUseCase | 8 | empty history, maps to ContinueWatchingItem, progress=0f for live, timeLeft empty, recency order, limit 10, most recent in top 10, preserves channel metadata |
| WatchHistoryRepositoryImpl | 9 (pre-existing) | recordWatchEvent CRUD, upsert, ordering, limit, empty state, single lookup |

## Files Created
| File | Description |
|------|-------------|
| `commonTest/.../domain/usecase/FakeWatchHistoryRepository.kt` | Fake repository for use case tests with call tracking |
| `commonTest/.../domain/usecase/RecordWatchEventUseCaseTest.kt` | 5 tests for record watch event delegation |
| `commonTest/.../domain/usecase/GetContinueWatchingUseCaseTest.kt` | 8 tests for continue watching flow mapping |

## Files Fixed
| File | Fix |
|------|-----|
| `commonTest/.../data/repository/WatchHistoryRepositoryImplTest.kt` | Fixed flaky `getRecentlyWatchedChannels returns channels in recency order` test — was using `recordWatchEvent` (Clock.System timestamps) causing same-millisecond non-determinism. Changed to use DAO directly with controlled timestamps. Added `WatchHistoryEntity` import. |
| `commonTest/.../domain/usecase/FakeWatchHistoryRepository.kt` | Added `@OptIn(ExperimentalTime::class)` for `Clock.System` usage |

## Security Review
| Check | Status | Notes |
|-------|--------|-------|
| SQL injection | ✅ | All queries use Room `@Query` with parameterized `:variables`. No string concatenation in SQL. Migration uses static DDL only. |
| Input validation | ✅ | `channelId` and `playlistId` are `Long` types enforced by Kotlin type system. No user-supplied strings in queries. |
| Data exposure | ✅ | Watch history contains only channel IDs and timestamps — no sensitive user data. No logging of watch history data. |
| DB migration safety | ✅ | Migration v2→v3 is additive only (`CREATE TABLE IF NOT EXISTS`, `CREATE INDEX IF NOT EXISTS`). No data loss risk. FK CASCADE correctly references `channels(id)`. |
| Path traversal | ✅ | N/A — no file operations in this feature. |
| XSS in WebView | ✅ | N/A — no web content displayed. |
| Insecure HTTP | ✅ | N/A — no new network calls. Stream URLs are from existing channel data. |
| Timer resource leak | ✅ | `watchTimerJob` is cancelled on channel switch, pause, and `onCleared()`. No leak risk. |

## Issues Found
- **Severity**: minor
- **Location**: `WatchHistoryRepositoryImplTest.kt:72`
- **Description**: Pre-existing flaky test used `Clock.System.now()` timestamps via `repository.recordWatchEvent()` for three sequential calls. When all three execute within the same millisecond, sort order becomes non-deterministic.
- **Resolution**: Fixed by using DAO directly with controlled timestamps (100L, 200L, 300L).

## Verdict
✅ PASS — ready for lint
