package com.simplevideo.whiteiptv.util

import com.simplevideo.whiteiptv.data.local.model.ChannelEntity

data class M3uChannel(
    val tvgId: String?,
    val tvgName: String?,
    val tvgLogo: String?,
    val groupTitle: String?,
    val title: String,
    val url: String
)

fun parseM3u(m3uString: String): List<M3uChannel> {
    val channels = mutableListOf<M3uChannel>()
    val lines = m3uString.lines()

    var i = 0
    while (i < lines.size) {
        val line = lines[i]
        if (line.startsWith("#EXTINF")) {
            val attributes = line.substringAfter("#EXTINF:-1 ").trim()
            val title = attributes.substringAfterLast(",").trim()
            val url = lines.getOrNull(i + 1)?.trim()

            if (url != null && url.isNotBlank()) {
                val tvgId = Regex("tvg-id=\"(.*?)\"").find(attributes)?.groupValues?.get(1)
                val tvgName = Regex("tvg-name=\"(.*?)\"").find(attributes)?.groupValues?.get(1)
                val tvgLogo = Regex("tvg-logo=\"(.*?)\"").find(attributes)?.groupValues?.get(1)
                val groupTitle = Regex("group-title=\"(.*?)\"").find(attributes)?.groupValues?.get(1)

                channels.add(
                    M3uChannel(
                        tvgId = tvgId,
                        tvgName = tvgName,
                        tvgLogo = tvgLogo,
                        groupTitle = groupTitle,
                        title = title,
                        url = url
                    )
                )
                i++
            }
        }
        i++
    }
    return channels
}
