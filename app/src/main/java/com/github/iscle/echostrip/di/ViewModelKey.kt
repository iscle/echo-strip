package com.github.iscle.echostrip.di

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.MapKey
import kotlin.reflect.KClass

/** Keys a ViewModel contributed with `@ContributesIntoMap` by its class. */
@MapKey
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ViewModelKey(val value: KClass<out ViewModel>)
