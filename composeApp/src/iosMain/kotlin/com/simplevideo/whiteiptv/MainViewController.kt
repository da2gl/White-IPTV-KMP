package com.simplevideo.whiteiptv

import androidx.compose.ui.window.ComposeUIViewController
import com.simplevideo.whiteiptv.di.initializeKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initializeKoin() },
) { App() }
