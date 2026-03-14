# Continue Watching — Implementation Plan

## Summary

Replace the empty `GetContinueWatchingUseCase` stub with a full watch history tracking system.
When a user plays a channel, we record a watch event (channel ID, playlist ID, timestamp, duration).
The Home screen displays the last 10 watched channels as a horizontal scrollable row with clickable
cards that navigate to the player.

## Decisions Made

### 1. Watch event recording strategy
- **Decision**: Record on play start + periodic 30s updates + final on pause/stop/channel-switch.
- **Rationale**: Captures both "what was watched" and approximate duration without excessive DB writes.
  30s interval balances accuracy vs. battery/IO cost.
- **Alternatives**: Record only on stop (misses data if app killed), record every second (too many writes).

### 2. Progress and time indicators for live IPTV
- **Decision**: Set `progress = 0f` (no progress bar) and `timeLeft = ""` (unused). Remove
  `LinearProgressIndicator` and `timeLeft` text from the card UI for now.
- **Rationale**: IPTV streams are infinite live content — progress/duration is meaningless. "Continue
  Watching" means "channels you recently watched", not "resume playback position".
- **Alternatives**: Show relative time like "2h ago" (deferred — adds complexity with no spec requirement).

### 3. Entity primary key design
- **Decision**: Use `channelId` as the `@PrimaryKey` (no auto-generated `id` column). One row per
  channel — latest session wins.
- **Rationale**: Room's `@Upsert` matches on primary key only. Using a separate auto-increment `id`
  with a unique index on `channelId` would cause `@Upsert` with `id=0` to always insert new rows
  instead of updating. Making `channelId` the PK ensures upsert works correctly for "update if
  watched again" semantics.
- **Alternatives**: Use `@Insert(onConflict = REPLACE)` with auto-increment id + unique channelId
  index — works but `@Upsert` is cleaner and the `id` column adds no value here.

### 4. Separate DAO vs extending PlaylistDao
- **Decision**: Create a new `WatchHistoryDao`. Keep it separate from `PlaylistDao`.
- **Rationale**: Single Responsibility. `PlaylistDao` already has ~20 methods. Watch history is a
  distinct concern. The existing codebase has a single DAO pattern, but adding a second DAO is
  straightforward in Room and follows the pattern shown in `AppDatabase`.
- **Alternatives**: Add methods to `PlaylistDao` — would work but increases coupling.

### 5. Repository layer
- **Decision**: Create `WatchHistoryRepository` interface in domain + `WatchHistoryRepositoryImpl`
  in data, following existing pattern (see `PlaylistRepository`/`PlaylistRepositoryImpl`).
- **Rationale**: Matches Clean Architecture pattern already used throughout codebase.

### 6. Architecture note — UseCase references presentation model
- **Decision**: Keep `GetContinueWatchingUseCase` returning `Flow<List<ContinueWatchingItem>>` where
  `ContinueWatchingItem` is from `feature.home.mvi`.
- **Rationale**: This is a pre-existing pattern in the stub. Fixing it (introducing a domain model
  + mapper) would be scope creep. The existing `HomeViewModel` already depends on this contract.
  Track as tech debt.

## Current State

### Existing files (relevant to this feature)

| File | Path | State |
|------|------|-------|
| GetContinueWatchingUseCase | `domain/usecase/GetContinueWatchingUseCase.kt` | Empty stub returning `flowOf(emptyList())`. No constructor params. |
| ContinueWatchingItem | `feature/home/mvi/HomeScreenModels.kt` | Data class with `channel: ChannelEntity`, `progress: Float`, `timeLeft: String` |
| HomeState | `feature/home/mvi/HomeMvi.kt:8-16` | Has `continueWatchingItems: List<ContinueWatchingItem>` field |
| HomeViewModel | `feature/home/HomeViewModel.kt:17-69` | Calls `getContinueWatching()` in combine block (line 30) |
| HomeScreen | `feature/home/HomeScreen.kt:153-165` | Renders Continue Watching section, cards NOT clickable |
| ContinueWatchingItem composable | `feature/home/HomeScreen.kt:242-259` | Shows logo, name, `LinearProgressIndicator`, `timeLeft` text. No onClick. |
| PlayerViewModel | `feature/player/PlayerViewModel.kt:14-126` | 3 constructor params. No watch tracking. No `onCleared()`. |
| AppDatabase | `data/local/AppDatabase.kt:12-25` | Version 2, 4 entities, only `playlistDao()` |
| DatabaseBuilder | `data/local/DatabaseBuilder.kt:1-17` | `getRoomDatabase()` with no migrations |
| KoinModule | `di/KoinModule.kt:1-87` | `GetContinueWatchingUseCase` registered at line 51 as `factoryOf` |
| ChannelEntity | `data/local/model/ChannelEntity.kt:13-92` | Table name = `"channels"`, PK = `id`, FK to `playlists` |
| PlaylistEntity | `data/local/model/PlaylistEntity.kt` | Uses `kotlin.time.Clock.System` with `@OptIn(ExperimentalTime::class)` |

All paths are relative to `composeApp/src/commonMain/kotlin/com/simplevideo/whiteiptv/`.

---

## Changes Required

### New Files

#### 1. `data/local/model/WatchHistoryEntity.kt`

Room entity for watch history. One row per channel (latest session).

```kotlin
package com.simplevideo.whiteiptv.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Records channel watch events for "Continue Watching" feature.
 * One row per channel — stores the most recent watch session.
 */
@Entity(
    tableName = "watch_history",
    foreignKeys = [
        ForeignKey(
            entity = ChannelEntity::class,
            parentColumns = ["id"],
            childColumns = ["channelId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["lastWatchedAt"]),
    ],
)
data class WatchHistoryEntity(
    @PrimaryKey
    val channelId: Long,
    val playlistId: Long,
    val lastWatchedAt: Long,
    val watchDurationMs: Long = 0,
)
```

**Design notes:**
- `channelId` is the PK — ensures one row per channel, and `@Upsert` works correctly
- `ForeignKey CASCADE` on `ChannelEntity` — deleting a channel removes its history
- `lastWatchedAt` indexed for efficient `ORDER BY DESC` in queries
- `playlistId` stored for potential filtering (not FK — history may survive playlist re-imports)

#### 2. `data/local/WatchHistoryDao.kt`

```kotlin
package com.simplevideo.whiteiptv.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Upsert
    suspend fun upsertWatchHistory(entry: WatchHistoryEntity)

    /**
     * Returns recently watched channels sorted by most recent, limited to [limit].
     * Joins with channels table to return full ChannelEntity data.
     */
    @Query(
        """
        SELECT c.* FROM channels c
        INNER JOIN watch_history wh ON c.id = wh.channelId
        ORDER BY wh.lastWatchedAt DESC
        LIMIT :limit
        """,
    )
    fun getRecentlyWatchedChannels(limit: Int): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM watch_history WHERE channelId = :channelId LIMIT 1")
    suspend fun getWatchHistoryForChannel(channelId: Long): WatchHistoryEntity?

    @Query("DELETE FROM watch_history")
    suspend fun clearAllHistory()
}
```

**Notes:**
- `@Upsert` matches on `channelId` (primary key) — inserts new or updates existing
- `getRecentlyWatchedChannels` returns `Flow<List<ChannelEntity>>` for reactive UI
- `getWatchHistoryForChannel` is suspend (one-time lookup for duration tracking)

#### 3. `domain/repository/WatchHistoryRepository.kt`

```kotlin
package com.simplevideo.whiteiptv.domain.repository

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

interface WatchHistoryRepository {
    fun getRecentlyWatchedChannels(limit: Int): Flow<List<ChannelEntity>>
    suspend fun recordWatchEvent(channelId: Long, playlistId: Long, durationMs: Long)
    suspend fun getWatchHistoryForChannel(channelId: Long): WatchHistoryEntity?
}
```

#### 4. `data/repository/WatchHistoryRepositoryImpl.kt`

```kotlin
package com.simplevideo.whiteiptv.data.repository

import com.simplevideo.whiteiptv.data.local.WatchHistoryDao
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.WatchHistoryEntity
import com.simplevideo.whiteiptv.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class WatchHistoryRepositoryImpl(
    private val watchHistoryDao: WatchHistoryDao,
) : WatchHistoryRepository {

    override fun getRecentlyWatchedChannels(limit: Int): Flow<List<ChannelEntity>> =
        watchHistoryDao.getRecentlyWatchedChannels(limit)

    override suspend fun recordWatchEvent(channelId: Long, playlistId: Long, durationMs: Long) {
        val entry = WatchHistoryEntity(
            channelId = channelId,
            playlistId = playlistId,
            lastWatchedAt = System.now().toEpochMilliseconds(),
            watchDurationMs = durationMs,
        )
        watchHistoryDao.upsertWatchHistory(entry)
    }

    override suspend fun getWatchHistoryForChannel(channelId: Long): WatchHistoryEntity? =
        watchHistoryDao.getWatchHistoryForChannel(channelId)
}
```

**Note:** No need to look up existing record before upsert — since `channelId` is the PK,
`@Upsert` handles insert-or-update automatically.

#### 5. `domain/usecase/RecordWatchEventUseCase.kt`

```kotlin
package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.repository.WatchHistoryRepository

/**
 * Records a watch event when a user plays, pauses, or stops a channel.
 * Called from PlayerViewModel on playback state changes and periodic updates.
 */
class RecordWatchEventUseCase(
    private val watchHistoryRepository: WatchHistoryRepository,
) {
    suspend operator fun invoke(channelId: Long, playlistId: Long, durationMs: Long = 0) {
        watchHistoryRepository.recordWatchEvent(channelId, playlistId, durationMs)
    }
}
```

### Modified Files

#### 6. `data/local/AppDatabase.kt`

Add `WatchHistoryEntity` to entities, bump version, add `watchHistoryDao()`.

```diff
 import com.simplevideo.whiteiptv.data.local.model.ChannelGroupCrossRef
 import com.simplevideo.whiteiptv.data.local.model.ChannelGroupEntity
 import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
+import com.simplevideo.whiteiptv.data.local.model.WatchHistoryEntity

 @Database(
     entities = [
         PlaylistEntity::class,
         ChannelEntity::class,
         ChannelGroupEntity::class,
         ChannelGroupCrossRef::class,
+        WatchHistoryEntity::class,
     ],
-    version = 2,
+    version = 3,
     exportSchema = true,
 )
 @ConstructedBy(AppDatabaseConstructor::class)
 abstract class AppDatabase : RoomDatabase() {
     abstract fun playlistDao(): PlaylistDao
+    abstract fun watchHistoryDao(): WatchHistoryDao
 }
```

#### 7. `data/local/DatabaseBuilder.kt`

Add migration v2->v3 and apply it in builder chain.

Replace the full file with:

```kotlin
package com.simplevideo.whiteiptv.data.local

import androidx.room.RoomDatabase.Builder
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Migration from v2 to v3: add watch_history table for Continue Watching feature
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `watch_history` (
                `channelId` INTEGER NOT NULL,
                `playlistId` INTEGER NOT NULL,
                `lastWatchedAt` INTEGER NOT NULL,
                `watchDurationMs` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`channelId`),
                FOREIGN KEY(`channelId`) REFERENCES `channels`(`id`) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_watch_history_lastWatchedAt` ON `watch_history` (`lastWatchedAt`)",
        )
    }
}

/**
 * Creates Room database with proper configuration for all platforms
 * Sets bundled SQLite driver and IO coroutine context
 */
fun getRoomDatabase(builder: Builder<AppDatabase>): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(MIGRATION_2_3)
        .build()
}
```

**Note:** Migration SQL matches entity definition exactly — `channelId` is `PRIMARY KEY` (not
auto-generated), with `FOREIGN KEY` to `channels.id` and index on `lastWatchedAt`. No
`id` column since `channelId` is the PK.

#### 8. `domain/usecase/GetContinueWatchingUseCase.kt`

Replace empty stub with repository-backed implementation:

```kotlin
package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.repository.WatchHistoryRepository
import com.simplevideo.whiteiptv.feature.home.mvi.ContinueWatchingItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Fetches continue watching items for the home screen.
 * Returns the last 10 watched channels sorted by recency.
 */
class GetContinueWatchingUseCase(
    private val watchHistoryRepository: WatchHistoryRepository,
) {
    operator fun invoke(): Flow<List<ContinueWatchingItem>> =
        watchHistoryRepository.getRecentlyWatchedChannels(limit = RECENT_LIMIT)
            .map { channels ->
                channels.map { channel ->
                    ContinueWatchingItem(
                        channel = channel,
                        progress = 0f,
                        timeLeft = "",
                    )
                }
            }

    companion object {
        private const val RECENT_LIMIT = 10
    }
}
```

**Note:** `progress = 0f` and `timeLeft = ""` — unused for live IPTV. The UI will be updated
to not display these fields.

#### 9. `feature/player/PlayerViewModel.kt`

Add `RecordWatchEventUseCase` and watch tracking logic.

Changes:
1. Add constructor parameter: `private val recordWatchEvent: RecordWatchEventUseCase`
2. Add watch tracking state: `watchStartTime`, `watchTimerJob`
3. Record on channel load (in `observeChannel` `.onEach`)
4. Start/stop timer on playback state changes
5. Cancel timer on channel switch and in `onCleared()`

```kotlin
package com.simplevideo.whiteiptv.feature.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.domain.usecase.GetAdjacentChannelUseCase
import com.simplevideo.whiteiptv.domain.usecase.GetChannelByIdUseCase
import com.simplevideo.whiteiptv.domain.usecase.RecordWatchEventUseCase
import com.simplevideo.whiteiptv.feature.player.mvi.PlayerAction
import com.simplevideo.whiteiptv.feature.player.mvi.PlayerEvent
import com.simplevideo.whiteiptv.feature.player.mvi.PlayerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class PlayerViewModel(
    savedStateHandle: SavedStateHandle,
    private val getChannelById: GetChannelByIdUseCase,
    private val getAdjacentChannel: GetAdjacentChannelUseCase,
    private val recordWatchEvent: RecordWatchEventUseCase,
) : BaseViewModel<PlayerState, PlayerAction, PlayerEvent>(
    initialState = PlayerState(),
) {
    private val initialChannelId: Long = checkNotNull(savedStateHandle["channelId"]) {
        "channelId is required for PlayerViewModel"
    }

    private val channelIdFlow = MutableStateFlow(initialChannelId)
    private var watchStartTime: Long = 0L
    private var watchTimerJob: Job? = null

    init {
        observeChannel()
    }

    private fun observeChannel() {
        channelIdFlow
            .map { channelId ->
                getChannelById(channelId)
            }
            .onEach { channel ->
                viewState = if (channel != null) {
                    recordInitialWatchEvent(channel.id, channel.playlistId)
                    viewState.copy(
                        channel = channel,
                        isLoading = false,
                        error = null,
                    )
                } else {
                    viewState.copy(
                        error = "Channel not found",
                        isLoading = false,
                    )
                }
            }
            .catch { e ->
                viewState = viewState.copy(
                    error = e.message ?: "Unknown error",
                    isLoading = false,
                )
            }
            .launchIn(viewModelScope)
    }

    private fun recordInitialWatchEvent(channelId: Long, playlistId: Long) {
        watchTimerJob?.cancel()
        watchStartTime = System.now().toEpochMilliseconds()
        viewModelScope.launch {
            recordWatchEvent(channelId, playlistId)
        }
    }

    private fun handlePlaybackStateForWatchTracking(isPlaying: Boolean) {
        if (isPlaying) {
            startWatchTimer()
        } else {
            stopWatchTimer()
        }
    }

    private fun startWatchTimer() {
        watchTimerJob?.cancel()
        watchTimerJob = viewModelScope.launch {
            while (true) {
                delay(WATCH_UPDATE_INTERVAL_MS)
                val channel = viewState.channel ?: continue
                val elapsed = System.now().toEpochMilliseconds() - watchStartTime
                recordWatchEvent(channel.id, channel.playlistId, elapsed)
            }
        }
    }

    private fun stopWatchTimer() {
        watchTimerJob?.cancel()
        watchTimerJob = null
        viewModelScope.launch {
            val channel = viewState.channel ?: return@launch
            val elapsed = System.now().toEpochMilliseconds() - watchStartTime
            recordWatchEvent(channel.id, channel.playlistId, elapsed)
        }
    }

    override fun obtainEvent(viewEvent: PlayerEvent) {
        when (viewEvent) {
            is PlayerEvent.OnBackClick -> {
                viewAction = PlayerAction.NavigateBack
            }

            is PlayerEvent.OnScreenTap -> {
                viewState = viewState.copy(controlsVisible = !viewState.controlsVisible)
            }

            is PlayerEvent.OnNextChannel -> navigateToChannel(next = true)

            is PlayerEvent.OnPreviousChannel -> navigateToChannel(next = false)

            is PlayerEvent.OnPlaybackStateChanged -> {
                viewState = viewState.copy(
                    isPlaying = viewEvent.isPlaying,
                    isBuffering = viewEvent.isBuffering,
                )
                handlePlaybackStateForWatchTracking(viewEvent.isPlaying)
            }

            is PlayerEvent.OnPlayerError -> {
                viewState = viewState.copy(error = viewEvent.message)
            }

            is PlayerEvent.OnTracksChanged -> {
                viewState = viewState.copy(tracksInfo = viewEvent.tracksInfo)
            }

            is PlayerEvent.OnShowTrackSelection -> {
                viewState = viewState.copy(showTrackSelectionDialog = viewEvent.type)
            }

            is PlayerEvent.OnDismissTrackSelection -> {
                viewState = viewState.copy(showTrackSelectionDialog = null)
            }

            is PlayerEvent.OnSelectAudioTrack -> {
                viewState = viewState.copy(showTrackSelectionDialog = null)
            }

            is PlayerEvent.OnSelectSubtitleTrack -> {
                viewState = viewState.copy(showTrackSelectionDialog = null)
            }

            is PlayerEvent.OnSelectVideoQuality -> {
                viewState = viewState.copy(showTrackSelectionDialog = null)
            }
        }
    }

    private fun navigateToChannel(next: Boolean) {
        val currentChannel = viewState.channel ?: return
        stopWatchTimer()

        viewModelScope.launch {
            val adjacentChannel = if (next) {
                getAdjacentChannel.getNext(currentChannel.playlistId, currentChannel.id)
            } else {
                getAdjacentChannel.getPrevious(currentChannel.playlistId, currentChannel.id)
            }

            adjacentChannel?.let {
                viewState = viewState.copy(isLoading = true)
                channelIdFlow.value = it.id
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        watchTimerJob?.cancel()
    }

    companion object {
        private const val WATCH_UPDATE_INTERVAL_MS = 30_000L
    }
}
```

**Key integration points:**
- `recordInitialWatchEvent()` called in `observeChannel()` when channel loads
- `handlePlaybackStateForWatchTracking()` called in `OnPlaybackStateChanged` handler
- `stopWatchTimer()` called in `navigateToChannel()` before switching
- `onCleared()` cancels timer job

#### 10. `feature/home/HomeScreen.kt`

Two changes:
1. Make `ContinueWatchingItem` composable clickable
2. Remove `LinearProgressIndicator` and `timeLeft` text (meaningless for live IPTV)

**Change A — ContinueWatchingItem call site (line 160-162):**

```kotlin
// Before:
items(state.continueWatchingItems) { item ->
    ContinueWatchingItem(item)
}

// After:
items(state.continueWatchingItems) { item ->
    ContinueWatchingItem(
        item = item,
        onClick = { onChannelClick(item.channel.id) },
    )
}
```

**Change B — ContinueWatchingItem composable (lines 242-259):**

```kotlin
// Before:
@Composable
private fun ContinueWatchingItem(item: ContinueWatchingItem) {
    Card(modifier = Modifier.width(200.dp)) {
        Column {
            AsyncImage(
                model = item.channel.logoUrl,
                contentDescription = item.channel.name,
                modifier = Modifier.height(100.dp).fillMaxWidth().background(Color.Gray),
                contentScale = ContentScale.Crop,
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(item.channel.name, style = MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(progress = { item.progress })
                Text(item.timeLeft, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// After:
@Composable
private fun ContinueWatchingItem(
    item: ContinueWatchingItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
    ) {
        Column {
            AsyncImage(
                model = item.channel.logoUrl,
                contentDescription = item.channel.name,
                modifier = Modifier.height(100.dp).fillMaxWidth().background(Color.Gray),
                contentScale = ContentScale.Crop,
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(item.channel.name, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
```

#### 11. `di/KoinModule.kt`

Add imports and registrations for new components:

```diff
+import com.simplevideo.whiteiptv.data.local.WatchHistoryDao
+import com.simplevideo.whiteiptv.data.repository.WatchHistoryRepositoryImpl
+import com.simplevideo.whiteiptv.domain.repository.WatchHistoryRepository

 val databaseModule = module {
     single { get<AppDatabase>().playlistDao() }
+    single { get<AppDatabase>().watchHistoryDao() }
 }

 val repositoryModule = module {
     singleOf(::PlaylistRepositoryImpl) bind PlaylistRepository::class
     singleOf(::ChannelRepositoryImpl) bind ChannelRepository::class
     singleOf(::CurrentPlaylistRepository)
+    singleOf(::WatchHistoryRepositoryImpl) bind WatchHistoryRepository::class
 }

 // In useCaseModule — add:
+    factoryOf(::RecordWatchEventUseCase)
 // GetContinueWatchingUseCase already registered — Koin auto-resolves new constructor param
```

**Note:** `GetContinueWatchingUseCase` is already registered at line 51. Koin's `factoryOf` will
automatically resolve the new `WatchHistoryRepository` constructor parameter.

---

## Implementation Order

1. **WatchHistoryEntity** — `data/local/model/WatchHistoryEntity.kt` (new file, no dependencies)
2. **WatchHistoryDao** — `data/local/WatchHistoryDao.kt` (new file, depends on entity)
3. **AppDatabase** — `data/local/AppDatabase.kt` (add entity + DAO, bump version 2->3)
4. **DatabaseBuilder** — `data/local/DatabaseBuilder.kt` (add `MIGRATION_2_3`, apply in builder)
5. **WatchHistoryRepository** — `domain/repository/WatchHistoryRepository.kt` (new interface)
6. **WatchHistoryRepositoryImpl** — `data/repository/WatchHistoryRepositoryImpl.kt` (new impl)
7. **RecordWatchEventUseCase** — `domain/usecase/RecordWatchEventUseCase.kt` (new UseCase)
8. **GetContinueWatchingUseCase** — `domain/usecase/GetContinueWatchingUseCase.kt` (replace stub)
9. **KoinModule** — `di/KoinModule.kt` (register DAO, repository, UseCase)
10. **PlayerViewModel** — `feature/player/PlayerViewModel.kt` (add watch tracking)
11. **HomeScreen** — `feature/home/HomeScreen.kt` (clickable cards, remove progress bar)

---

## Files Summary

### New Files (5)

| # | File | Description |
|---|------|-------------|
| 1 | `data/local/model/WatchHistoryEntity.kt` | Room entity — `channelId` PK, `playlistId`, `lastWatchedAt`, `watchDurationMs` |
| 2 | `data/local/WatchHistoryDao.kt` | DAO — upsert, Flow-based recent channels query, single channel lookup |
| 3 | `domain/repository/WatchHistoryRepository.kt` | Repository interface for watch history |
| 4 | `data/repository/WatchHistoryRepositoryImpl.kt` | Repository implementation delegating to DAO |
| 5 | `domain/usecase/RecordWatchEventUseCase.kt` | Records watch events from PlayerViewModel |

### Modified Files (5)

| # | File | Changes |
|---|------|---------|
| 6 | `data/local/AppDatabase.kt` | Add `WatchHistoryEntity` to entities, bump version 2->3, add `watchHistoryDao()` |
| 7 | `data/local/DatabaseBuilder.kt` | Add `MIGRATION_2_3` (new table + index), apply via `.addMigrations()` |
| 8 | `domain/usecase/GetContinueWatchingUseCase.kt` | Replace stub with repository query, add constructor param |
| 9 | `feature/player/PlayerViewModel.kt` | Add `RecordWatchEventUseCase`, watch start/timer/stop, `onCleared()` |
| 10 | `feature/home/HomeScreen.kt` | Add `onClick` to `ContinueWatchingItem`, remove `LinearProgressIndicator` and `timeLeft` |
| 11 | `di/KoinModule.kt` | Register `WatchHistoryDao`, `WatchHistoryRepositoryImpl`, `RecordWatchEventUseCase` |

### Database Changes

- **Migration v2 -> v3**: Additive — creates `watch_history` table + index. No data loss risk.
- **Schema**: New table `watch_history(channelId PK, playlistId, lastWatchedAt, watchDurationMs)`
  with FK to `channels(id)` CASCADE and index on `lastWatchedAt`.

---

## Potential Merge Conflicts with Other Wave 1 Features

### High Risk: `di/KoinModule.kt`
All Wave 1 features add lines here. All changes are additive (new lines in module blocks).
Merge conflicts will be simple line-level conflicts resolvable by keeping all additions.

### Medium Risk: `feature/home/HomeScreen.kt`
- **Playlist Management** touches `HomeTopAppBar` (gear icon click)
- **Search Enhancement** touches `HomeTopAppBar` (search icon click)
- **Continue Watching** touches `ContinueWatchingItem` composable and its call site in `HomeContent`
- Changes are in **different sections** of the file — low actual conflict risk.

### Low Risk: All other files
`AppDatabase.kt`, `DatabaseBuilder.kt`, `PlayerViewModel.kt` — only Continue Watching touches these.

---

## Testing Strategy

### Unit Tests
- `WatchHistoryRepositoryImplTest`: verify `recordWatchEvent` creates correct entity, verify upsert updates existing
- `GetContinueWatchingUseCaseTest`: verify Flow maps ChannelEntity to ContinueWatchingItem correctly
- `RecordWatchEventUseCaseTest`: verify delegates to repository

### Integration Tests
- DB migration test: verify v2->v3 migration creates table with correct schema
- DAO test: verify `upsertWatchHistory` inserts and updates, `getRecentlyWatchedChannels` returns correct order

### Edge Cases
- Play channel with no logo (null `logoUrl`)
- Switch channels rapidly (timer cancellation)
- Channel deleted while in watch history (CASCADE should clean up)
- Empty watch history (section hidden on Home)

### Manual Verification
```bash
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:testDebugUnitTest
ls composeApp/schemas/com.simplevideo.whiteiptv.data.local.AppDatabase/3.json
```

---

## Doc Updates Required

After implementation:
1. **`docs/constraints/current-limitations.md`** — Remove "Continue Watching returns empty" section
2. **`docs/features/home.md`** — Update Continue Watching section to note that progress bar and
   time-left are omitted for live IPTV (only channel logo + name shown)
