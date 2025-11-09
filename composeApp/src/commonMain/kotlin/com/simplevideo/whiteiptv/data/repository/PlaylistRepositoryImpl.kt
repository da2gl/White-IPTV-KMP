package com.simplevideo.whiteiptv.data.repository

import com.simplevideo.whiteiptv.data.local.PlaylistDao
import com.simplevideo.whiteiptv.data.local.model.ChannelEntity
import com.simplevideo.whiteiptv.data.local.model.PlaylistEntity
import com.simplevideo.whiteiptv.domain.repository.PlaylistRepository
import com.simplevideo.whiteiptv.util.parseM3u
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val httpClient: HttpClient
) : PlaylistRepository {
    override suspend fun hasPlaylist(): Boolean {
        return playlistDao.getPlaylistCount() > 0
    }

    override suspend fun importPlaylistFromUrl(url: String) {
        val playlistId = playlistDao.insertPlaylist(PlaylistEntity(url = url))
        val m3uString = httpClient.get(url).bodyAsText()
        val channels = parseM3u(m3uString).map {
            ChannelEntity(
                playlistId = playlistId,
                name = it.title,
                url = it.url,
                logoUrl = it.tvgLogo
            )
        }
        playlistDao.insertChannels(channels)
    }
}
