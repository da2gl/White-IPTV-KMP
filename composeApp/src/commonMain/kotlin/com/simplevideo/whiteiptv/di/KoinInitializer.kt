package com.simplevideo.whiteiptv.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

/**
 * Koin initialization for WhiteIPTV app
 *
 * Call this function to initialize dependency injection
 * Pass platform-specific configuration via the config lambda
 */
fun initializeKoin(config: (KoinApplication.() -> Unit)? = null) {
    startKoin {
        // Platform-specific configuration (logger, context, etc.)
        config?.invoke(this)

        // Load all modules
        modules(appModules)
    }
}
