package com.github.iscle.echostrip

import android.app.Application
import timber.log.Timber

class EchoStripApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}
