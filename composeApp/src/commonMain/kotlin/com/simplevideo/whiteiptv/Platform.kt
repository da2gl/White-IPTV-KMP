package com.simplevideo.whiteiptv

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform