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

## EPG Details

EPG data comes from XMLTV via `url-tvg`, but implementation details are undefined.

- How to handle large XMLTV files (50MB+) — stream parsing or download-then-parse?
- How long should EPG data be cached locally?
- How to handle timezone differences between EPG data and user's locale?
- UI for browsing past programs and triggering catchup playback.
- Should EPG info appear in channel cards on browse screens?

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
