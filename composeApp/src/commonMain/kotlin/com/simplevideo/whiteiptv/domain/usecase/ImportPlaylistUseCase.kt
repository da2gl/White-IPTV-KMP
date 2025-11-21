package com.simplevideo.whiteiptv.domain.usecase

import co.touchlab.kermit.Logger
import com.simplevideo.whiteiptv.common.AppLogger
import com.simplevideo.whiteiptv.data.mapper.ChannelMapper
import com.simplevideo.whiteiptv.data.mapper.PlaylistMapper
import com.simplevideo.whiteiptv.data.parser.playlist.M3uParser
import com.simplevideo.whiteiptv.data.parser.playlist.model.Channel
import com.simplevideo.whiteiptv.data.parser.playlist.model.PlaylistHeader
import com.simplevideo.whiteiptv.domain.exception.PlaylistException
import com.simplevideo.whiteiptv.domain.model.PlaylistSource
import com.simplevideo.whiteiptv.domain.repository.ChannelRepository
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import com.simplevideo.whiteiptv.platform.FileReader
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    private val playlistRepository: PlaylistRepository,
    private val channelRepository: ChannelRepository,
    private val httpClient: HttpClient,
    private val fileReader: FileReader,
    private val channelMapper: ChannelMapper,
    private val playlistMapper: PlaylistMapper,
) {

    private val log: Logger = Logger.withTag(AppLogger.Tags.IMPORT)

    suspend operator fun invoke(source: PlaylistSource) {
        log.i { "Starting playlist import from ${source::class.simpleName}" }
        try {
            val m3uString = when (source) {
                is PlaylistSource.Url -> downloadFromUrl(source.url)
                is PlaylistSource.LocalFile -> fileReader.readFile(source.uri)
            }

            log.d { "Received M3U content: ${m3uString.length} characters" }

            if (m3uString.isBlank() || !m3uString.contains("#EXTM3U")) {
                log.e { "Invalid M3U format: missing #EXTM3U header" }
                throw PlaylistException.ParseError("Invalid M3U format: missing #EXTM3U header")
            }

            log.d { "Parsing M3U content..." }
            // CPU-intensive, move to Default dispatcher
            val (header, channels) = withContext(Dispatchers.Default) {
                try {
                    M3uParser.parse(m3uString)
                } catch (e: Exception) {
                    log.e(e) { "Failed to parse M3U content" }
                    throw PlaylistException.ParseError("Failed to parse M3U content", e)
                }
            }

            if (channels.isEmpty()) {
                log.w { "Parsed playlist contains no channels" }
                throw PlaylistException.EmptyPlaylist()
            }

            log.i { "Successfully parsed ${channels.size} channels" }
            log.d { "Playlist header: urlTvg=${header.urlTvg ?: "N/A"}, refresh=${header.refresh ?: "N/A"}" }

            val playlistUrl = when (source) {
                is PlaylistSource.Url -> source.url
                is PlaylistSource.LocalFile -> "file://${source.fileName}"
            }

            val existingPlaylist = try {
                playlistRepository.getPlaylistByUrl(playlistUrl)
            } catch (e: Exception) {
                log.e(e) { "Failed to check existing playlist" }
                throw PlaylistException.DatabaseError("Failed to check existing playlist", e)
            }

            try {
                if (existingPlaylist != null) {
                    log.i { "Updating existing playlist (id=${existingPlaylist.id})" }
                    updateExistingPlaylist(existingPlaylist.id, header, channels)
                } else {
                    log.i { "Inserting new playlist" }
                    insertNewPlaylist(playlistUrl, header, channels)
                }
                log.i { "Playlist import completed successfully" }
            } catch (e: PlaylistException) {
                throw e
            } catch (e: Exception) {
                log.e(e) { "Failed to save playlist" }
                throw PlaylistException.DatabaseError("Failed to save playlist", e)
            }
        } catch (e: PlaylistException) {
            log.e(e) { "Playlist import failed: ${e::class.simpleName}" }
            throw e
        } catch (e: HttpRequestTimeoutException) {
            log.e(e) { "HTTP request timeout" }
            throw PlaylistException.NetworkError("Request timeout", e)
        } catch (e: Exception) {
            // Handle network exceptions (ConnectException, UnknownHostException, etc.)
            val isNetworkError = e::class.simpleName in listOf(
                "ConnectException",
                "UnknownHostException",
                "SocketException",
                "NoRouteToHostException",
            ) || e.message?.contains("Connection refused") == true

            if (isNetworkError) {
                log.e(e) { "Network error: ${e::class.simpleName}" }
                throw PlaylistException.NetworkError("Connection failed. Check your internet connection", e)
            }

            log.e(e) { "Unexpected error during playlist import" }
            throw PlaylistException.Unknown("Unexpected error during playlist import", e)
        }
    }

    private suspend fun downloadFromUrl(url: String): String {
        if (!isValidUrl(url)) {
            log.e { "Invalid URL: $url" }
            throw PlaylistException.InvalidUrl(url)
        }

        log.d { "Downloading playlist from $url" }
        val response = httpClient.get(url)

        if (!response.status.isSuccess()) {
            log.e { "Failed to download playlist: HTTP ${response.status.value}" }
            throw PlaylistException.NetworkError(
                "Failed to download playlist: HTTP ${response.status.value}",
            )
        }

        log.d { "Successfully downloaded playlist" }
        return response.bodyAsText()
    }

    private suspend fun insertNewPlaylist(
        url: String,
        header: PlaylistHeader,
        channels: List<Channel>,
    ) {
        log.d { "Preparing data for new playlist..." }

        // CPU-intensive: prepare all data in parallel
        val (playlistEntity, groups, channelEntities) = withContext(Dispatchers.Default) {
            coroutineScope {
                val playlist = playlistMapper.toEntity(
                    url = url,
                    header = header,
                    channelCount = channels.size,
                )

                // Run mapping operations in parallel
                val groupsDeferred = async { channelMapper.extractGroups(playlistId = 0, channels) }
                val entitiesDeferred = async { channelMapper.toEntityList(playlistId = 0, channels) }

                Triple(playlist, groupsDeferred.await(), entitiesDeferred.await())
            }
        }

        log.d { "Data prepared: ${channelEntities.size} channels, ${groups.size} groups" }
        log.d { "Saving to database..." }

        // All database operations
        val playlistId = playlistRepository.importPlaylistData(
            playlist = playlistEntity,
            groups = groups,
            channels = channelEntities,
            crossRefsProvider = { channelIds, groupIds ->
                // Create mapping without zip: group name -> database ID
                val groupNameToId = buildMap(groups.size) {
                    for (i in groups.indices) {
                        put(groups[i].name, groupIds[i])
                    }
                }
                val crossRefs = channelMapper.createCrossRefsWithIds(channelIds, channels, groupNameToId)
                log.d { "Created ${crossRefs.size} channel-group cross-references" }
                crossRefs
            },
        )

        log.i { "New playlist saved with id=$playlistId" }
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
        val existingPlaylist = playlistRepository.getPlaylistById(playlistId)
            ?: throw PlaylistException.NotFound(playlistId)

        val existingChannels = channelRepository.getChannelsList(playlistId)
        log.d { "Found ${existingChannels.size} existing channels" }

        log.d { "Preparing data for playlist update..." }

        // CPU-intensive: prepare all data in parallel
        val (updatedPlaylist, groups, channelEntities) = withContext(Dispatchers.Default) {
            coroutineScope {
                val playlist = playlistMapper.updateEntity(
                    existing = existingPlaylist,
                    header = header,
                    channelCount = newChannels.size,
                )

                // Build favorites sets for preserving user preferences
                val favoritesTvgIds = existingChannels
                    .filter { it.isFavorite && it.tvgId != null }
                    .mapTo(mutableSetOf()) { it.tvgId }

                val favoritesUrls = existingChannels
                    .filter { it.isFavorite }
                    .mapTo(mutableSetOf()) { it.url }

                // Run mapping operations in parallel
                val groupsDeferred = async { channelMapper.extractGroups(playlistId = 0, newChannels) }
                val entitiesDeferred = async {
                    channelMapper.toEntityList(
                        playlistId = 0,
                        channels = newChannels,
                        favoritesTvgIds = favoritesTvgIds,
                        favoritesUrls = favoritesUrls,
                    )
                }

                Triple(playlist, groupsDeferred.await(), entitiesDeferred.await())
            }
        }

        val favoritesCount = channelEntities.count { it.isFavorite }
        log.d { "Data prepared: ${channelEntities.size} channels, ${groups.size} groups, $favoritesCount favorites preserved" }
        log.d { "Updating database..." }

        // All database operations
        playlistRepository.updatePlaylistData(
            playlist = updatedPlaylist,
            groups = groups,
            channels = channelEntities,
            crossRefsProvider = { channelIds, groupIds ->
                // Create mapping without zip: group name -> database ID
                val groupNameToId = buildMap(groups.size) {
                    for (i in groups.indices) {
                        put(groups[i].name, groupIds[i])
                    }
                }
                val crossRefs = channelMapper.createCrossRefsWithIds(channelIds, newChannels, groupNameToId)
                log.d { "Created ${crossRefs.size} channel-group cross-references" }
                crossRefs
            },
        )

        log.i { "Playlist id=$playlistId updated successfully" }
    }

    private fun isValidUrl(url: String): Boolean {
        return url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))
    }
}
