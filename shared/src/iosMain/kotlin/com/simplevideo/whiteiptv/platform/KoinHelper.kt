package com.simplevideo.whiteiptv.platform

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Helper class to expose Koin dependencies to Swift code.
 * Swift cannot directly call Koin's generic resolution, so this
 * provides typed accessor methods.
 */
class KoinHelper : KoinComponent {
    private val iosBackgroundScheduler: IOSBackgroundScheduler by inject()

    fun getIOSBackgroundScheduler(): IOSBackgroundScheduler = iosBackgroundScheduler

    companion object {
        val shared = KoinHelper()
    }
}
