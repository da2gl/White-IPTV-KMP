package com.simplevideo.whiteiptv

import androidx.compose.runtime.Composable
import com.simplevideo.whiteiptv.designsystem.AppTheme
import com.simplevideo.whiteiptv.navigation.AppNavGraph
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    AppTheme {
        AppNavGraph()
    }
}
