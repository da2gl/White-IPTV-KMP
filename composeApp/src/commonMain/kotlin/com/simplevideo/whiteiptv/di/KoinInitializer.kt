package com.simplevideo.whiteiptv.di

import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE

/**
 * Koin initialization for WhiteIPTV app
 *
 * Call this function in App.kt to initialize dependency injection
 *
 * TODO: Configure Koin logger based on build type (debug/release)
 * TODO: Add platform-specific modules if needed
 */
fun initKoin() {
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

        // Load all modules
        modules(appModules)
    }
}
