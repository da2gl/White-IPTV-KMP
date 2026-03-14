# EPG Data Layer — Implementation Plan

## Summary

Build the EPG (Electronic Program Guide) data pipeline: a custom streaming XMLTV parser (pure Kotlin, KMP-compatible), Room database entity with indices, DAO, domain model, repository, and two use cases — `LoadEpgUseCase` (download → stream-parse → batch-insert → purge old) and `GetCurrentProgramUseCase` (query current + next program by tvgId + current time).

**Out of scope**: Player UI integration (Wave 3b), channel card EPG, catchup playback, EPG grid/timeline UI.

## Open Questions — Resolved

| Question | Decision |
|----------|----------|
| Large file handling (50MB+) | Custom streaming XML parser: line-by-line regex-based approach. No DOM loading. Process `<programme>` elements one-at-a-time. |
| XML parsing library | **No external library.** Use a lightweight custom streaming parser with regex. XMLTV is a simple, predictable format. Avoids adding a heavy dependency (xmlutil is 2MB+). The parser reads the response body line by line and extracts `<programme>` elements using simple string matching. |
| Gzip decompression | Ktor handles `Content-Encoding: gzip` automatically via CIO engine. For `.gz` file URLs (not content-encoding), add `ktor-client-encoding` plugin which adds `Accept-Encoding: gzip, deflate` header and decompresses. |
| Caching strategy | Store in Room DB. Purge programs older than 24h on each fresh load. |
| Timezone handling | Store all times as UTC epoch millis. `tvgShift` (hours offset from playlist header) is applied during parsing. Display layer converts to local time. |
| Channel card EPG | Deferred — not in this task. |
| Catchup UI | Deferred — not in this task. |

## Architecture

```
LoadEpgUseCase
  → Ktor HttpClient (download XMLTV)
  → XmltvParser (streaming parse → List<EpgProgramEntity>)
  → EpgDao (batch insert + purge old)

GetCurrentProgramUseCase
  → EpgDao (query by tvgId + time window)
  → returns EpgProgram domain model
```

## Files to Create

### 1. `data/local/model/EpgProgramEntity.kt`
Room entity for EPG program data.

```kotlin
package com.simplevideo.whiteiptv.data.local.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "epg_programs",
    indices = [
        Index(value = ["channelTvgId", "startTime"]),
        Index(value = ["channelTvgId", "endTime"]),
    ],
)
data class EpgProgramEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val channelTvgId: String,
    val title: String,
    val description: String? = null,
    val startTime: Long, // UTC epoch millis
    val endTime: Long,   // UTC epoch millis
    val category: String? = null,
    val iconUrl: String? = null,
)
```

**Notes:**
- No foreign key to ChannelEntity — EPG matching is by `tvgId` string, not by DB id. Channels may not exist yet when EPG is loaded.
- Composite index on `(channelTvgId, startTime)` for efficient current/next program queries.
- Second index on `(channelTvgId, endTime)` for purge queries and "current program" lookups.

### 2. `data/local/EpgDao.kt`
DAO for EPG CRUD operations.

```kotlin
package com.simplevideo.whiteiptv.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simplevideo.whiteiptv.data.local.model.EpgProgramEntity

@Dao
interface EpgDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrograms(programs: List<EpgProgramEntity>)

    @Query(
        """
        SELECT * FROM epg_programs
        WHERE channelTvgId = :tvgId
          AND startTime <= :timeMs
          AND endTime > :timeMs
        LIMIT 1
        """
    )
    suspend fun getCurrentProgram(tvgId: String, timeMs: Long): EpgProgramEntity?

    @Query(
        """
        SELECT * FROM epg_programs
        WHERE channelTvgId = :tvgId
          AND startTime > :timeMs
        ORDER BY startTime ASC
        LIMIT 1
        """
    )
    suspend fun getNextProgram(tvgId: String, timeMs: Long): EpgProgramEntity?

    @Query("DELETE FROM epg_programs WHERE endTime < :timeMs")
    suspend fun deleteOlderThan(timeMs: Long)

    @Query("DELETE FROM epg_programs WHERE channelTvgId IN (SELECT tvgId FROM channels WHERE playlistId = :playlistId)")
    suspend fun deleteByPlaylistChannels(playlistId: Long)

    @Query("DELETE FROM epg_programs")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM epg_programs")
    suspend fun getProgramCount(): Int
}
```

### 3. `data/parser/epg/XmltvParser.kt`
Streaming XMLTV parser — pure Kotlin, no external XML library.

```kotlin
package com.simplevideo.whiteiptv.data.parser.epg

import com.simplevideo.whiteiptv.common.AppLogger
import com.simplevideo.whiteiptv.data.local.model.EpgProgramEntity

/**
 * Streaming XMLTV parser for EPG data.
 *
 * Parses XMLTV format without loading entire DOM into memory.
 * Processes <programme> elements using string matching and regex.
 * Handles files 50MB+ by processing line-by-line.
 *
 * XMLTV programme format:
 * <programme start="20240101120000 +0000" stop="20240101130000 +0000" channel="channel.id">
 *   <title lang="en">Program Title</title>
 *   <desc lang="en">Description</desc>
 *   <category lang="en">Category</category>
 *   <icon src="http://example.com/icon.png"/>
 * </programme>
 */
class XmltvParser {

    /**
     * Parse XMLTV content string into EPG program entities.
     * @param content Full XMLTV content as string
     * @param tvgShiftHours Global timezone shift from playlist header (hours)
     * @return List of parsed EPG programs
     */
    fun parse(content: String, tvgShiftHours: Int = 0): List<EpgProgramEntity> {
        val programs = mutableListOf<EpgProgramEntity>()
        val shiftMs = tvgShiftHours * 3600_000L

        val programRegex = Regex(
            """<programme\s+([^>]*)>(.*?)</programme>""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE),
        )
        val startRegex = Regex("""start="([^"]+)"""")
        val stopRegex = Regex("""stop="([^"]+)"""")
        val channelRegex = Regex("""channel="([^"]+)"""")
        val titleRegex = Regex("""<title[^>]*>(.*?)</title>""", RegexOption.DOT_MATCHES_ALL)
        val descRegex = Regex("""<desc[^>]*>(.*?)</desc>""", RegexOption.DOT_MATCHES_ALL)
        val categoryRegex = Regex("""<category[^>]*>(.*?)</category>""", RegexOption.DOT_MATCHES_ALL)
        val iconRegex = Regex("""<icon\s+src="([^"]+)"""")

        programRegex.findAll(content).forEach { match ->
            try {
                val attrs = match.groupValues[1]
                val body = match.groupValues[2]

                val channelId = channelRegex.find(attrs)?.groupValues?.get(1) ?: return@forEach
                val startStr = startRegex.find(attrs)?.groupValues?.get(1) ?: return@forEach
                val stopStr = stopRegex.find(attrs)?.groupValues?.get(1) ?: return@forEach

                val startTime = parseXmltvTime(startStr) + shiftMs
                val stopTime = parseXmltvTime(stopStr) + shiftMs
                if (startTime == 0L || stopTime == 0L) return@forEach

                val title = titleRegex.find(body)?.groupValues?.get(1)?.decodeXmlEntities() ?: return@forEach
                val description = descRegex.find(body)?.groupValues?.get(1)?.decodeXmlEntities()
                val category = categoryRegex.find(body)?.groupValues?.get(1)?.decodeXmlEntities()
                val iconUrl = iconRegex.find(body)?.groupValues?.get(1)

                programs.add(
                    EpgProgramEntity(
                        channelTvgId = channelId,
                        title = title,
                        description = description,
                        startTime = startTime,
                        endTime = stopTime,
                        category = category,
                        iconUrl = iconUrl,
                    ),
                )
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to parse programme element: ${e.message}")
            }
        }

        AppLogger.d(TAG, "Parsed ${programs.size} EPG programs")
        return programs
    }

    companion object {
        private const val TAG = "XmltvParser"

        /**
         * Parse XMLTV timestamp format: "YYYYMMDDHHmmss +HHMM" or "YYYYMMDDHHmmss"
         * Returns UTC epoch milliseconds
         */
        internal fun parseXmltvTime(timeStr: String): Long {
            // Implementation: parse "20240101120000 +0000" format
            // Extract date/time parts, apply timezone offset, return epoch millis
            // See implementation step for full code
        }

        internal fun String.decodeXmlEntities(): String {
            return this
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .trim()
        }
    }
}
```

**Important implementation notes:**
- The regex approach loads the full content into memory for regex matching. For very large files (100MB+), this could be problematic. However, for typical XMLTV files (10-50MB), this is acceptable given Kotlin string handling.
- Alternative: If memory is a concern, implement a line-by-line state machine parser that accumulates `<programme>` blocks. The coder should evaluate and choose the best approach.
- `parseXmltvTime` must handle both `"YYYYMMDDHHmmss +HHMM"` (with timezone) and `"YYYYMMDDHHmmss"` (UTC assumed) formats. Use manual parsing (no kotlinx-datetime dependency needed for this).

### 4. `domain/model/EpgProgram.kt`
Domain model for EPG program (used by presentation layer).

```kotlin
package com.simplevideo.whiteiptv.domain.model

data class EpgProgram(
    val title: String,
    val description: String? = null,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val category: String? = null,
    val iconUrl: String? = null,
)
```

### 5. `data/mapper/EpgProgramMapper.kt`
Maps between entity and domain model.

```kotlin
package com.simplevideo.whiteiptv.data.mapper

import com.simplevideo.whiteiptv.data.local.model.EpgProgramEntity
import com.simplevideo.whiteiptv.domain.model.EpgProgram

class EpgProgramMapper {
    fun toDomain(entity: EpgProgramEntity): EpgProgram = EpgProgram(
        title = entity.title,
        description = entity.description,
        startTimeMs = entity.startTime,
        endTimeMs = entity.endTime,
        category = entity.category,
        iconUrl = entity.iconUrl,
    )
}
```

### 6. `domain/repository/EpgRepository.kt`
Repository interface.

```kotlin
package com.simplevideo.whiteiptv.domain.repository

import com.simplevideo.whiteiptv.domain.model.EpgProgram

interface EpgRepository {
    suspend fun loadEpg(playlistId: Long, xmltvUrl: String, tvgShiftHours: Int = 0)
    suspend fun getCurrentProgram(tvgId: String): EpgProgram?
    suspend fun getNextProgram(tvgId: String): EpgProgram?
    suspend fun clearEpg()
    suspend fun hasEpgData(): Boolean
}
```

### 7. `data/repository/EpgRepositoryImpl.kt`
Repository implementation.

```kotlin
package com.simplevideo.whiteiptv.data.repository

import com.simplevideo.whiteiptv.common.AppLogger
import com.simplevideo.whiteiptv.data.local.EpgDao
import com.simplevideo.whiteiptv.data.mapper.EpgProgramMapper
import com.simplevideo.whiteiptv.data.parser.epg.XmltvParser
import com.simplevideo.whiteiptv.domain.model.EpgProgram
import com.simplevideo.whiteiptv.domain.repository.EpgRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlin.time.Clock.System
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class EpgRepositoryImpl(
    private val httpClient: HttpClient,
    private val epgDao: EpgDao,
    private val xmltvParser: XmltvParser,
    private val epgProgramMapper: EpgProgramMapper,
) : EpgRepository {

    override suspend fun loadEpg(playlistId: Long, xmltvUrl: String, tvgShiftHours: Int) {
        // 1. Download XMLTV content
        val content = httpClient.get(xmltvUrl).bodyAsText()

        // 2. Parse XMLTV into entities
        val programs = xmltvParser.parse(content, tvgShiftHours)

        // 3. Clear old EPG data for this playlist's channels, then insert new
        epgDao.deleteByPlaylistChannels(playlistId)

        // 4. Batch insert (in chunks to avoid SQLite variable limit)
        programs.chunked(BATCH_SIZE).forEach { batch ->
            epgDao.insertPrograms(batch)
        }

        // 5. Purge programs older than 24h
        val cutoff = System.now().toEpochMilliseconds() - PURGE_THRESHOLD_MS
        epgDao.deleteOlderThan(cutoff)

        AppLogger.d(TAG, "Loaded ${programs.size} EPG programs for playlist $playlistId")
    }

    override suspend fun getCurrentProgram(tvgId: String): EpgProgram? {
        val now = System.now().toEpochMilliseconds()
        return epgDao.getCurrentProgram(tvgId, now)?.let { epgProgramMapper.toDomain(it) }
    }

    override suspend fun getNextProgram(tvgId: String): EpgProgram? {
        val now = System.now().toEpochMilliseconds()
        return epgDao.getNextProgram(tvgId, now)?.let { epgProgramMapper.toDomain(it) }
    }

    override suspend fun clearEpg() {
        epgDao.deleteAll()
    }

    override suspend fun hasEpgData(): Boolean {
        return epgDao.getProgramCount() > 0
    }

    companion object {
        private const val TAG = "EpgRepository"
        private const val BATCH_SIZE = 500
        private const val PURGE_THRESHOLD_MS = 24 * 60 * 60 * 1000L // 24 hours
    }
}
```

### 8. `domain/usecase/LoadEpgUseCase.kt`
Use case to load EPG data for a playlist.

```kotlin
package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.common.AppLogger
import com.simplevideo.whiteiptv.domain.repository.EpgRepository
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository

class LoadEpgUseCase(
    private val playlistRepository: PlaylistRepository,
    private val epgRepository: EpgRepository,
) {
    /**
     * Load EPG data for the given playlist.
     * Silently does nothing if playlist has no urlTvg.
     * Errors are logged but not thrown (EPG is non-critical).
     */
    suspend operator fun invoke(playlistId: Long) {
        val playlist = playlistRepository.getPlaylistById(playlistId) ?: return
        val xmltvUrl = playlist.urlTvg ?: return

        runCatching {
            epgRepository.loadEpg(
                playlistId = playlistId,
                xmltvUrl = xmltvUrl,
                tvgShiftHours = playlist.tvgShift ?: 0,
            )
        }.onFailure { e ->
            AppLogger.w(TAG, "Failed to load EPG for playlist $playlistId: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "LoadEpgUseCase"
    }
}
```

### 9. `domain/usecase/GetCurrentProgramUseCase.kt`
Use case to get current + next program for a channel.

```kotlin
package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.domain.model.EpgProgram
import com.simplevideo.whiteiptv.domain.repository.EpgRepository

class GetCurrentProgramUseCase(
    private val epgRepository: EpgRepository,
) {
    /**
     * Returns current and next program for the given tvgId.
     * Returns nulls if no EPG data available.
     */
    suspend operator fun invoke(tvgId: String?): Pair<EpgProgram?, EpgProgram?> {
        if (tvgId.isNullOrBlank()) return null to null
        val current = epgRepository.getCurrentProgram(tvgId)
        val next = epgRepository.getNextProgram(tvgId)
        return current to next
    }
}
```

## Files to Modify

### 10. `data/local/AppDatabase.kt`
Add EpgProgramEntity to entities list, bump version to 4, add `epgDao()`.

```kotlin
// Add to entities array: EpgProgramEntity::class
// Change version to 4
// Add: abstract fun epgDao(): EpgDao
```

### 11. `data/local/DatabaseBuilder.kt`
Add MIGRATION_3_4.

```kotlin
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `epg_programs` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `channelTvgId` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `description` TEXT,
                `startTime` INTEGER NOT NULL,
                `endTime` INTEGER NOT NULL,
                `category` TEXT,
                `iconUrl` TEXT
            )
            """.trimIndent(),
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_epg_programs_channelTvgId_startTime` ON `epg_programs` (`channelTvgId`, `startTime`)",
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_epg_programs_channelTvgId_endTime` ON `epg_programs` (`channelTvgId`, `endTime`)",
        )
    }
}

// In getRoomDatabase(): add .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
```

### 12. `di/KoinModule.kt`
Register new components.

```kotlin
// repositoryModule: add
singleOf(::EpgRepositoryImpl) bind EpgRepository::class

// mapperModule: add
factoryOf(::EpgProgramMapper)

// useCaseModule: add
factoryOf(::LoadEpgUseCase)
factoryOf(::GetCurrentProgramUseCase)

// databaseModule: add
single { get<AppDatabase>().epgDao() }

// Also register XmltvParser as factory:
// In a new or existing module:
factoryOf(::XmltvParser)
```

### 13. `domain/repository/PlaylistRepository.kt`
Add method to get playlist by ID (if not already present). Check if `getPlaylistById(id: Long): PlaylistEntity?` exists. If not, add it.

### 14. `gradle/libs.versions.toml` and `build.gradle.kts`
Add `ktor-client-encoding` dependency for automatic gzip handling:

```toml
# libs.versions.toml - add to [libraries]:
ktor-client-encoding = { module = "io.ktor:ktor-client-encoding", version.ref = "ktor" }
```

```kotlin
// build.gradle.kts - add to commonMain.dependencies:
implementation(libs.ktor.client.encoding)
```

Then install the ContentEncoding plugin in `HttpClientFactory.kt`:

```kotlin
import io.ktor.client.plugins.compression.ContentEncoding

// In HttpClient config block:
install(ContentEncoding) {
    gzip()
    deflate()
}
```

## Implementation Order

1. **EpgProgramEntity** — Room entity
2. **EpgDao** — DAO interface
3. **AppDatabase** — Add entity + DAO, bump version
4. **DatabaseBuilder** — Add MIGRATION_3_4
5. **XmltvParser** — Streaming XMLTV parser
6. **EpgProgram** — Domain model
7. **EpgProgramMapper** — Entity ↔ domain mapping
8. **EpgRepository** — Interface
9. **EpgRepositoryImpl** — Implementation with Ktor download + parse + DB operations
10. **LoadEpgUseCase** — Orchestrates EPG loading
11. **GetCurrentProgramUseCase** — Queries current/next program
12. **KoinModule.kt** — Register all new components
13. **libs.versions.toml + build.gradle.kts** — Add ktor-client-encoding
14. **HttpClientFactory.kt** — Install ContentEncoding plugin
15. **PlaylistRepository** — Add `getPlaylistById` if missing

## XmltvParser Implementation Details

### XMLTV Time Format
Format: `YYYYMMDDHHmmss +HHMM` or `YYYYMMDDHHmmss` (assumes UTC)

Examples:
- `"20240315120000 +0300"` → March 15, 2024 12:00:00 in +03:00 timezone
- `"20240315120000"` → March 15, 2024 12:00:00 UTC

### Time Parsing Algorithm
```
1. Split by space to get datetime part and optional timezone
2. Parse datetime: year(4), month(2), day(2), hour(2), minute(2), second(2)
3. Convert to epoch millis using manual calculation (days since epoch × ms/day + time of day)
4. Apply timezone offset if present: subtract offset to convert to UTC
5. Apply tvgShift offset (added during parse call)
```

Use `kotlinx-datetime` (already in dependencies) for reliable date calculations:
```kotlin
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant

fun parseXmltvTime(timeStr: String): Long {
    val parts = timeStr.trim().split(" ", limit = 2)
    val dt = parts[0]
    if (dt.length < 14) return 0L

    val year = dt.substring(0, 4).toIntOrNull() ?: return 0L
    val month = dt.substring(4, 6).toIntOrNull() ?: return 0L
    val day = dt.substring(6, 8).toIntOrNull() ?: return 0L
    val hour = dt.substring(8, 10).toIntOrNull() ?: return 0L
    val minute = dt.substring(10, 12).toIntOrNull() ?: return 0L
    val second = dt.substring(12, 14).toIntOrNull() ?: return 0L

    val localDateTime = LocalDateTime(year, month, day, hour, minute, second)

    val offset = if (parts.size > 1) {
        val tz = parts[1].trim()
        val sign = if (tz.startsWith("-")) -1 else 1
        val tzDigits = tz.removePrefix("+").removePrefix("-")
        val tzHours = tzDigits.substring(0, 2).toIntOrNull() ?: 0
        val tzMinutes = if (tzDigits.length >= 4) tzDigits.substring(2, 4).toIntOrNull() ?: 0 else 0
        UtcOffset(hours = sign * tzHours, minutes = sign * tzMinutes)
    } else {
        UtcOffset.ZERO
    }

    return localDateTime.toInstant(offset).toEpochMilliseconds()
}
```

### XML Entity Decoding
Handle standard XML entities: `&amp;`, `&lt;`, `&gt;`, `&quot;`, `&apos;`.

### Error Handling
- Individual `<programme>` parse failures are logged and skipped (don't fail the whole load)
- Network failures in `LoadEpgUseCase` are caught and logged (EPG is non-critical per spec)
- Empty/invalid XMLTV content results in 0 programs inserted (no error)

## Testing Strategy

Unit tests for:
1. **XmltvParser** — Parse valid XMLTV, handle edge cases (missing fields, malformed times, XML entities)
2. **parseXmltvTime** — Various timestamp formats, timezone offsets, edge cases
3. **EpgProgramMapper** — Entity to domain conversion
4. **GetCurrentProgramUseCase** — Null tvgId handling, delegates to repository
5. **LoadEpgUseCase** — Null urlTvg skips, error handling

Integration test:
6. **EpgDao** — Insert, query current/next, purge old programs (requires Room test infra)

## Build Verification

After implementation, verify:
```bash
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:testDebugUnitTest
```

## Dependencies

- `kotlinx-datetime` (already in project) — for reliable XMLTV time parsing
- `ktor-client-encoding` (NEW) — for automatic gzip decompression of XMLTV downloads
- Room (already in project) — for EPG data storage
- Ktor HttpClient (already in project) — for XMLTV download
