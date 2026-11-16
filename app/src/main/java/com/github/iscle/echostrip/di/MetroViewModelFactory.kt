package com.github.iscle.echostrip.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.SingleIn
import kotlin.reflect.KClass

/** Builds every ViewModel the graph contributes, so screens never construct their own. */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class MetroViewModelFactory(
    private val providers: Map<KClass<out ViewModel>, Provider<ViewModel>>,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val provider = providers[modelClass.kotlin]
            ?: error("No ViewModel bound for ${modelClass.name}; is it @ContributesIntoMap?")
        @Suppress("UNCHECKED_CAST")
        return provider() as T
    }
}

val LocalViewModelFactory = staticCompositionLocalOf<ViewModelProvider.Factory> {
    error("LocalViewModelFactory is not provided")
}

/** A ViewModel from the Metro graph, scoped to the nearest ViewModelStoreOwner. */
@Composable
inline fun <reified T : ViewModel> metroViewModel(): T = viewModel(factory = LocalViewModelFactory.current)
