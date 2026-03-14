package com.simplevideo.whiteiptv.platform.avplayer

import com.simplevideo.whiteiptv.platform.AudioTrackInfo
import com.simplevideo.whiteiptv.platform.SubtitleTrackInfo
import com.simplevideo.whiteiptv.platform.TracksInfo
import platform.AVFoundation.*
import platform.Foundation.*

/**
 * Maps AVPlayer media selection groups to platform-agnostic TracksInfo
 */
class AVPlayerTracksMapper {

    @Suppress("UNCHECKED_CAST")
    fun map(playerItem: AVPlayerItem): TracksInfo {
        val asset = playerItem.asset
        val currentSelection = playerItem.currentMediaSelection

        val audioTracks = mutableListOf<AudioTrackInfo>()
        val subtitleTracks = mutableListOf<SubtitleTrackInfo>()

        // Map audio tracks
        val audioGroup = asset.mediaSelectionGroupForMediaCharacteristic(AVMediaCharacteristicAudible)
        if (audioGroup != null) {
            val options = audioGroup.options as List<AVMediaSelectionOption>
            val selectedOption = currentSelection.selectedMediaOptionInMediaSelectionGroup(audioGroup)

            options.forEachIndexed { index, option ->
                audioTracks.add(
                    AudioTrackInfo(
                        id = index.toString(),
                        label = option.displayName,
                        language = option.locale?.languageCode,
                        channelCount = 0,
                        sampleRate = 0,
                        isSelected = option == selectedOption,
                    ),
                )
            }
        }

        // Map subtitle tracks
        val subtitleGroup = asset.mediaSelectionGroupForMediaCharacteristic(AVMediaCharacteristicLegible)
        if (subtitleGroup != null) {
            val options = subtitleGroup.options as List<AVMediaSelectionOption>
            val selectedOption = currentSelection.selectedMediaOptionInMediaSelectionGroup(subtitleGroup)

            options.forEachIndexed { index, option ->
                subtitleTracks.add(
                    SubtitleTrackInfo(
                        id = index.toString(),
                        label = option.displayName,
                        language = option.locale?.languageCode,
                        isSelected = option == selectedOption,
                    ),
                )
            }
        }

        return TracksInfo(
            audioTracks = audioTracks,
            subtitleTracks = subtitleTracks,
            videoQualities = emptyList(),
        )
    }
}
