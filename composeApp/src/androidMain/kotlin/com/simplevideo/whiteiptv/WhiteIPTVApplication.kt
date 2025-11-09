package com.simplevideo.whiteiptv

import android.app.Application
import com.simplevideo.whiteiptv.di.initializeKoin
import org.koin.android.ext.koin.androidContext

class WhiteIPTVApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeKoin {
            androidContext(this@WhiteIPTVApplication)
        }
    }
}
