package com.simplevideo.whiteiptv.data.network

/**
 * Network configuration constants
 */
object NetworkConfig {
    /**
     * Timeout for establishing connection (milliseconds)
     */
    const val CONNECT_TIMEOUT = 15_000L

    /**
     * Timeout for reading data from socket (milliseconds)
     */
    const val SOCKET_TIMEOUT = 15_000L

    /**
     * Timeout for the entire request (milliseconds)
     */
    const val REQUEST_TIMEOUT = 30_000L
}
