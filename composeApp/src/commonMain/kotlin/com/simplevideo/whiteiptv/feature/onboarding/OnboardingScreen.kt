package com.simplevideo.whiteiptv.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

/**
 * Onboarding screen for WhiteIPTV
 *
 * Simple screen for importing IPTV playlist
 *
 * TODO: Add app logo/icon
 * TODO: Implement file picker platform-specific logic
 * TODO: Add loading animation during import
 * TODO: Add success animation after import
 */
@Composable
fun OnboardingScreen(
    onNavigateToMain: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.viewStates().collectAsStateWithLifecycle()
    val action by viewModel.viewActions().collectAsStateWithLifecycle(initialValue = null)

    // Handle actions (side effects)
    LaunchedEffect(action) {
        when (action) {
            is OnboardingAction.NavigateToMain -> {
                onNavigateToMain()
                viewModel.clearAction()
            }

            is OnboardingAction.ShowFilePicker -> {
                // TODO: Show platform-specific file picker
                viewModel.clearAction()
            }

            is OnboardingAction.ShowError -> {
                // Error is shown in UI via state.error
                viewModel.clearAction()
            }

            is OnboardingAction.ShowSuccess -> {
                // TODO: Show success snackbar or animation
                viewModel.clearAction()
            }

            null -> { /* No action */
            }
        }
    }

    OnboardingContent(
        state = state,
        onEvent = viewModel::obtainEvent,
    )
}

@Composable
private fun OnboardingContent(
    state: OnboardingState,
    onEvent: (OnboardingEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Logo placeholder
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "App logo",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            // App name
            Text(
                text = "Streamify",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = "Upload your IPTV playlist",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            // Subtitle
            Text(
                text = "Enter a link or choose a file to start watching",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Playlist URL input
            OutlinedTextField(
                value = state.playlistUrl,
                onValueChange = { onEvent(OnboardingEvent.EnterPlaylistUrl(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Playlist URL") },
                placeholder = { Text("https://...") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                singleLine = true,
                enabled = !state.isLoading,
            )

            // OR divider
            Text(
                text = "OR",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Choose file button
            OutlinedButton(
                onClick = { onEvent(OnboardingEvent.ChooseFile) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
            ) {
                Text(
                    text = state.playlistFileName ?: "Choose file",
                    modifier = Modifier.padding(8.dp),
                )
            }

            // Import button
            Button(
                onClick = { onEvent(OnboardingEvent.ImportPlaylist) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && (state.isValidUrl || state.playlistFileName != null),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(
                        text = "Import playlist",
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }

            // Error message
            if (state.error != null) {
                Text(
                    text = "⚠ ${state.error}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }

            // Use demo playlist link
            TextButton(
                onClick = { onEvent(OnboardingEvent.UseDemoPlaylist) },
                enabled = !state.isLoading,
            ) {
                Text("Use demo playlist")
            }
        }
    }
}
