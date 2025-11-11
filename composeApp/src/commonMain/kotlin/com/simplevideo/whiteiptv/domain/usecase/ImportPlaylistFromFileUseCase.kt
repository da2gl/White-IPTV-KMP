package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.mapper.ChannelMapper
import com.simplevideo.whiteiptv.data.mapper.PlaylistMapper
import com.simplevideo.whiteiptv.data.parser.playlist.M3uParser
import com.simplevideo.whiteiptv.data.parser.playlist.model.Channel
import com.simplevideo.whiteiptv.data.parser.playlist.model.PlaylistHeader
import com.simplevideo.whiteiptv.domain.exception.PlaylistException
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import com.simplevideo.whiteiptv.platform.FileReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for importing IPTV playlist from local file
 *
 * Similar to ImportPlaylistUseCase but reads from file instead of HTTP
 */
class ImportPlaylistFromFileUseCase(
    private val repository: PlaylistRepository,
    private val fileReader: FileReader,
    private val channelMapper: ChannelMapper,
    private val playlistMapper: PlaylistMapper,
) {

    companion object {
        private const val BATCH_SIZE = 1000
    }

    suspend operator fun invoke(fileUri: String, fileName: String) {
        try {
            // Read file content
            val m3uString = fileReader.readFile(fileUri)

            if (m3uString.isBlank() || !m3uString.contains("#EXTM3U")) {
                throw PlaylistException.ParseError("Invalid M3U format: missing #EXTM3U header")
            }

            // CPU-intensive, move to Default dispatcher
            val (header, channels) = withContext(Dispatchers.Default) {
                try {
                    M3uParser.parse(m3uString)
                } catch (e: Exception) {
                    throw PlaylistException.ParseError("Failed to parse M3U content", e)
                }
            }

            if (channels.isEmpty()) {
                throw PlaylistException.EmptyPlaylist()
            }

            // Use fileName as URL for local files
            val localUrl = "file://$fileName"

            val existingPlaylist = try {
                repository.getPlaylistByUrl(localUrl)
            } catch (e: Exception) {
                throw PlaylistException.DatabaseError("Failed to check existing playlist", e)
            }

            try {
                if (existingPlaylist != null) {
                    updateExistingPlaylist(existingPlaylist.id, localUrl, header, channels)
                } else {
                    insertNewPlaylist(localUrl, header, channels)
                }
            } catch (e: PlaylistException) {
                throw e
            } catch (e: Exception) {
                throw PlaylistException.DatabaseError("Failed to save playlist", e)
            }
        } catch (e: PlaylistException) {
            throw e
        } catch (e: Exception) {
            throw PlaylistException.Unknown("Unexpected error during file import", e)
        }
    }

    private suspend fun insertNewPlaylist(
        url: String,
        header: PlaylistHeader,
        channels: List<Channel>,
    ) {
        val playlistEntity = playlistMapper.toEntity(
            url = url,
            header = header,
            channelCount = channels.size,
        )

        val playlistId = repository.insertPlaylist(playlistEntity)

        // CPU-intensive, move to Default dispatcher
        val channelEntities = withContext(Dispatchers.Default) {
            channelMapper.toEntityList(
                playlistId = playlistId,
                channels = channels,
            )
        }

        insertChannelsBatched(channelEntities)
    }

    private suspend fun updateExistingPlaylist(
        playlistId: Long,
        url: String,
        header: PlaylistHeader,
        newChannels: List<Channel>,
    ) {
        val existingPlaylist = repository.getPlaylistById(playlistId)
            ?: throw PlaylistException.NotFound(playlistId)

        val updatedPlaylist = playlistMapper.updateEntity(
            existing = existingPlaylist,
            header = header,
            channelCount = newChannels.size,
        )
        repository.updatePlaylist(updatedPlaylist)

        val existingChannels = repository.getChannelsList(playlistId)

        // CPU-intensive: build favorites sets and map channels
        val newChannelEntities = withContext(Dispatchers.Default) {
            val favoritesTvgIds = existingChannels
                .filter { it.isFavorite && it.tvgId != null }
                .mapTo(mutableSetOf()) { it.tvgId }

            val favoritesUrls = existingChannels
                .filter { it.isFavorite }
                .mapTo(mutableSetOf()) { it.url }

            channelMapper.toEntityList(
                playlistId = playlistId,
                channels = newChannels,
                favoritesTvgIds = favoritesTvgIds,
                favoritesUrls = favoritesUrls,
            )
        }

        repository.deleteChannelsByPlaylistId(playlistId)
        insertChannelsBatched(newChannelEntities)
    }

    private suspend fun insertChannelsBatched(
        channels: List<ChannelEntity>,
    ) {
        if (channels.size > BATCH_SIZE) {
            channels.chunked(BATCH_SIZE).forEach { batch ->
                repository.insertChannels(batch)
            }
        } else {
            repository.insertChannels(channels)
        }
    }
}