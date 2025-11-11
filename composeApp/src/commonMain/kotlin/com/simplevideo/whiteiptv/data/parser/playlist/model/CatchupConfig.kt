package com.simplevideo.whiteiptv.data.parser.playlist.model

/**
 * Catchup TV configuration for time-shifted viewing
 *
 * Catchup TV allows users to watch previously broadcast content
 */
data class CatchupConfig(
    /**
     * Whether catchup is enabled for this channel
     */
    val enabled: Boolean,

    /**
     * Type of catchup implementation
     * - default: Basic catchup with source template
     * - append: Append time parameters to URL
     * - shift: Time shift based implementation
     * - flussonic: Flussonic server format
     * - xtream: Xtream Codes format
     * - fs: FlussоnicStreaming format
     */
    val type: CatchupType,

    /**
     * URL template for catchup with placeholders:
     * - ${start} - Unix timestamp of program start
     * - ${timestamp} - Unix timestamp
     * - ${duration} - Program duration in seconds
     * - ${offset} - Time offset
     * - ${utc} - UTC time
     * - ${lutc} - Local UTC time
     */
    val source: String? = null,

    /**
     * Number of days available for catchup
     */
    val days: Int? = null,

    /**
     * Time correction in seconds (can be negative)
     */
    val correction: Int? = null,
)

/**
 * Types of catchup implementations
 */
enum class CatchupType {
    DEFAULT,
    APPEND,
    SHIFT,
    FLUSSONIC,
    XTREAM,
    FS;

    companion object {
        fun fromString(value: String?): CatchupType {
            return when (value?.lowercase()) {
                "default" -> DEFAULT
                "append" -> APPEND
                "shift" -> SHIFT
                "flussonic", "flussonic-hls", "flussonic-ts" -> FLUSSONIC
                "xtream", "xtream-codes" -> XTREAM
                "fs" -> FS
                else -> DEFAULT
            }
        }
    }
}
