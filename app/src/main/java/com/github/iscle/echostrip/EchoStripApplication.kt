package com.github.iscle.echostrip

import android.app.Application
import com.github.iscle.echostrip.di.AppGraph
import dev.zacsweers.metro.createGraphFactory
import timber.log.Timber

class EchoStripApplication : Application() {
    val graph: AppGraph by lazy { createGraphFactory<AppGraph.Factory>().create(this) }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}
