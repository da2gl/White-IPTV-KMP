package com.simplevideo.whiteiptv.domain.usecase

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.mapper.ChannelMapper
import com.simplevideo.whiteiptv.data.mapper.PlaylistMapper
import com.simplevideo.whiteiptv.data.parser.playlist.M3uParser
import com.simplevideo.whiteiptv.data.parser.playlist.model.Channel
import com.simplevideo.whiteiptv.data.parser.playlist.model.PlaylistHeader
import com.simplevideo.whiteiptv.domain.exception.PlaylistException
import com.simplevideo.whiteiptv.domain.model.PlaylistSource
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import com.simplevideo.whiteiptv.platform.FileReader
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for importing IPTV playlist from URL or local file
 *
 * Handles:
 * - URL validation and download OR file reading
 * - M3U parsing
 * - New playlist creation or existing playlist update
 * - Preserving user preferences (favorites) on update
 * - Batch insertion for large playlists
 *
 * Usage:
 * ```kotlin
 * val useCase = ImportPlaylistUseCase(...)
 * useCase(PlaylistSource.Url("https://example.com/playlist.m3u"))
 * useCase(PlaylistSource.LocalFile(uri = "content://...", fileName = "playlist.m3u"))
 * ```
 */
class ImportPlaylistUseCase(
    private val repository: PlaylistRepository,
    private val httpClient: HttpClient,
    private val fileReader: FileReader,
    private val channelMapper: ChannelMapper,
    private val playlistMapper: PlaylistMapper,
) {

    companion object {
        /**
         * Batch size for channel insertion
         * Optimizes memory usage for large playlists (50K+ channels)
         */
        private const val BATCH_SIZE = 1000
    }

    suspend operator fun invoke(source: PlaylistSource) {
        try {
            val m3uString = when (source) {
                is PlaylistSource.Url -> downloadFromUrl(source.url)
                is PlaylistSource.LocalFile -> fileReader.readFile(source.uri)
            }

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

            val playlistUrl = when (source) {
                is PlaylistSource.Url -> source.url
                is PlaylistSource.LocalFile -> "file://${source.fileName}"
            }

            val existingPlaylist = try {
                repository.getPlaylistByUrl(playlistUrl)
            } catch (e: Exception) {
                throw PlaylistException.DatabaseError("Failed to check existing playlist", e)
            }

            try {
                if (existingPlaylist != null) {
                    updateExistingPlaylist(existingPlaylist.id, header, channels)
                } else {
                    insertNewPlaylist(playlistUrl, header, channels)
                }
            } catch (e: PlaylistException) {
                throw e
            } catch (e: Exception) {
                throw PlaylistException.DatabaseError("Failed to save playlist", e)
            }
        } catch (e: PlaylistException) {
            throw e
        } catch (e: HttpRequestTimeoutException) {
            throw PlaylistException.NetworkError("Request timeout", e)
        } catch (e: Exception) {
            throw PlaylistException.Unknown("Unexpected error during playlist import", e)
        }
    }

    private suspend fun downloadFromUrl(url: String): String {
        if (!isValidUrl(url)) {
            throw PlaylistException.InvalidUrl(url)
        }

        val response = httpClient.get(url)

        if (!response.status.isSuccess()) {
            throw PlaylistException.NetworkError(
                "Failed to download playlist: HTTP ${response.status.value}",
            )
        }

        return response.bodyAsText()
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

    /**
     * Update existing playlist: refresh metadata and channels
     * Preserves user preferences (isFavorite) by matching channels by tvgId or URL
     */
    private suspend fun updateExistingPlaylist(
        playlistId: Long,
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

    private fun isValidUrl(url: String): Boolean {
        return url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))
    }
}
