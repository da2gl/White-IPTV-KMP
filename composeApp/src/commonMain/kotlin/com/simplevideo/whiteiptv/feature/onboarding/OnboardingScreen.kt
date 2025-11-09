package com.simplevideo.whiteiptv.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplevideo.whiteiptv.designsystem.AppTypography
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import white_iptv_kmp.composeapp.generated.resources.*

@Composable
fun OnboardingScreen(
    onNavigateToMain: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.viewStates().collectAsStateWithLifecycle()
    val action by viewModel.viewActions().collectAsStateWithLifecycle(initialValue = null)

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
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Header: Logo and App Name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_play_circle),
                    contentDescription = stringResource(Res.string.app_name),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = stringResource(Res.string.app_name),
                    style = AppTypography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Text Block
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(Res.string.onboarding_title),
                    style = AppTypography.headlineLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(Res.string.onboarding_subtitle),
                    style = AppTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Input Section
            Column(
                modifier = Modifier.widthIn(max = 400.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = state.playlistUrl,
                    onValueChange = { onEvent(OnboardingEvent.EnterPlaylistUrl(it)) },
                    label = { Text(stringResource(Res.string.playlist_url_label)) },
                    placeholder = { Text(stringResource(Res.string.playlist_url_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isLoading
                )

                // OR Separator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(Res.string.or_separator),
                        style = AppTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                // File Upload Button
                Button(
                    onClick = { onEvent(OnboardingEvent.ChooseFile) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(text = stringResource(Res.string.choose_file_button))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Primary CTA and Feedback
            Column(
                modifier = Modifier.widthIn(max = 400.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { onEvent(OnboardingEvent.ImportPlaylist) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !state.isLoading && (state.isValidUrl || state.playlistFileName != null)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(text = stringResource(Res.string.import_playlist_button))
                    }
                }

                // Feedback Area
                if (state.isLoading) {
                    Text(
                        text = stringResource(Res.string.importing_message),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (state.error != null) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = { onEvent(OnboardingEvent.UseDemoPlaylist) }) {
                Text(text = stringResource(Res.string.demo_playlist_link))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    OnboardingContent(
        state = OnboardingState(),
        onEvent = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenLoadingPreview() {
    OnboardingContent(
        state = OnboardingState(isLoading = true),
        onEvent = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenErrorPreview() {
    OnboardingContent(
        state = OnboardingState(error = "Invalid playlist format"),
        onEvent = {}
    )
}
