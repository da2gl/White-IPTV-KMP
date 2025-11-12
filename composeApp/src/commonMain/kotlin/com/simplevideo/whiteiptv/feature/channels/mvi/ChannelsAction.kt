package com.simplevideo.whiteiptv.feature.channels.mvi

sealed interface ChannelsAction {
    data class ShowError(val message: String) : ChannelsAction
}
