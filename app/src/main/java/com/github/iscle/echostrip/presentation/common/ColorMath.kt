package com.github.iscle.echostrip.presentation.common

import androidx.compose.ui.graphics.Color
import com.github.iscle.echostrip.domain.model.Rgb
import android.graphics.Color as AndroidColor

fun Rgb.toColor(): Color = Color(red, green, blue)

/** Fully saturated colour at [hue] degrees. */
fun rgbFromHue(hue: Float): Rgb = Rgb.fromPacked(AndroidColor.HSVToColor(floatArrayOf(hue.coerceIn(0f, 359.9f), 1f, 1f)))

/** Hue in degrees, or null for greys and white, which have none. */
fun Rgb.hueOrNull(): Float? {
    val hsv = FloatArray(3)
    AndroidColor.RGBToHSV(red, green, blue, hsv)
    return if (hsv[1] < 0.2f) null else hsv[0]
}
