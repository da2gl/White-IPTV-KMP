package com.simplevideo.whiteiptv.domain.model

data class Channel(
    val id: String,
    val name: String,
    val logoUrl: String,
    val category: String,
    val isFavorite: Boolean
)
