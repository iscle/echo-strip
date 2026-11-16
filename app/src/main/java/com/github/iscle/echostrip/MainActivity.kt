package com.github.iscle.echostrip

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.github.iscle.echostrip.di.LocalViewModelFactory
import com.github.iscle.echostrip.presentation.shell.StripApp
import com.github.iscle.echostrip.ui.theme.EchoStripTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app is always dark, so the bars must be too, whatever the system theme says.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        val graph = (application as EchoStripApplication).graph
        setContent {
            CompositionLocalProvider(LocalViewModelFactory provides graph.viewModelFactory) {
                EchoStripTheme { StripApp() }
            }
        }
    }
}
