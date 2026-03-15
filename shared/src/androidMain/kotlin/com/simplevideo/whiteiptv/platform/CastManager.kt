package com.simplevideo.whiteiptv.platform

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManager
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.common.images.WebImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android Chromecast session manager using the Cast SDK.
 * Observes session lifecycle and exposes connection state as a [StateFlow].
 */
actual class CastManager(context: Context) {

    private val _castState = MutableStateFlow(CastConnectionState.NOT_CONNECTED)
    actual val castState: StateFlow<CastConnectionState> = _castState.asStateFlow()

    private val castContext: CastContext? = try {
        CastContext.getSharedInstance(context)
    } catch (@Suppress("TooGenericExceptionCaught") e: RuntimeException) {
        Log.e(TAG, "Cast SDK not available", e)
        null
    }

    private val sessionManager: SessionManager? = castContext?.sessionManager

    private val sessionListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarting(session: CastSession) {
            _castState.value = CastConnectionState.CONNECTING
        }

        override fun onSessionStarted(session: CastSession, sessionId: String) {
            _castState.value = CastConnectionState.CONNECTED
        }

        override fun onSessionStartFailed(session: CastSession, error: Int) {
            _castState.value = CastConnectionState.NOT_CONNECTED
        }

        override fun onSessionEnding(session: CastSession) {
            // Keep CONNECTED until fully ended
        }

        override fun onSessionEnded(session: CastSession, error: Int) {
            _castState.value = CastConnectionState.NOT_CONNECTED
        }

        override fun onSessionResuming(session: CastSession, sessionId: String) {
            _castState.value = CastConnectionState.CONNECTING
        }

        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            _castState.value = CastConnectionState.CONNECTED
        }

        override fun onSessionResumeFailed(session: CastSession, error: Int) {
            _castState.value = CastConnectionState.NOT_CONNECTED
        }

        override fun onSessionSuspended(session: CastSession, reason: Int) {
            _castState.value = CastConnectionState.NOT_CONNECTED
        }
    }

    init {
        sessionManager?.addSessionManagerListener(sessionListener, CastSession::class.java)
        // Sync initial state
        if (sessionManager?.currentCastSession?.isConnected == true) {
            _castState.value = CastConnectionState.CONNECTED
        }
    }

    actual fun startCasting(url: String, title: String?, logoUrl: String?) {
        val session = sessionManager?.currentCastSession ?: return
        val remoteMediaClient = session.remoteMediaClient ?: return

        val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
            if (title != null) putString(MediaMetadata.KEY_TITLE, title)
            if (logoUrl != null) addImage(WebImage(Uri.parse(logoUrl)))
        }

        val mediaInfo = MediaInfo.Builder(url)
            .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
            .setMetadata(metadata)
            .build()

        val loadRequest = MediaLoadRequestData.Builder()
            .setMediaInfo(mediaInfo)
            .setAutoplay(true)
            .build()

        remoteMediaClient.load(loadRequest)
    }

    actual fun stopCasting() {
        sessionManager?.currentCastSession?.remoteMediaClient?.stop()
    }

    actual fun isAvailable(): Boolean {
        return castContext != null
    }

    companion object {
        private const val TAG = "WhiteIPTV:CastManager"
    }
}
