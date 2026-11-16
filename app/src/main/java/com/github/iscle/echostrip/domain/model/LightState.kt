package com.github.iscle.echostrip.domain.model

/**
 * What the strip is showing. The strip cannot report this back, so the app's copy is the source
 * of truth and is pushed to the strip whenever it changes.
 */
data class LightState(
    val isOn: Boolean,
    val color: Rgb,
    /** 1..100 percent. */
    val brightness: Int,
) {
    companion object {
        val Default = LightState(isOn = true, color = Rgb(255, 119, 0), brightness = 80)
    }
}
