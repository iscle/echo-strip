package com.github.iscle.echostrip.presentation.light

import com.github.iscle.echostrip.domain.model.Rgb
import com.github.iscle.echostrip.presentation.common.rgbFromHue

data class ColorPreset(val name: String, val color: Rgb)

val ColorPresets = listOf(
    ColorPreset("Red", rgbFromHue(0f)),
    ColorPreset("Amber", rgbFromHue(28f)),
    ColorPreset("Yellow", rgbFromHue(55f)),
    ColorPreset("Green", rgbFromHue(120f)),
    ColorPreset("Cyan", rgbFromHue(185f)),
    ColorPreset("Blue", rgbFromHue(225f)),
    ColorPreset("Violet", rgbFromHue(275f)),
    ColorPreset("White", Rgb.White),
)
