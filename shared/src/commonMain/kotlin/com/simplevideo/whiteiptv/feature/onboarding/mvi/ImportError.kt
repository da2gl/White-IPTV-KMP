package com.simplevideo.whiteiptv.feature.onboarding.mvi

/**
 * Represents user-facing import error types.
 * Resolved to localized strings in the UI layer.
 */
sealed interface ImportError {
    data object NoConnection : ImportError
    data object ServerNotFound : ImportError
    data object Timeout : ImportError
    data class HttpError(val statusCode: Int) : ImportError
    data object InvalidUrl : ImportError
    data object InvalidFormat : ImportError
    data object EmptyPlaylist : ImportError
    data object StorageError : ImportError
    data class Unknown(val detail: String? = null) : ImportError
}
