package com.simplevideo.whiteiptv.feature.channels

import androidx.lifecycle.viewModelScope
import com.simplevideo.whiteiptv.common.BaseViewModel
import com.simplevideo.whiteiptv.domain.model.ChannelCategory
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import com.simplevideo.whiteiptv.domain.repository.CurrentPlaylistRepository
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsAction
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsEvent
import com.simplevideo.whiteiptv.feature.channels.mvi.ChannelsState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChannelsViewModel(
    private val channelRepository: ChannelRepository,
    private val playlistRepository: PlaylistRepository,
    private val currentPlaylistRepository: CurrentPlaylistRepository,
) : BaseViewModel<ChannelsState, ChannelsAction, ChannelsEvent>(
    initialState = ChannelsState()
) {
    private var initialCategoryId: String? = null
    private var isInitialized = false

    fun setInitialCategory(categoryId: String?) {
        if (!isInitialized) {
            initialCategoryId = categoryId
            isInitialized = true
            loadData()
        }
    }

    private fun loadData() {
        combine(
            playlistRepository.getPlaylists(),
            channelRepository.getAllGroups(),
            currentPlaylistRepository.selectedPlaylistId,
        ) { playlists, groups, selectedPlaylistId ->
            val filteredGroups = if (selectedPlaylistId != null) {
                groups.filter { it.playlistId == selectedPlaylistId }
            } else {
                groups
            }

            val categories = buildList {
                add(ChannelCategory.All)
                add(ChannelCategory.Favorites)
                filteredGroups.forEach { group ->
                    add(
                        ChannelCategory.Group(
                            id = group.id.toString(),
                            displayName = group.name,
                            icon = group.icon,
                            channelCount = group.channelCount,
                            playlistId = group.playlistId,
                        ),
                    )
                }
            }

            val selectedCategory = when (initialCategoryId) {
                null, "_all" -> ChannelCategory.All
                "_favorites" -> ChannelCategory.Favorites
                else -> categories.find { it.id == initialCategoryId } ?: ChannelCategory.All
            }

            Triple(
                playlists to selectedPlaylistId,
                categories,
                selectedCategory,
            )
        }.onEach { (playlistData, categories, selectedCategory) ->
            val (playlists, selectedPlaylistId) = playlistData
            viewState = viewState.copy(
                playlists = playlists,
                selectedPlaylistId = selectedPlaylistId,
                categories = categories,
            )
            selectCategory(selectedCategory)
        }.launchIn(viewModelScope)
    }

    override fun obtainEvent(viewEvent: ChannelsEvent) {
        when (viewEvent) {
            is ChannelsEvent.OnPlaylistSelected -> selectPlaylist(viewEvent.playlistId)
            is ChannelsEvent.OnCategorySelected -> selectCategory(viewEvent.category)
            is ChannelsEvent.OnToggleFavorite -> toggleFavorite(viewEvent.channelId)
        }
    }

    private fun selectPlaylist(playlistId: Long?) {
        currentPlaylistRepository.selectPlaylist(playlistId)
        viewState = viewState.copy(
            selectedCategory = ChannelCategory.All,
            isLoading = true,
        )
    }

    private fun selectCategory(category: ChannelCategory) {
        viewState = viewState.copy(
            selectedCategory = category,
            isLoading = true,
        )

        val selectedPlaylistId = viewState.selectedPlaylistId

        val channelsFlow = when (category) {
            is ChannelCategory.All -> {
                if (selectedPlaylistId != null) {
                    channelRepository.getChannels(selectedPlaylistId)
                } else {
                    channelRepository.getAllChannels()
                }
            }
            is ChannelCategory.Favorites -> channelRepository.getFavoriteChannels()
            is ChannelCategory.Group -> {
                val groupId = category.id.toLongOrNull()
                if (groupId != null) {
                    channelRepository.getChannelsByGroupId(groupId)
                } else {
                    channelRepository.getAllChannels()
                }
            }
        }

        channelsFlow.onEach { channels ->
            viewState = viewState.copy(
                channels = channels,
                selectedCategory = category,
                isLoading = false,
            )
        }.launchIn(viewModelScope)
    }

    private fun toggleFavorite(channelId: Long) {
        viewModelScope.launch {
            try {
                channelRepository.toggleFavoriteStatus(channelId)
            } catch (e: Exception) {
                viewAction = ChannelsAction.ShowError(e.message ?: "Unknown error")
            }
        }
    }
}
