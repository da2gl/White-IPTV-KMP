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

- Matches channel **name only** (case-insensitive).
- No search history or suggestions.
- Empty query shows all channels in the current scope.
- Database-driven: uses **FTS4 (Full-Text Search)** virtual table with phrase-prefix matching for fast indexed lookups. Handles playlists with 10k+ channels efficiently.

### FTS4 Implementation Details

- The FTS4 index uses the `unicode61` tokenizer, which splits text on word boundaries and is case-insensitive.
- Search uses phrase-prefix matching: the user's query is wrapped in double quotes and appended with `*` to enable prefix matching on the last word.
- Special characters in user input (FTS operators like `AND`, `OR`, `NEAR`, `NOT`, `*`, `"`) are treated as literals due to the double-quote escaping.

### Behavioral Notes

- **Word prefixes work**: searching "BB" matches "BBC World News" because "BB" is a prefix of the token "BBC".
- **Individual words match**: searching "News" matches "CNN News" because "News" is a separate token.
- **Partial-word substrings do not match**: searching "NN" will NOT match "CNN" because "NN" is not a word prefix -- it appears in the middle of the token "CNN". This differs from the previous `LIKE '%query%'` behavior.
- **Multi-word queries**: searching "CNN News" matches channels containing the exact phrase "CNN News" as a prefix sequence.
