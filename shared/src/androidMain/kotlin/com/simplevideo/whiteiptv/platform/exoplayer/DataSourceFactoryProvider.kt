package com.simplevideo.whiteiptv.platform.exoplayer

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cronet.CronetDataSource
import androidx.media3.datasource.cronet.CronetUtil
import co.touchlab.kermit.Logger
import com.simplevideo.whiteiptv.platform.PlayerConfig
import java.util.concurrent.Executors

/**
 * Provides DataSource.Factory with Cronet support and automatic fallback
 */
@OptIn(UnstableApi::class)
class DataSourceFactoryProvider(
    private val context: Context,
) {

    private val cronetEngine by lazy {
        CronetUtil.buildCronetEngine(context)
    }

    private val executor by lazy { Executors.newCachedThreadPool() }

    fun create(config: PlayerConfig): DataSource.Factory {
        if (config.useCronet) {
            val engine = cronetEngine
            if (engine != null) {
                Logger.d("Player") { "Using Cronet data source" }
                return CronetDataSource.Factory(engine, executor)
                    .setConnectionTimeoutMs(config.connectTimeoutMs)
                    .setReadTimeoutMs(config.readTimeoutMs)
                    .setResetTimeoutOnRedirects(true)
                    .setHandleSetCookieRequests(true)
                    .setKeepPostFor302Redirects(true)
            }
            Logger.w("Player") { "Cronet engine unavailable, falling back to DefaultHttpDataSource" }
        }
        return DefaultHttpDataSource.Factory()
            .setConnectTimeoutMs(config.connectTimeoutMs)
            .setReadTimeoutMs(config.readTimeoutMs)
            .setAllowCrossProtocolRedirects(true)
    }
}
