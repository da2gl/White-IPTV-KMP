package com.simplevideo.whiteiptv.platform.exoplayer

import androidx.annotation.OptIn
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.simplevideo.whiteiptv.platform.AudioTrackInfo
import com.simplevideo.whiteiptv.platform.SubtitleTrackInfo
import com.simplevideo.whiteiptv.platform.TracksInfo
import com.simplevideo.whiteiptv.platform.VideoQualityInfo

/**
 * Maps ExoPlayer Tracks to platform-agnostic TracksInfo
 */
@OptIn(UnstableApi::class)
class TracksInfoMapper(
    private val trackSelector: DefaultTrackSelector,
) {

    fun map(tracks: Tracks): TracksInfo {
        val audioTracks = mutableListOf<AudioTrackInfo>()
        val subtitleTracks = mutableListOf<SubtitleTrackInfo>()
        val videoQualities = mutableListOf<VideoQualityInfo>()

        for (trackGroup in tracks.groups) {
            val group = trackGroup.mediaTrackGroup
            for (i in 0 until group.length) {
                val format = group.getFormat(i)
                val isSelected = trackGroup.isTrackSelected(i)
                val trackId = "${group.id}:$i"

                when {
                    format.sampleMimeType?.startsWith("audio/") == true -> {
                        audioTracks.add(
                            AudioTrackInfo(
                                id = trackId,
                                label = format.label ?: format.language ?: "Track ${audioTracks.size + 1}",
                                language = format.language,
                                channelCount = format.channelCount,
                                sampleRate = format.sampleRate,
                                isSelected = isSelected,
                            ),
                        )
                    }

                    format.sampleMimeType?.startsWith("text/") == true ||
                        format.sampleMimeType?.contains("subtitle") == true -> {
                        subtitleTracks.add(
                            SubtitleTrackInfo(
                                id = trackId,
                                label = format.label ?: format.language ?: "Subtitle ${subtitleTracks.size + 1}",
                                language = format.language,
                                isSelected = isSelected,
                            ),
                        )
                    }

                    format.sampleMimeType?.startsWith("video/") == true -> {
                        if (format.width > 0 && format.height > 0) {
                            videoQualities.add(
                                VideoQualityInfo(
                                    id = trackId,
                                    label = "${format.height}p",
                                    width = format.width,
                                    height = format.height,
                                    bitrate = format.bitrate,
                                    isSelected = isSelected,
                                ),
                            )
                        }
                    }
                }
            }
        }

        // Add auto quality option if there are multiple video qualities
        if (videoQualities.size > 1) {
            val isAutoSelected = !trackSelector.parameters.forceHighestSupportedBitrate
            videoQualities.add(
                0,
                VideoQualityInfo(
                    id = "auto",
                    label = "Auto",
                    width = 0,
                    height = 0,
                    bitrate = 0,
                    isSelected = isAutoSelected,
                    isAuto = true,
                ),
            )
        }

        return TracksInfo(
            audioTracks = audioTracks,
            subtitleTracks = subtitleTracks,
            videoQualities = videoQualities.sortedByDescending { if (it.isAuto) Int.MAX_VALUE else it.height },
        )
    }
}
