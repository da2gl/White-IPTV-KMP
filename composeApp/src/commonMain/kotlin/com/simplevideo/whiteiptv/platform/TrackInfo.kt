package com.simplevideo.whiteiptv.platform

/**
 * Information about an audio track in the media stream
 */
data class AudioTrackInfo(
    val id: String,
    val label: String,
    val language: String?,
    val channelCount: Int,
    val sampleRate: Int,
    val isSelected: Boolean,
)

/**
 * Information about a subtitle track in the media stream
 */
data class SubtitleTrackInfo(
    val id: String,
    val label: String,
    val language: String?,
    val isSelected: Boolean,
)

/**
 * Information about a video quality/rendition in the media stream
 */
data class VideoQualityInfo(
    val id: String,
    val label: String,
    val width: Int,
    val height: Int,
    val bitrate: Int,
    val isSelected: Boolean,
    val isAuto: Boolean = false,
)

/**
 * Container for all available tracks in the current media
 */
data class TracksInfo(
    val audioTracks: List<AudioTrackInfo> = emptyList(),
    val subtitleTracks: List<SubtitleTrackInfo> = emptyList(),
    val videoQualities: List<VideoQualityInfo> = emptyList(),
)
