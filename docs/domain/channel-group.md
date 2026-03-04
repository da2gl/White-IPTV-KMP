# Channel Group

A category or genre of channels within a playlist. Groups are parsed from the `group-title` attribute in M3U files.

## Properties

- **Name** — group display name (e.g., "Sports", "News", "Movies").
- **Icon** — optional group icon URL.
- **Display order** — sorting priority. Groups with more channels or common names (News, Sports, Music) are prioritized.
- **Channel count** — number of channels in the group.

## Relationships

- A Channel Group belongs to one [Playlist](./playlist.md).
- A Channel Group contains zero or more [Channels](./channel.md) (many-to-many via cross-reference).

## Business Rules

- Group names are unique within a playlist.
- Groups with invalid names ("Undefined", "Unknown", "Other") are deprioritized in display.
- When a playlist is updated, groups are recreated from the new M3U data.
- Deleting a playlist cascade-deletes all its groups and cross-references.
