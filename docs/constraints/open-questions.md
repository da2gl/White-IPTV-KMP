# Open Questions

Topics that need to be defined as the product evolves.

---

## Monetization

Whether the app will have a business model.

Roadmap mentioned Premium themes, Cloud sync subscription, White-label licensing — but no decision has been made.

- What features (if any) are gated behind a paywall?
- Is there a subscription model?
- Is the app free / open source?

---

## EPG Details — Resolved

Resolved in EPG Data Layer implementation plan (`.claude/features/epg/prep.md`):

- **Large XMLTV files**: Custom streaming parser processes line-by-line with regex. No DOM tree. Batch-inserts every 500 programs.
- **Cache duration**: Room DB storage, purge programs older than 24h on each load.
- **Timezone handling**: Store as UTC epoch millis. Apply `tvg-shift` during parsing. Display layer converts to local time.
- **Catchup UI**: Deferred to a future task.
- **Channel card EPG**: Deferred to a future task.

---

## Cloud Backup

Mentioned in the roadmap but not specified:

- What data would be backed up (playlists, favorites, settings, watch history)?
- Where would it be stored (iCloud, Google Drive, custom backend)?
- How would multi-device sync work?

---

## TV Layout

Roadmap mentions a "TV-optimized layout" for Android TV / Apple TV:

- Is this still planned?
- D-pad navigation considerations?
- Leanback library integration on Android?
