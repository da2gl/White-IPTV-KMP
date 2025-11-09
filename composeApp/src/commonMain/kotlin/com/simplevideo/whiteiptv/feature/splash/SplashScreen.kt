package com.simplevideo.whiteiptv.feature.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.simplevideo.whiteiptv.feature.splash.mvi.SplashAction
import com.simplevideo.whiteiptv.navigation.Route
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    onNavigate: (Route) -> Unit,
) {
    val viewModel: SplashViewModel = koinViewModel()
    val action by viewModel.viewActions().collectAsState(null)

    LaunchedEffect(action) {
        when (val currentAction = action) {
            is SplashAction.Navigate -> {
                onNavigate(currentAction.route)
                viewModel.clearAction()
            }

            null -> {}
        }
    }

    SplashScreenContent()
}

@Composable
private fun SplashScreenContent(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    SplashScreenContent()
}


