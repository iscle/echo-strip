package com.github.iscle.echostrip.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.github.iscle.echostrip.R

val RajdhaniFontFamily = FontFamily(
    Font(R.font.rajdhani_light, FontWeight.Light),
    Font(R.font.rajdhani_regular, FontWeight.Normal),
    Font(R.font.rajdhani_medium, FontWeight.Medium),
    Font(R.font.rajdhani_semibold, FontWeight.SemiBold),
    Font(R.font.rajdhani_bold, FontWeight.Bold),
)

private val defaults = Typography()

/** Rajdhani's generous ascent and descent push text low in its box; trim the line box. */
private fun TextStyle.rajdhani(): TextStyle = copy(
    fontFamily = RajdhaniFontFamily,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both,
    ),
)

val Typography = Typography(
    displayLarge = defaults.displayLarge.rajdhani(),
    displayMedium = defaults.displayMedium.rajdhani(),
    displaySmall = defaults.displaySmall.rajdhani(),
    headlineLarge = defaults.headlineLarge.rajdhani(),
    headlineMedium = defaults.headlineMedium.rajdhani(),
    headlineSmall = defaults.headlineSmall.rajdhani(),
    titleLarge = defaults.titleLarge.rajdhani(),
    titleMedium = defaults.titleMedium.rajdhani(),
    titleSmall = defaults.titleSmall.rajdhani(),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp).rajdhani(),
    bodyMedium = defaults.bodyMedium.rajdhani(),
    bodySmall = defaults.bodySmall.rajdhani(),
    labelLarge = defaults.labelLarge.rajdhani(),
    labelMedium = defaults.labelMedium.rajdhani(),
    labelSmall = defaults.labelSmall.rajdhani(),
)
