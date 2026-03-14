# Current Limitations

Known gaps between the product specification and the current implementation.

---

## Settings: Language switching not implemented

The Settings screen Language option shows "System" only. No locale override or translated string resources exist yet.

**Target behavior**: Allow users to select a language from a list, overriding the system default.

---

## Settings: Clear Cache is a placeholder

The Clear Cache option shows "0 MB" and displays a success message, but does not actually clear platform caches (Coil image cache, Ktor HTTP cache). Requires platform-specific expect/actual implementation.

**Target behavior**: Calculate and display actual cache size, clear all caches when tapped.

---

## Search: FTS4 word-boundary constraint

Channel search uses FTS4 full-text indexing for performance. FTS4 tokenizes on word boundaries, so partial-word substring searches (e.g., searching "NN" to find "CNN") will not return results. Only word prefixes (e.g., "BB" finding "BBC") and full word matches (e.g., "News" finding "CNN News") are supported. This is an acceptable tradeoff for the significant performance improvement on playlists with 10k+ channels.

---

## Settings: Accent Color is persisted but not applied to theme

The Accent Color preference (Teal/Blue/Red) is saved in settings but does not yet change the Material theme color scheme. The `AppTheme` always uses the default Teal primary color.

**Target behavior**: Switching accent color changes the primary color throughout the app.

