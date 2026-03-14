# Validation: Continue Watching

## Status: REWORK NEEDED

## Checklist

### Plan vs Implementation

- [x] `data/local/model/WatchHistoryEntity.kt` — created, matches plan exactly (channelId PK, FK CASCADE, index on lastWatchedAt)
- [x] `data/local/WatchHistoryDao.kt` — created, matches plan (upsert, Flow-based JOIN query, single lookup, clearAll)
- [x] `domain/repository/WatchHistoryRepository.kt` — created, matches plan interface
- [x] `data/repository/WatchHistoryRepositoryImpl.kt` — created, matches plan (Clock.System timestamps, delegates to DAO)
- [x] `domain/usecase/RecordWatchEventUseCase.kt` — created, matches plan (delegates to repository, default durationMs=0)
- [x] `data/local/AppDatabase.kt` — WatchHistoryEntity added to entities, version bumped 2->3, watchHistoryDao() added
- [x] `data/local/DatabaseBuilder.kt` — MIGRATION_2_3 added with correct DDL, applied via .addMigrations()
- [x] `domain/usecase/GetContinueWatchingUseCase.kt` — stub replaced with repository-backed impl, RECENT_LIMIT=10, progress=0f, timeLeft=""
- [x] `di/KoinModule.kt` — WatchHistoryDao (single), WatchHistoryRepositoryImpl (single bind), RecordWatchEventUseCase (factory) all registered
- [x] `feature/player/PlayerViewModel.kt` — RecordWatchEventUseCase added, watch tracking with initial record, 30s timer, stop on pause/channel-switch, onCleared() cleanup
- [x] `feature/home/HomeScreen.kt` — ContinueWatchingItem now clickable with onClick lambda, LinearProgressIndicator and timeLeft removed
- [x] No extra files or changes beyond the plan

### Code Quality

- [x] MVI pattern followed correctly — PlayerViewModel uses State/Event/Action, watch tracking integrates via existing event handlers
- [x] UseCase pattern followed — RecordWatchEventUseCase and GetContinueWatchingUseCase both follow invoke() convention, registered as factory
- [x] Koin registrations present and correct — DAO (single), repository (single bind), UseCase (factory)
- [x] No new navigation routes needed (uses existing Player route)
- [x] No `Dispatchers.IO` in new commonMain code (DatabaseBuilder.kt uses Dispatchers.IO but this is pre-existing and runs in platform-specific context)
- [x] Error handling — PlayerViewModel uses catch {} on channel flow; RecordWatchEventUseCase is fire-and-forget from viewModelScope.launch (acceptable for non-critical watch tracking)
- [x] No hardcoded strings that should be resources — "Continue Watching" section title is in HomeScreen.kt:377 but this matches pre-existing pattern in the codebase
- [x] Timer resource management — watchTimerJob cancelled on channel switch, pause, and onCleared()
- [x] ExperimentalTime opt-in correctly applied where Clock.System is used

### Test Coverage

- [x] RecordWatchEventUseCaseTest — 5 tests covering delegation, default params, retrieval, multiple channels, upsert
- [x] GetContinueWatchingUseCaseTest — 8 tests covering empty state, mapping, progress=0f, timeLeft="", ordering, limit 10, top 10 selection, metadata preservation
- [x] WatchHistoryRepositoryImplTest — 9 tests covering CRUD, upsert, ordering, limit, empty state, single lookup
- [x] Flaky test fixed (same-millisecond non-determinism in ordering test — changed to controlled timestamps)
- [x] FakeWatchHistoryDao and FakeWatchHistoryRepository are well-structured test doubles
- [x] Edge cases tested: empty history, upsert same channel, limit enforcement, null lookups
- [ ] **Missing: PlayerViewModel test coverage** — No tests for watch tracking integration (recordInitialWatchEvent, startWatchTimer, stopWatchTimer, onCleared cancellation). However, this is a pre-existing gap — PlayerViewModel had no tests before this feature. Noted but not blocking.

### Documentation

- [ ] `docs/features/home.md:22` — Still says "Time indicator (e.g., '24m left', '1h 15m left')" but implementation removed the progress bar and time indicator for live IPTV. **Must be updated.**
- [ ] `docs/constraints/current-limitations.md:23-27` — "Continue Watching returns empty" section still present. **Must be removed** since the feature is now implemented.

### Build & Lint

- [x] Code-report confirms all Continue Watching files compile successfully (KSP generated WatchHistoryDao_Impl.kt)
- [ ] Full `assembleDebug` fails due to **unrelated** Search Enhancement coder's changes (PlaylistDao search methods with incomplete KSP generation). Not a Continue Watching issue.
- [x] Test-report confirms all 13 new tests + 9 pre-existing tests pass (22 total)
- [x] Code was formatted (linter ran as part of pipeline)

### E2E Testing

- [x] Skipped — no emulator available. Noted as limitation.

## Rework Required

### 1. Documentation Updates

- **Who**: coder
- **What**: Update two documentation files:
  1. `docs/features/home.md:19-22` — Replace the Continue Watching card description. Remove "Time indicator (e.g., '24m left', '1h 15m left')" bullet. The card now shows only channel logo/thumbnail and channel name (no progress bar or time indicator for live IPTV).
  2. `docs/constraints/current-limitations.md:23-29` — Remove the entire "Continue Watching returns empty" section (lines 22-29 including the `---` separator above it), since the feature is now implemented.
- **Why**: The plan explicitly listed these as "Doc Updates Required". Implementation changed the UI (removed progress bar), but docs still describe the old behavior. Current-limitations still lists Continue Watching as unimplemented.
- **Acceptance**: `docs/features/home.md` Continue Watching section mentions only logo + name (no time indicator), and `docs/constraints/current-limitations.md` has no "Continue Watching" section.

## Summary

The Continue Watching implementation is high quality and faithfully follows the plan. All 5 new files and 6 modified files match the spec. The database migration is correct and additive. Watch tracking in PlayerViewModel properly handles initial record, periodic updates, pause/stop recording, and cleanup. Tests are comprehensive with 22 passing tests covering all new UseCases and the repository. The only gap is documentation — two doc files need minor updates as specified in the plan's own "Doc Updates Required" section. This is a small rework that should take minutes.
