package com.simplevideo.whiteiptv.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE

/**
 * Koin initialization for WhiteIPTV app
 *
 * Call this function to initialize dependency injection
 * Pass platform-specific configuration via the config lambda
 */
fun initializeKoin(config: (KoinApplication.() -> Unit)? = null) {
    startKoin {
        // Enable Koin logger for debug
        logger(
            object : Logger() {
                override fun display(level: Level, msg: MESSAGE) {
                    when (level) {
                        Level.DEBUG -> println("DEBUG: $msg")
                        Level.INFO -> println("INFO: $msg")
                        Level.ERROR -> println("ERROR: $msg")
                        Level.WARNING -> println("WARNING: $msg")
                        else -> println(msg)
                    }
                }
            },
        )

        // Platform-specific configuration (e.g., androidContext on Android)
        config?.invoke(this)

        // Load all modules
        modules(appModules)
    }
}
