package com.simplevideo.whiteiptv

import android.app.Application
import com.simplevideo.whiteiptv.di.initializeKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class WhiteIPTVApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeKoin {
            androidLogger()
            androidContext(this@WhiteIPTVApplication)
        }
    }
}
