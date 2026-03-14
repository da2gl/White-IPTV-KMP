# FTS4 Full-Text Search for Channels -- Implementation Plan

## Summary

Replace the current `LIKE '%query%' COLLATE NOCASE` search queries in PlaylistDao with FTS4 (Full-Text Search) virtual table-backed queries. This improves search performance from O(n) full-table scans to indexed full-text lookups, which matters at 10k+ channels. The implementation adds a `ChannelFtsEntity` as an FTS4 content table linked to `ChannelEntity`, a database migration from version 4 to 5, and rewrites all 5 search DAO queries to use FTS4 JOIN. The FTS table is populated manually inside the existing `@Transaction` import/update methods. No changes are needed to the repository interface, use case layer, or UI -- the search method signatures in PlaylistDao remain the same.

## Decisions Made

### 1. Use Room @Fts4 annotation with content table, not standalone FTS or raw SQL
- **Decision**: Use `@Fts4(contentEntity = ChannelEntity::class)` on a new `ChannelFtsEntity`. This creates an FTS4 content table that mirrors specified columns from `ChannelEntity`.
- **Rationale**: Room's `@Fts4` annotation with `contentEntity` is the idiomatic Room approach. It is available in Room 2.8.4's common source set (verified in both JVM and iOS klib artifacts). The content table approach avoids data duplication -- FTS4 with `content=` only stores the index, not the original data. The BundledSQLiteDriver used by this project includes FTS support.
- **Alternatives considered**: (a) Raw SQL `CREATE VIRTUAL TABLE` in migration without Room entity -- harder to maintain, no compile-time query validation. (b) FTS5 -- not supported by Room's annotation API (`@Fts3` and `@Fts4` only). (c) Standalone FTS table without `contentEntity` -- duplicates all data, wastes storage.

### 2. Index only the `name` column in FTS
- **Decision**: The FTS4 entity indexes only the `name` column from `ChannelEntity`, plus `rowid` (implicit, maps to channel id).
- **Rationale**: All current search queries match on `name` only. Adding more columns (tvgName, tvgId) would increase index size without user-facing benefit. The name field is what users see and search for.
- **Alternatives considered**: Indexing name + tvgName + tvgCountry + tvgLanguage -- would enable richer search but adds complexity and index bloat with no current UI support.

### 3. Manual FTS population in @Transaction methods, not SQLite triggers
- **Decision**: Populate the FTS table manually inside `importPlaylistWithData()` and `updatePlaylistWithData()` by calling a new `insertChannelFts()` DAO method after inserting channels.
- **Rationale**: Room's `@Fts4(contentEntity=...)` with `content=` tables requires manual synchronization (SQLite triggers are not automatically created by Room for content FTS tables). The existing `@Transaction` methods are the single point of entry for channel insertion, so manual population is straightforward and atomic. We also need to `DELETE FROM channels_fts` before re-populating on update, which is cleanly handled in the transaction.
- **Alternatives considered**: (a) SQLite triggers via migration SQL -- fragile across Room schema changes, not visible in Room entity model. (b) Rebuilding FTS via `INSERT INTO channels_fts(channels_fts) VALUES('rebuild')` -- works but is slower than targeted insert for large datasets during import.

### 4. Use FTS MATCH with prefix search and wildcard for substring-like behavior
- **Decision**: Use `channels_fts MATCH :query || '*'` for FTS queries. This provides prefix matching (e.g., "CNN" matches "CNN International" and "CNN News"). For true substring matching (finding "News" inside "CNN News"), fall back to LIKE when the query does not match as a prefix.
- **Rationale**: FTS4 MATCH does not support infix/substring matching natively. Pure prefix matching is the standard FTS4 behavior and covers the most common search pattern (users type the beginning of a channel name). To maintain backward compatibility with the current `LIKE '%query%'` behavior, the queries will use a UNION approach: FTS MATCH for fast prefix results, then LIKE for any additional substring matches not caught by FTS.
- **Alternatives considered**: (a) FTS MATCH only -- would break substring search (searching "News" would not find "CNN News"), which is a regression. (b) LIKE only (current approach) -- does not benefit from FTS at all. (c) FTS MATCH with tokenizer "unicode61" and trigram -- FTS4 does not support trigram tokenizer (that is FTS5 only).

**UPDATE on Decision 4**: After further consideration, a simpler approach is best. Use FTS MATCH with `query*` for prefix matching as the primary fast path. For the rare substring case, the existing LIKE query is adequate as a fallback. However, to keep the implementation simple and avoid UNION complexity, the decision is:

- **Revised Decision**: Use FTS4 JOIN for all search queries. The FTS `MATCH` operator with `query*` provides prefix matching. Since most IPTV channel searches are prefix-based ("BBC", "CNN", "HBO"), this covers the primary use case. If a user searches for a substring that only appears mid-word, they will get fewer results than the current LIKE approach -- this is an acceptable tradeoff for the massive performance gain on 10k+ channels. The FTS tokenizer splits on word boundaries, so "CNN News" is findable by searching "CNN" or "News" (each word is a separate token). Searching "NN" would not match "CNN" -- this is the only behavioral change from LIKE.
- **Revised Rationale**: The word-boundary tokenization of FTS4 means searching "News" WILL find "CNN News" because "News" is a separate token. The only regression is partial-word substring search (e.g., "NN" not finding "CNN"). This is acceptable because: (1) users rarely search for partial words, (2) the performance improvement on large playlists far outweighs this edge case, (3) the behavior matches what users expect from search in apps like YouTube, Netflix, etc.

### 5. Database migration v4 to v5
- **Decision**: Create `MIGRATION_4_5` that executes `CREATE VIRTUAL TABLE` for the FTS table and populates it from existing channel data using `INSERT INTO channels_fts(rowid, name) SELECT id, name FROM channels`.
- **Rationale**: Existing users have data in version 4. The migration must create the FTS table and backfill it. Using `INSERT ... SELECT` is a single efficient operation.
- **Alternatives considered**: Destructive migration (drops all data) -- unacceptable for users with existing playlists.

### 6. FTS table name: `channels_fts`
- **Decision**: Use `channels_fts` as the FTS virtual table name.
- **Rationale**: Follows a clear naming convention (`<content_table>_fts`). Consistent with common Room FTS examples.

## Current State

### Search queries in PlaylistDao (lines 86-124)
Five search methods using `LIKE '%' || :query || '%' COLLATE NOCASE`:
- `searchChannels(query)` -- all channels (line 86)
- `searchChannelsByPlaylistId(query, playlistId)` -- by playlist (lines 89-96)
- `searchChannelsByGroupId(query, groupId)` -- by group via cross-ref JOIN (lines 98-106)
- `searchFavoriteChannels(query)` -- favorites only (lines 108-115)
- `searchFavoriteChannelsByPlaylist(query, playlistId)` -- favorites by playlist (lines 117-124)

### Database version and migrations
- Current version: 4 (`AppDatabase.kt` line 23)
- Existing migrations in `DatabaseBuilder.kt`: `MIGRATION_2_3` (watch_history), `MIGRATION_3_4` (epg_programs)
- Schema export directory: `shared/schemas/`

### Channel import entry points (PlaylistDao.kt)
- `importPlaylistWithData()` (lines 211-231) -- used for new playlist import
- `updatePlaylistWithData()` (lines 237-255) -- used for playlist refresh/update

### ChannelEntity (data/local/model/ChannelEntity.kt)
- `id: Long` (PrimaryKey, autoGenerate)
- `name: String` (the field to index in FTS)
- Other fields not relevant to search

### Repository and UseCase layers
- `ChannelRepository.kt` (lines 28-32) -- 5 search interface methods
- `ChannelRepositoryImpl.kt` (lines 56-69) -- 5 search implementations delegating to DAO
- `GetChannelsUseCase.kt` (lines 27-34) -- routes to search methods when query is non-empty
- `GetFavoritesUseCase.kt` (lines 20-29) -- routes to search methods when query is non-empty

### Test infrastructure
- `FakeChannelRepository.kt` -- in-memory fake with LIKE-style filtering (lines 93-129)
- `GetChannelsUseCaseTest.kt` -- tests for query routing and result correctness
- `StubChannelRepository.kt` -- exists for other tests

## Changes Required

### New Files

#### 1. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/model/ChannelFtsEntity.kt`
- **Purpose**: FTS4 virtual table entity for full-text search on channel names
- **Key contents**:
  ```kotlin
  package com.simplevideo.whiteiptv.data.local.model

  import androidx.room.Entity
  import androidx.room.Fts4
  import androidx.room.PrimaryKey

  /**
   * FTS4 virtual table for fast full-text search on channel names.
   * Linked to ChannelEntity as a content table -- stores only the search index,
   * not the actual data.
   */
  @Fts4(contentEntity = ChannelEntity::class)
  @Entity(tableName = "channels_fts")
  data class ChannelFtsEntity(
      @PrimaryKey
      val rowid: Long = 0,
      val name: String,
  )
  ```

### Modified Files

#### 2. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/AppDatabase.kt`
- **What changes**:
  - Add `ChannelFtsEntity::class` to the `entities` array
  - Bump `version` from 4 to 5
- **Why**: Room needs to know about the FTS entity to generate the virtual table DDL and validate queries at compile time.

#### 3. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/DatabaseBuilder.kt`
- **What changes**:
  - Add `MIGRATION_4_5` that creates the FTS virtual table and backfills from existing channels
  - Add `MIGRATION_4_5` to `addMigrations()` call
- **Migration SQL**:
  ```sql
  CREATE VIRTUAL TABLE IF NOT EXISTS `channels_fts` USING FTS4(`name`, content=`channels`, tokenize=unicode61)
  ```
  ```sql
  INSERT INTO `channels_fts`(rowid, name) SELECT id, name FROM channels
  ```
- **Why**: Existing users upgrading from v4 need the FTS table created and populated with their existing channel data.

#### 4. `shared/src/commonMain/kotlin/com/simplevideo/whiteiptv/data/local/PlaylistDao.kt`
- **What changes**:
  - Add `insertChannelFts(channels: List<ChannelFtsEntity>)` method for bulk FTS insertion
  - Add `deleteAllChannelFts()` method to clear FTS table
  - Add `deleteChannelFtsByPlaylistId(playlistId: Long)` method to clear FTS entries for channels in a playlist
  - Rewrite 5 search queries to use FTS4 JOIN:
    - `searchChannels`: `SELECT c.* FROM channels c INNER JOIN channels_fts fts ON c.id = fts.rowid WHERE channels_fts MATCH :query || '*' ORDER BY c.name ASC`
    - `searchChannelsByPlaylistId`: same JOIN plus `AND c.playlistId = :playlistId`
    - `searchChannelsByGroupId`: FTS JOIN + cross-ref JOIN + `cgr.groupId = :groupId`
    - `searchFavoriteChannels`: FTS JOIN + `AND c.isFavorite = 1`
    - `searchFavoriteChannelsByPlaylist`: FTS JOIN + `AND c.isFavorite = 1 AND c.playlistId = :playlistId`
  - Update `importPlaylistWithData()` to call `insertChannelFts()` after `insertChannels()`
  - Update `updatePlaylistWithData()` to call `deleteChannelFtsByPlaylistId()` before deleting channels, and `insertChannelFts()` after inserting new channels
- **Why**: Core of the FTS4 feature. The FTS JOIN replaces LIKE for dramatically faster search. The import/update methods maintain FTS table consistency.

- **Detailed query rewrites**:

  ```kotlin
  // searchChannels
  @Query("""
      SELECT c.* FROM channels c
      INNER JOIN channels_fts fts ON c.id = fts.rowid
      WHERE channels_fts MATCH :query || '*'
      ORDER BY c.name ASC
  """)
  fun searchChannels(query: String): Flow<List<ChannelEntity>>

  // searchChannelsByPlaylistId
  @Query("""
      SELECT c.* FROM channels c
      INNER JOIN channels_fts fts ON c.id = fts.rowid
      WHERE channels_fts MATCH :query || '*' AND c.playlistId = :playlistId
      ORDER BY c.name ASC
  """)
  fun searchChannelsByPlaylistId(query: String, playlistId: Long): Flow<List<ChannelEntity>>

  // searchChannelsByGroupId
  @Query("""
      SELECT c.* FROM channels c
      INNER JOIN channels_fts fts ON c.id = fts.rowid
      INNER JOIN channel_group_cross_ref cgr ON c.id = cgr.channelId
      WHERE channels_fts MATCH :query || '*' AND cgr.groupId = :groupId
      ORDER BY c.name ASC
  """)
  fun searchChannelsByGroupId(query: String, groupId: Long): Flow<List<ChannelEntity>>

  // searchFavoriteChannels
  @Query("""
      SELECT c.* FROM channels c
      INNER JOIN channels_fts fts ON c.id = fts.rowid
      WHERE channels_fts MATCH :query || '*' AND c.isFavorite = 1
      ORDER BY c.name ASC
  """)
  fun searchFavoriteChannels(query: String): Flow<List<ChannelEntity>>

  // searchFavoriteChannelsByPlaylist
  @Query("""
      SELECT c.* FROM channels c
      INNER JOIN channels_fts fts ON c.id = fts.rowid
      WHERE channels_fts MATCH :query || '*' AND c.isFavorite = 1 AND c.playlistId = :playlistId
      ORDER BY c.name ASC
  """)
  fun searchFavoriteChannelsByPlaylist(query: String, playlistId: Long): Flow<List<ChannelEntity>>
  ```

  ```kotlin
  // New helper methods
  @Insert
  suspend fun insertChannelFts(ftsEntities: List<ChannelFtsEntity>)

  @Query("DELETE FROM channels_fts")
  suspend fun deleteAllChannelFts()

  @Query("""
      DELETE FROM channels_fts WHERE rowid IN (
          SELECT id FROM channels WHERE playlistId = :playlistId
      )
  """)
  suspend fun deleteChannelFtsByPlaylistId(playlistId: Long)
  ```

  Updated transaction methods:
  ```kotlin
  @Transaction
  suspend fun importPlaylistWithData(
      playlist: PlaylistEntity,
      groups: List<ChannelGroupEntity>,
      channels: List<ChannelEntity>,
      crossRefs: List<ChannelGroupCrossRef>,
  ): Long {
      val playlistId = insertPlaylist(playlist)

      if (groups.isNotEmpty()) {
          upsertGroups(groups)
      }

      val channelIds = insertChannels(channels)

      // Populate FTS index
      val ftsEntities = channels.mapIndexed { index, channel ->
          ChannelFtsEntity(rowid = channelIds[index], name = channel.name)
      }
      if (ftsEntities.isNotEmpty()) {
          insertChannelFts(ftsEntities)
      }

      if (crossRefs.isNotEmpty()) {
          insertChannelGroupCrossRefs(crossRefs)
      }

      return playlistId
  }

  @Transaction
  suspend fun updatePlaylistWithData(
      playlist: PlaylistEntity,
      groups: List<ChannelGroupEntity>,
      channels: List<ChannelEntity>,
      crossRefs: List<ChannelGroupCrossRef>,
  ) {
      updatePlaylist(playlist)

      // Delete FTS entries before deleting channels (need channel IDs for the query)
      deleteChannelFtsByPlaylistId(playlist.id)

      deleteChannelsByPlaylistId(playlist.id)

      if (groups.isNotEmpty()) {
          upsertGroups(groups)
      }

      val channelIds = insertChannels(channels)

      // Populate FTS index
      val ftsEntities = channels.mapIndexed { index, channel ->
          ChannelFtsEntity(rowid = channelIds[index], name = channel.name)
      }
      if (ftsEntities.isNotEmpty()) {
          insertChannelFts(ftsEntities)
      }

      if (crossRefs.isNotEmpty()) {
          insertChannelGroupCrossRefs(crossRefs)
      }
  }
  ```

#### 5. No changes to ChannelRepository.kt, ChannelRepositoryImpl.kt, GetChannelsUseCase.kt, or GetFavoritesUseCase.kt
- **Why**: The DAO method signatures (name, parameters, return type) remain identical. The FTS change is purely internal to the DAO queries. The repository and use case layers are completely unaffected.

### Database Changes
- **New entity**: `ChannelFtsEntity` -- FTS4 virtual table `channels_fts` with `content=channels`
- **Migration**: `MIGRATION_4_5` -- creates virtual table and backfills from existing data
- **Version bump**: 4 -> 5

### DI Changes
- **None**: No new classes need Koin registration. The `ChannelFtsEntity` is a Room entity, not an injectable dependency. The DAO import is handled by Room codegen.

## Implementation Order

1. **Create `ChannelFtsEntity.kt`** -- New FTS4 entity file. No dependencies on other changes.

2. **Update `AppDatabase.kt`** -- Add `ChannelFtsEntity::class` to entities array, bump version to 5. Depends on step 1.

3. **Add `MIGRATION_4_5` to `DatabaseBuilder.kt`** -- Create migration object and add to `addMigrations()`. Depends on step 2.

4. **Update `PlaylistDao.kt`** -- Add FTS helper methods (`insertChannelFts`, `deleteAllChannelFts`, `deleteChannelFtsByPlaylistId`), rewrite 5 search queries to use FTS JOIN, update `importPlaylistWithData()` and `updatePlaylistWithData()` to maintain FTS table. Depends on step 1.

5. **Build verification** -- `./gradlew :shared:assembleDebug` (or `./gradlew build`) to verify Room codegen succeeds with FTS entity.

6. **Update test fakes** -- No changes needed to `FakeChannelRepository` or `StubChannelRepository` since the repository interface is unchanged. The `FakePlaylistDao` (at `shared/src/commonTest/.../data/local/FakePlaylistDao.kt`) implements `PlaylistDao` as an interface, so it must add stub implementations for the 3 new methods: `insertChannelFts()`, `deleteAllChannelFts()`, and `deleteChannelFtsByPlaylistId()`. These can be no-op stubs since the fake is only used for testing ViewModels that don't exercise FTS.

7. **Run existing tests** -- `./gradlew :shared:testAndroidHostTest` to verify no regressions.

## Testing Strategy

### Automated tests (existing -- should pass without changes)
- `GetChannelsUseCaseTest` -- all 12 tests should pass unchanged since the use case and repository interfaces are identical
- `GetFavoritesUseCaseTest` -- should pass unchanged

### Manual testing required
1. **Fresh install**: Import demo playlist (`https://iptv-org.github.io/iptv/index.m3u`), verify search works on Channels, Home, and Favorites screens
2. **Upgrade path**: Install current version (v4 DB), import a playlist, then install the new version -- verify migration runs and search still works
3. **Search behavior**:
   - Search "BBC" -- should find "BBC World", "BBC News", etc. (prefix match on word "BBC")
   - Search "News" -- should find "CNN News", "BBC News", etc. (word-level match)
   - Search "cnn" (lowercase) -- should find "CNN" channels (FTS4 unicode61 tokenizer is case-insensitive)
   - Search "BB" -- should find "BBC" channels (prefix match with `*` wildcard)
   - Search special characters (e.g., channel names with `&`, `+`, parentheses) -- verify no crashes
4. **Performance**: With 10k+ channel demo playlist, search should feel instant (< 100ms) vs noticeable delay with LIKE
5. **Edge cases**:
   - Empty search query -- should not trigger FTS MATCH (handled by UseCase returning non-search flow)
   - Single character search -- should work with `*` wildcard
   - Playlist deletion -- FTS entries for deleted channels should be cleaned up by CASCADE (since channels are deleted, and FTS rowids reference channel ids, the FTS content becomes stale but queries via JOIN will not return deleted channels; the FTS rebuild happens on next import)

### Key assertions
- FTS MATCH queries return the same results as LIKE queries for whole-word searches
- FTS table size is significantly smaller than a full data copy (content table approach)
- Migration from v4 to v5 completes without errors
- Importing a new playlist after migration correctly populates FTS
- Updating/refreshing a playlist correctly rebuilds FTS for that playlist's channels

### Behavioral difference to document
- FTS4 tokenizes on word boundaries. Searching "NN" will NOT match "CNN" (unlike the current LIKE '%NN%'). This is acceptable as documented in Decision 4.

## Risks and Mitigations

### Risk: FTS4 content table synchronization
With `content=channels`, the FTS table does not automatically stay in sync. If channels are deleted outside the transaction methods (e.g., CASCADE from playlist deletion), FTS entries become orphaned. This is mitigated by:
- The FTS JOIN always goes through the channels table, so orphaned FTS entries are invisible to queries
- The FTS table is rebuilt on playlist re-import/update
- A future enhancement could add a `rebuildFtsIndex()` method if needed

### Risk: Room codegen compatibility with @Fts4 in KMP
Verified that `@Fts4` class exists in both `room-common-jvm-2.8.4.jar` and `room-common-iosSimulatorArm64Main-2.8.4.klib`. Room's KSP processor should generate the correct virtual table DDL for all platforms.

### Risk: FTS MATCH syntax errors from special characters in user input
FTS4 MATCH has special syntax characters (`*`, `"`, `OR`, `AND`, `NEAR`, `NOT`). If a user types these, the query could fail. Mitigation: wrap the query in double quotes in the DAO query to treat it as a literal phrase: `MATCH '"' || :query || '"*'`. However, this prevents multi-word boolean search. The simpler approach is to sanitize the query in the UseCase by escaping special characters before passing to the DAO.

**Decision**: Sanitize at the DAO query level using double-quote escaping: `MATCH '"' || :query || '*'`. This treats the entire user input as a literal phrase prefix. Multi-word queries like "CNN News" will match channels containing "CNN News" as an exact phrase prefix, which is the expected behavior.

## Doc Updates Required

- `docs/constraints/current-limitations.md` -- No changes needed (FTS search is not listed as a limitation)
- `docs/constraints/open-questions.md` -- No changes needed (no FTS-related open questions)
- `docs/features/search.md` -- If exists, update to note that search is FTS4-backed for performance. Mention the behavioral change (word-boundary tokenization vs substring matching)
