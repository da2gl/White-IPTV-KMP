package com.simplevideo.whiteiptv

import androidx.compose.ui.window.ComposeUIViewController
import com.simplevideo.whiteiptv.di.initializeKoin
import org.koin.core.logger.Level

fun MainViewController() = ComposeUIViewController(
    configure = {
        initializeKoin {
            printLogger(Level.INFO)
        }
    },
) { App() }
