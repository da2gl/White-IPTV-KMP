# Settings

Application-wide configuration.

## Purpose

Let users customize the app appearance, manage data, and configure playlist behavior.

## Sections

### Appearance

| Setting | Options | Default |
|---------|---------|---------|
| Theme | System / Light / Dark | System |
| Accent Color | Teal, Blue, Red (color picker) | Teal |
| Channel View | List / Grid | List |

### App Behavior

| Setting | Options | Default |
|---------|---------|---------|
| Language | English, Spanish, French, German, etc. | System language |
| Auto Update Playlists | On / Off | Off (uses `url-refresh` interval from M3U when enabled) |

### Data & Storage

| Action | Description |
|--------|-------------|
| Clear Cache | Free up storage space. Shows current cache size. |
| Clear Favorites | Remove all favorite channels. Confirmation dialog required. |
| Reset to Defaults | Restore all settings to defaults. Confirmation dialog required. Destructive action shown in red. |

### About

| Item | Description |
|------|-------------|
| App Version | Current version number |
| Contact Support | Opens email/support channel |
| Privacy Policy | Opens privacy policy page |

## Navigation

Settings is a tab in the bottom navigation bar. It does not have a back button — it's a root-level screen.

## Persistence

Settings are stored locally using a key-value preferences store. They persist across app restarts.
