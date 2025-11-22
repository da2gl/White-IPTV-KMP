package com.simplevideo.whiteiptv.platform.exoplayer

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cronet.CronetDataSource
import androidx.media3.datasource.cronet.CronetUtil
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

    fun create(config: PlayerConfig): DataSource.Factory {
        if (config.useCronet) {
            val engine = cronetEngine
            if (engine != null) {
                return CronetDataSource.Factory(engine, Executors.newCachedThreadPool())
                    .setConnectionTimeoutMs(config.connectTimeoutMs)
                    .setReadTimeoutMs(config.readTimeoutMs)
                    .setResetTimeoutOnRedirects(true)
                    .setHandleSetCookieRequests(true)
                    .setKeepPostFor302Redirects(true)
            }
        }
        return DefaultHttpDataSource.Factory()
            .setConnectTimeoutMs(config.connectTimeoutMs)
            .setReadTimeoutMs(config.readTimeoutMs)
            .setAllowCrossProtocolRedirects(true)
    }
}
