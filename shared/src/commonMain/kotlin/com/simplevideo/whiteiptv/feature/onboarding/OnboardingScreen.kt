package com.simplevideo.whiteiptv.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simplevideo.whiteiptv.designsystem.AppTheme
import com.simplevideo.whiteiptv.designsystem.AppTypography
import com.simplevideo.whiteiptv.feature.onboarding.mvi.ImportError
import com.simplevideo.whiteiptv.feature.onboarding.mvi.OnboardingAction
import com.simplevideo.whiteiptv.feature.onboarding.mvi.OnboardingEvent
import com.simplevideo.whiteiptv.feature.onboarding.mvi.OnboardingState
import com.simplevideo.whiteiptv.platform.FilePickerFactory
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import white_iptv_kmp.shared.generated.resources.Res
import white_iptv_kmp.shared.generated.resources.app_name
import white_iptv_kmp.shared.generated.resources.choose_file_button
import white_iptv_kmp.shared.generated.resources.demo_playlist_link
import white_iptv_kmp.shared.generated.resources.ic_play_circle
import white_iptv_kmp.shared.generated.resources.import_playlist_button
import white_iptv_kmp.shared.generated.resources.importing_message
import white_iptv_kmp.shared.generated.resources.onboarding_subtitle
import white_iptv_kmp.shared.generated.resources.onboarding_title
import white_iptv_kmp.shared.generated.resources.or_separator
import white_iptv_kmp.shared.generated.resources.error_empty_playlist
import white_iptv_kmp.shared.generated.resources.error_http_403
import white_iptv_kmp.shared.generated.resources.error_http_404
import white_iptv_kmp.shared.generated.resources.error_http_500
import white_iptv_kmp.shared.generated.resources.error_http_generic
import white_iptv_kmp.shared.generated.resources.error_invalid_format
import white_iptv_kmp.shared.generated.resources.error_invalid_url
import white_iptv_kmp.shared.generated.resources.error_no_connection
import white_iptv_kmp.shared.generated.resources.error_server_not_found
import white_iptv_kmp.shared.generated.resources.error_storage
import white_iptv_kmp.shared.generated.resources.error_timeout
import white_iptv_kmp.shared.generated.resources.error_unknown
import white_iptv_kmp.shared.generated.resources.playlist_url_label
import white_iptv_kmp.shared.generated.resources.playlist_url_placeholder

@Composable
fun OnboardingScreen(
    onNavigateToMain: () -> Unit,
) {
    val viewModel = koinViewModel<OnboardingViewModel>()
    val filePicker = koinInject<FilePickerFactory>().createFilePicker()
    val state by viewModel.viewStates().collectAsStateWithLifecycle()
    val action by viewModel.viewActions().collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(action) {
        when (action) {
            is OnboardingAction.NavigateToMain -> {
                onNavigateToMain()
                viewModel.clearAction()
            }

            is OnboardingAction.ShowFilePicker -> {
                filePicker.pickFile { uri, fileName ->
                    viewModel.obtainEvent(OnboardingEvent.FileSelected(fileName, uri))
                }
                viewModel.clearAction()
            }

            else -> Unit
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
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Header: Logo and App Name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_play_circle),
                    contentDescription = stringResource(Res.string.app_name),
                    modifier = Modifier.size(64.dp),
                )
                Text(
                    text = stringResource(Res.string.app_name),
                    style = AppTypography.headlineSmall,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Text Block
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.onboarding_title),
                    style = AppTypography.headlineLarge,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.onboarding_subtitle),
                    style = AppTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input Section
            Column(
                modifier = Modifier.widthIn(max = 400.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = state.playlistUrl,
                    onValueChange = { onEvent(OnboardingEvent.EnterPlaylistUrl(it)) },
                    label = { Text(stringResource(Res.string.playlist_url_label)) },
                    placeholder = { Text(stringResource(Res.string.playlist_url_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isLoading,
                )

                // OR Separator
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                ) {
                    Text(text = stringResource(Res.string.choose_file_button))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary CTA and Feedback
            Column(
                modifier = Modifier.widthIn(max = 400.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Button(
                    onClick = { onEvent(OnboardingEvent.ImportPlaylist) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !state.isLoading && (state.isValidUrl || state.playlistFileName != null),
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(text = stringResource(Res.string.import_playlist_button))
                    }
                }

                // Feedback Area
                if (state.isLoading) {
                    Text(
                        text = stringResource(Res.string.importing_message),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else if (state.error != null) {
                    Text(
                        text = resolveErrorMessage(state.error),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = { onEvent(OnboardingEvent.UseDemoPlaylist) }) {
                Text(text = stringResource(Res.string.demo_playlist_link))
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun resolveErrorMessage(error: ImportError): String {
    return when (error) {
        is ImportError.NoConnection -> stringResource(Res.string.error_no_connection)
        is ImportError.ServerNotFound -> stringResource(Res.string.error_server_not_found)
        is ImportError.Timeout -> stringResource(Res.string.error_timeout)
        is ImportError.HttpError -> when (error.statusCode) {
            403 -> stringResource(Res.string.error_http_403)
            404 -> stringResource(Res.string.error_http_404)
            in 500..599 -> stringResource(Res.string.error_http_500)
            else -> stringResource(Res.string.error_http_generic, error.statusCode)
        }
        is ImportError.InvalidUrl -> stringResource(Res.string.error_invalid_url)
        is ImportError.InvalidFormat -> stringResource(Res.string.error_invalid_format)
        is ImportError.EmptyPlaylist -> stringResource(Res.string.error_empty_playlist)
        is ImportError.StorageError -> stringResource(Res.string.error_storage)
        is ImportError.Unknown -> stringResource(Res.string.error_unknown)
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    AppTheme {
        OnboardingContent(
            state = OnboardingState(),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenLoadingPreview() {
    AppTheme {
        OnboardingContent(
            state = OnboardingState(isLoading = true),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenErrorPreview() {
    AppTheme {
        OnboardingContent(
            state = OnboardingState(error = ImportError.InvalidFormat),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenDarkPreview() {
    AppTheme(darkTheme = true) {
        OnboardingContent(
            state = OnboardingState(),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenLoadingDarkPreview() {
    AppTheme(darkTheme = true) {
        OnboardingContent(
            state = OnboardingState(isLoading = true),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenErrorDarkPreview() {
    AppTheme(darkTheme = true) {
        OnboardingContent(
            state = OnboardingState(error = ImportError.InvalidFormat),
            onEvent = {},
        )
    }
}
