# IPTV Database Structure

Comprehensive database schema for IPTV application with full M3U/M3U8 tag support.

## Tables Overview

### 1. **playlists** - IPTV Playlists

### 2. **channels** - TV Channels

### 3. **channel_groups** - Channel Categories (Optional)

---

## Table: `playlists`

Stores IPTV playlist metadata and global settings from M3U header.

| Column            | Type    | Description               | UI Usage                         |
|-------------------|---------|---------------------------|----------------------------------|
| `id`              | LONG    | Primary key               | -                                |
| `name`            | STRING  | Playlist name             | **Display in playlist selector** |
| `url`             | STRING  | M3U source URL            | Re-download for updates          |
| `icon`            | STRING? | Playlist logo             | **Show in playlist list**        |
| `urlTvg`          | STRING? | EPG/XMLTV source          | **Download TV program guide**    |
| `tvgShift`        | INT?    | Timezone offset (hours)   | Apply to EPG times               |
| `userAgent`       | STRING? | HTTP User-Agent           | Use in stream requests           |
| `refreshInterval` | INT?    | Update interval (seconds) | Auto-refresh schedule            |
| `channelCount`    | INT     | Number of channels        | **Display "250 channels"**       |
| `lastUpdate`      | LONG    | Last refresh timestamp    | **Show "Updated 2h ago"**        |
| `createdAt`       | LONG    | Creation timestamp        | Sort by date added               |

### UI Features from Playlist:

✅ **Playlist Card:**

```kotlin
PlaylistCard(
    name = playlist.name,
    icon = playlist.icon,
    channelCount = "${playlist.channelCount} channels",
    lastUpdate = "Updated ${formatRelativeTime(playlist.lastUpdate)}"
)
```

✅ **EPG Integration:**

```kotlin
if (playlist.urlTvg != null) {
    downloadEPG(playlist.urlTvg, playlist.tvgShift ?: 0)
    showTVGuide() // Show "What's on now"
}
```

---

## Table: `channels`

Stores TV channel information with comprehensive IPTV metadata.

| Column                    | Type    | Description          | UI Usage                   |
|---------------------------|---------|----------------------|----------------------------|
| `id`                      | LONG    | Primary key          | -                          |
| `playlistId`              | LONG    | Foreign key          | Filter by playlist         |
| **Basic**                 |
| `name`                    | STRING  | Channel name         | **Display as title**       |
| `url`                     | STRING  | Stream URL           | Play channel               |
| `logoUrl`                 | STRING? | Channel logo         | **Show channel icon**      |
| **TVG (EPG Integration)** |
| `tvgId`                   | STRING? | EPG channel ID       | **Link to TV guide**       |
| `tvgName`                 | STRING? | EPG name             | Fallback EPG matching      |
| `tvgChno`                 | STRING? | Channel number       | **Display "001 - CNN"**    |
| `tvgLanguage`             | STRING? | Language code        | **Filter chips: 🇺🇸 EN**  |
| `tvgCountry`              | STRING? | Country code         | **Filter by country flag** |
| **Grouping**              |
| `groupTitle`              | STRING? | Category             | **Group by: Sports, News** |
| `isFavorite`              | BOOLEAN | Favorite flag        | **Show in Favorites tab**  |
| **Catchup TV**            |
| `catchupDays`             | INT?    | Archive days         | **Show "📼 7 days"**       |
| `catchupType`             | STRING? | Catchup type         | Build catchup URL          |
| `catchupSource`           | STRING? | Catchup URL template | Player uses this           |
| **Network**               |
| `userAgent`               | STRING? | HTTP User-Agent      | Override playlist UA       |
| `referer`                 | STRING? | HTTP Referer         | Required by some providers |
| **Extended**              |
| `extendedMetadata`        | STRING? | JSON for extras      | Parse when needed          |

### Indexed Columns (Fast Queries):

- `playlistId` - Filter by playlist
- `tvgId` - EPG lookups
- `groupTitle` - Category filtering
- `isFavorite` - Favorites list

### UI Features from Channel:

#### **1. Channel List Item:**

```kotlin
ChannelListItem(
    number = channel.tvgChno, // "001"
    logo = channel.logoUrl,
    name = channel.name,
    language = channel.tvgLanguage, // "🇺🇸 EN"
    catchupBadge = if (channel.catchupDays > 0) "📼 ${channel.catchupDays}d" else null,
    isFavorite = channel.isFavorite,
    currentProgram = epg.getCurrentProgram(channel.tvgId) // From EPG
)
```

#### **2. Category Filters:**

```kotlin
// Group channels by category
val groups = channels.groupBy { it.groupTitle }
TabRow {
    groups.keys.forEach { groupTitle ->
        Tab(text = groupTitle) // "Sports", "News", "Movies"
    }
}
```

#### **3. Language/Country Filters:**

```kotlin
FilterChip(
    selected = selectedLanguage == "en",
    onClick = { filterByLanguage("en") },
    label = { Text("🇺🇸 English") }
)
```

#### **4. EPG Display:**

```kotlin
// When user opens channel details
if (channel.tvgId != null) {
    val programs = epgDatabase.getProgramsForChannel(
        channelId = channel.tvgId,
        date = today
    )
    ProgramList(programs) // Shows schedule
}
```

#### **5. Catchup/Archive:**

```kotlin
if (channel.catchupDays != null && channel.catchupDays > 0) {
    ArchiveButton(
        onClick = {
            val yesterday = now - 1.days
            val catchupUrl = buildCatchupUrl(
                template = channel.catchupSource,
                originalUrl = channel.url,
                type = channel.catchupType,
                start = yesterday
            )
            player.play(catchupUrl)
        },
        text = "📼 Watch from ${channel.catchupDays} days ago"
    )
}
```

#### **6. Favorites:**

```kotlin
// Toggle favorite
IconButton(onClick = {
    channelDao.update(channel.copy(isFavorite = !channel.isFavorite))
}) {
    Icon(if (channel.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder)
}

// Show favorites screen
val favorites = channelDao.getFavoriteChannels() // WHERE isFavorite = true
```

---

## Table: `channel_groups` (Optional)

Normalized table for channel categories. Use this for advanced UI features.

| Column         | Type    | Description       | UI Usage            |
|----------------|---------|-------------------|---------------------|
| `id`           | LONG    | Primary key       | -                   |
| `playlistId`   | LONG    | Foreign key       | Per-playlist groups |
| `name`         | STRING  | Group name        | **Category title**  |
| `icon`         | STRING? | Group icon        | **Category icon**   |
| `displayOrder` | INT     | Sort order        | Custom ordering     |
| `channelCount` | INT     | Channels in group | **"Sports (45)"**   |

### UI Features:

```kotlin
// Show category grid with counts
GroupGrid {
    groups.forEach { group ->
        CategoryCard(
            name = group.name,
            icon = group.icon,
            count = "${group.channelCount} channels"
        )
    }
}
```

---

## Example Queries for UI

### Get all channels for playlist:

```kotlin
channelDao.getChannelsByPlaylist(playlistId)
    .sortedBy { it.tvgChno?.toIntOrNull() ?: Int.MAX_VALUE }
```

### Get channels by category:

```kotlin
channelDao.getChannelsByGroup(groupTitle = "Sports")
```

### Get favorites:

```kotlin
channelDao.getFavoriteChannels() // WHERE isFavorite = true
```

### Get channels with catchup:

```kotlin
channelDao.getChannelsWithCatchup() // WHERE catchupDays > 0
```

### Filter by language:

```kotlin
channelDao.getChannelsByLanguage(language = "en")
```

### Get channels with EPG:

```kotlin
channelDao.getChannelsWithEPG() // WHERE tvgId IS NOT NULL
```

---

## Summary

### What's Stored:

✅ **Playlist:** name, icon, EPG source, User-Agent, refresh settings
✅ **Channel:** name, logo, stream URL, EPG ID, language, country, category
✅ **Catchup:** archive days, catchup type, catchup URL template
✅ **Network:** User-Agent, Referer headers
✅ **User Prefs:** isFavorite flag

### What Can Be Shown in UI:

✅ Channel numbers and logos
✅ Categories/groups (Sports, News, Movies)
✅ Language/country filters with flags
✅ "Archive available" badges
✅ EPG integration (TV guide)
✅ Favorites list
✅ Channel count per playlist/category
✅ Last update time

### Key Benefits:

- 🚀 Fast queries with indexes
- 📺 Full EPG integration ready
- 📼 Catchup TV support
- 🌍 Language/country filtering
- ⭐ Favorites system
- 🎯 Category organization
- 🔄 Auto-refresh capability

---

## Migration Notes

When creating database migrations, remember:

1. Add indexes for `tvgId`, `groupTitle`, `isFavorite`
2. Set default values for new columns (nullable or defaults)
3. Preserve existing `isFavorite` data if upgrading

Example Room migration:

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new columns with defaults
        database.execSQL("ALTER TABLE playlists ADD COLUMN urlTvg TEXT")
        database.execSQL("ALTER TABLE playlists ADD COLUMN tvgShift INTEGER")
        database.execSQL("ALTER TABLE channels ADD COLUMN tvgId TEXT")
        database.execSQL("ALTER TABLE channels ADD COLUMN catchupDays INTEGER")
        // ... etc

        // Create indexes
        database.execSQL("CREATE INDEX index_channels_tvgId ON channels(tvgId)")
        database.execSQL("CREATE INDEX index_channels_groupTitle ON channels(groupTitle)")
    }
}
```
