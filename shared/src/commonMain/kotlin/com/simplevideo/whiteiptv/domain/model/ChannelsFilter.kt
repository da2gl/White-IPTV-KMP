package com.simplevideo.whiteiptv.domain.model

sealed interface ChannelsFilter {
    data object All : ChannelsFilter
    data class ByPlaylist(val playlistId: Long) : ChannelsFilter
    data class ByGroup(val groupId: Long) : ChannelsFilter
}
