# Search

Context-aware channel search across the app.

## Purpose

Let users quickly find channels by name within their current context (playlist, group, or favorites).

## Behavior

Search is **contextual** — it respects the active filters on the screen where it's triggered:

| Screen | Search scope |
|--------|-------------|
| Home | Channels in the selected playlist |
| Channels (specific playlist + group) | Channels matching both filters |
| Channels (All playlists) | All channels across all playlists |
| Favorites | Favorite channels matching the current playlist filter |

## UI

- Triggered by tapping the search icon in the header.
- Full-screen search overlay with:
  - Back arrow to dismiss
  - Text input with placeholder "Search channels..."
  - Search icon in the input field
- Results update as the user types (instant filtering).
- Matching channels displayed in the same layout as the parent screen.

## Search Logic

- Matches channel name (case-insensitive substring match).
- No search history or suggestions.
- Empty query shows all channels in the current scope.

> [!NOTE]
> **Undefined — requires clarification:**
> - Whether search should also match group names or other metadata (tvg-id, language).
> - Whether database-driven search (LIKE query) is needed for large playlists, or in-memory filtering is sufficient.
