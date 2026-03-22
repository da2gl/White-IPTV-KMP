package com.simplevideo.whiteiptv.platform

import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class AndroidFullscreenSheetController : FullscreenSheetController {
    @Composable
    override fun Effect() {
        val view = LocalView.current
        SideEffect {
            val window = getWindow(view) ?: return@SideEffect
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

private fun getWindow(view: View): Window? {
    val dialogWindow = findDialogWindow(view)
    if (dialogWindow != null) return dialogWindow
    return findActivityWindow(view)
}

private fun findDialogWindow(view: View): Window? {
    var parent: android.view.ViewParent? = view.parent
    while (parent != null) {
        if (parent is androidx.compose.ui.window.DialogWindowProvider) {
            return parent.window
        }
        parent = parent.parent
    }
    return null
}

private fun findActivityWindow(view: View): Window? {
    var context = view.context
    while (context is android.content.ContextWrapper) {
        if (context is android.app.Activity) {
            return context.window
        }
        context = context.baseContext
    }
    return null
}
