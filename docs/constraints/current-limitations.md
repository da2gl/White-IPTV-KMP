# Current Limitations

Known gaps between the product specification and the current implementation.

---

## Settings: Language switching not implemented

The Settings screen Language option shows "System" only. No locale override or translated string resources exist yet.

**Target behavior**: Allow users to select a language from a list, overriding the system default.

---

## Search: FTS4 word-boundary constraint

Channel search uses FTS4 full-text indexing for performance. FTS4 tokenizes on word boundaries, so partial-word substring searches (e.g., searching "NN" to find "CNN") will not return results. Only word prefixes (e.g., "BB" finding "BBC") and full word matches (e.g., "News" finding "CNN News") are supported. This is an acceptable tradeoff for the significant performance improvement on playlists with 10k+ channels.


