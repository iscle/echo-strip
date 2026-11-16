package com.github.iscle.echostrip.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// A fixed dark system: the strip's own colour is the only thing that should tint the screen.
private val StripColorScheme = darkColorScheme(
    primary = StripAccent,
    onPrimary = StripInk,
    secondary = StripMint,
    onSecondary = StripInk,
    background = StripSurface,
    onBackground = StripOnSurface,
    surface = StripSurface,
    onSurface = StripOnSurface,
    surfaceVariant = StripSurfaceRaised,
    onSurfaceVariant = StripOnSurfaceMuted,
    surfaceContainer = StripSurfacePanel,
    outline = StripBorder,
    outlineVariant = StripBorderFaint,
    error = StripCoral,
    onError = StripInk,
)

@Composable
fun EchoStripTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = StripColorScheme, typography = Typography, content = content)
}
