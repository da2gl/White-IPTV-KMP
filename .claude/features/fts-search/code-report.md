# Code Report: FTS4 Full-Text Search for Channels

## Files Created
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/model/ChannelFtsEntity.kt` -- FTS4 virtual table entity with `@Fts4(contentEntity = ChannelEntity::class)` annotation, indexing only the `name` column.

## Files Modified
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/AppDatabase.kt` -- Added `ChannelFtsEntity::class` to entities array, bumped version from 4 to 5.
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/DatabaseBuilder.kt` -- Added `MIGRATION_4_5` that creates the FTS4 virtual table and backfills from existing channel data. Added migration to `addMigrations()` call.
- `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/PlaylistDao.kt` -- Rewrote 5 search queries to use FTS4 JOIN with phrase-prefix matching (`MATCH '"' || :query || '*'`). Added 3 FTS helper methods (`insertChannelFts`, `deleteAllChannelFts`, `deleteChannelFtsByPlaylistId`). Updated `importPlaylistWithData()` and `updatePlaylistWithData()` to maintain FTS table consistency.
- `shared/src/commonTest/kotlin/com/simplevideo/whiteiptv/data/local/FakePlaylistDao.kt` -- Added stub implementations for the 3 new FTS methods.
- `docs/features/search.md` -- Updated to describe FTS4-backed search, documented behavioral differences (word-boundary tokenization, prefix matching, special-character sanitization).
- `docs/constraints/current-limitations.md` -- Added entry about FTS4 word-boundary constraint.

## Build Status
Compiles successfully. All existing unit tests pass.

## Notes
- The FTS4 MATCH query uses double-quote escaping (`'"' || :query || '*'`) to treat user input as a literal phrase prefix. This prevents FTS operator injection from special characters in search queries.
- The `deleteChannelFtsByPlaylistId` is called before `deleteChannelsByPlaylistId` in `updatePlaylistWithData` because it needs to look up channel IDs from the channels table to know which FTS rows to delete.
- FTS content table entries may become orphaned when playlists are deleted via CASCADE, but the JOIN-based queries ensure orphaned entries are never returned. The FTS table is rebuilt on the next playlist import/update.
