package com.github.iscle.echostrip.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.github.iscle.echostrip.data.local.DeviceDao
import com.github.iscle.echostrip.data.local.LightStateDao
import com.github.iscle.echostrip.data.local.StripDatabase
import com.github.iscle.echostrip.data.local.TimerDao
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Process-wide objects. Repositories, the strip link and ViewModels contribute themselves. */
@DependencyGraph(AppScope::class)
interface AppGraph {
    val viewModelFactory: ViewModelProvider.Factory

    @Provides @SingleIn(AppScope::class)
    fun provideAppScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides @SingleIn(AppScope::class)
    fun provideDatabase(context: Context): StripDatabase = StripDatabase.open(context)

    @Provides
    fun provideLightStateDao(database: StripDatabase): LightStateDao = database.lightStateDao()

    @Provides
    fun provideTimerDao(database: StripDatabase): TimerDao = database.timerDao()

    @Provides
    fun provideDeviceDao(database: StripDatabase): DeviceDao = database.deviceDao()

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides context: Context): AppGraph
    }
}
