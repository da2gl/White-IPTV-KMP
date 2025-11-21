package com.simplevideo.whiteiptv.common

/**
 * Centralized logging tags for the application.
 *
 * Use hierarchical tags for easy filtering in Logcat:
 * - "WhiteIPTV" - all app logs
 * - "WhiteIPTV:Import" - playlist import operations
 * - "WhiteIPTV:HTTP" - network requests
 */
object AppLogger {
    const val BASE_TAG = "WhiteIPTV"

    object Tags {
        const val IMPORT = "$BASE_TAG:Import"
        const val HTTP = "$BASE_TAG:HTTP"
        const val DATABASE = "$BASE_TAG:Database"
        const val HOME = "$BASE_TAG:Home"
        const val ONBOARDING = "$BASE_TAG:Onboarding"
    }
}
