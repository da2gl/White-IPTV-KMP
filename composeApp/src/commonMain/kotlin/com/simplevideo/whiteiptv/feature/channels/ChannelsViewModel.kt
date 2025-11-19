package com.simplevideo.whiteiptv.feature.channels

import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsAction
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsEvent
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsState

class ChannelsViewModel(
//    private val getChannelsUseCase: GetChannelsUseCase,
//    private val getChannelCategoriesUseCase: GetChannelCategoriesUseCase,
//    private val toggleFavoriteStatusUseCase: ToggleFavoriteStatusUseCase
) : BaseViewModel<ChannelsState, ChannelsAction, ChannelsEvent>(
    initialState = ChannelsState()
) {

//    init {
//        loadChannels()
//    }
//
//    override fun obtainEvent(viewEvent: ChannelsEvent) {
//        when (viewEvent) {
//            is ChannelsEvent.OnCategorySelected -> {
//                setState { copy(selectedCategory = viewEvent.category) }
//                filterChannels(viewEvent.category.name)
//            }
//            is ChannelsEvent.OnToggleFavorite -> {
//                toggleFavoriteStatus(viewEvent.channelId)
//            }
//        }
//    }
//
//    private fun loadChannels() {
//        viewModelScope.launch {
//            setState { copy(isLoading = true) }
//            try {
//                val channels = getChannelsUseCase()
//                val categories = mutableListOf(ChannelCategory("All"))
//                categories.addAll(getChannelCategoriesUseCase())
//                setState {
//                    copy(
//                        isLoading = false,
//                        channels = channels,
//                        categories = categories,
//                        selectedCategory = categories.firstOrNull()
//                    )
//                }
//            } catch (e: Exception) {
//                setState { copy(isLoading = false, error = e.message) }
//            }
//        }
//    }
//
//    private fun toggleFavoriteStatus(channelId: String) {
//        viewModelScope.launch {
//            try {
//                val updatedChannel = toggleFavoriteStatusUseCase(channelId)
//                setState {
//                    copy(channels = channels.map {
//                        if (it.id == updatedChannel.id) {
//                            updatedChannel
//                        } else {
//                            it
//                        }
//                    })
//                }
//            } catch (e: Exception) {
//                setAction(ChannelsAction.ShowError(e.message ?: "Unknown error"))
//            }
//        }
//    }
//
//    private fun filterChannels(category: String) {
//        viewModelScope.launch {
//            val allChannels = getChannelsUseCase()
//            val filteredChannels = if (category == "All") {
//                allChannels
//            } else {
//                allChannels.filter { it.category == category }
//            }
//            setState { copy(channels = filteredChannels) }
//        }
//    }
}
