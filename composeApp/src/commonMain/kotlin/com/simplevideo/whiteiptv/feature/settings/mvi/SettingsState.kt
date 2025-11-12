package com.simplevideo.whiteiptv.feature.settings.mvi

data class SettingsState(
    // Appearance
    val theme: String = "System",
    val accentColor: String = "Teal",
    val channelView: String = "List",

    // Playback
    val defaultPlayer: String = "ExoPlayer",
    val preferredQuality: String = "Auto",

    // App Behavior
    val defaultPlaylist: String = "My Main Playlist",
    val language: String = "English",
    val autoUpdatePlaylists: Boolean = true,

    // About
    val appVersion: String = "1.2.3"
)
