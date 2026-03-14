package com.simplevideo.whiteiptv.data.mapper

import com.simplevideo.whiteiptv.data.local.model.ChannelGroupEntity
import com.simplevideo.whiteiptv.domain.model.ChannelGroup

class ChannelGroupMapper {
    fun toDomain(entity: ChannelGroupEntity): ChannelGroup =
        ChannelGroup(
            id = entity.id.toString(),
            displayName = entity.name,
            icon = entity.icon,
            channelCount = entity.channelCount,
            playlistId = entity.playlistId,
        )

    fun toDomainList(entities: List<ChannelGroupEntity>): List<ChannelGroup> =
        entities.map { toDomain(it) }
}
