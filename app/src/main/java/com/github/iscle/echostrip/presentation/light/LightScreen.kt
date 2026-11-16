package com.github.iscle.echostrip.presentation.light

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.iscle.echostrip.di.metroViewModel
import com.github.iscle.echostrip.domain.model.LightState
import com.github.iscle.echostrip.domain.model.Rgb
import com.github.iscle.echostrip.presentation.common.Divider
import com.github.iscle.echostrip.presentation.common.GradientSlider
import com.github.iscle.echostrip.presentation.common.SettingsRow
import com.github.iscle.echostrip.presentation.common.StripMotion
import com.github.iscle.echostrip.presentation.common.StripSwitch
import com.github.iscle.echostrip.presentation.common.hueOrNull
import com.github.iscle.echostrip.presentation.common.rgbFromHue
import com.github.iscle.echostrip.presentation.common.stripCard
import com.github.iscle.echostrip.presentation.common.toColor
import com.github.iscle.echostrip.ui.theme.StripAccent
import com.github.iscle.echostrip.ui.theme.StripInk
import com.github.iscle.echostrip.ui.theme.StripOnSurface
import com.github.iscle.echostrip.ui.theme.StripOnSurfaceMuted
import kotlin.math.roundToInt

private val Rainbow = Brush.horizontalGradient(
    listOf(0f, 60f, 120f, 180f, 240f, 300f, 359.9f).map { rgbFromHue(it).toColor() },
)

@Composable
fun LightScreen(viewModel: LightViewModel = metroViewModel()) {
    val state by viewModel.lightState.collectAsStateWithLifecycle()
    LightContent(
        state = state,
        onPowerChange = viewModel::onPowerChange,
        onColorPick = viewModel::onColorPick,
        onHueChange = viewModel::onHueChange,
        onBrightnessChange = viewModel::onBrightnessChange,
    )
}

@Composable
fun LightContent(
    state: LightState,
    onPowerChange: (Boolean) -> Unit,
    onColorPick: (Rgb) -> Unit,
    onHueChange: (Float) -> Unit,
    onBrightnessChange: (Int) -> Unit,
) {
    // While a finger is on a slider, show its position rather than waiting on the round trip.
    var dragHue by remember { mutableStateOf<Float?>(null) }
    var dragBrightness by remember { mutableStateOf<Float?>(null) }

    val stripColor by animateColorAsState(
        if (state.isOn) state.color.toColor() else Color(0xFF8A8E98),
        tween(StripMotion.SCREEN_MILLIS), label = "strip_color",
    )
    val controlsAlpha by animateFloatAsState(if (state.isOn) 1f else 0.45f, label = "controls_alpha")
    val hue = dragHue ?: state.color.hueOrNull() ?: 0f
    val brightness = dragBrightness ?: (state.brightness / 100f)
    val preset = ColorPresets.firstOrNull { it.color == state.color }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 44.dp, vertical = 34.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .stripCard()
                .padding(horizontal = 28.dp),
        ) {
            SettingsRow(
                title = "Power",
                subtitle = if (state.isOn) "The strip is on" else "The strip is off",
                modifier = Modifier.padding(vertical = 20.dp),
            ) {
                StripSwitch(checked = state.isOn, onCheckedChange = onPowerChange, contentDescription = "Power")
            }
            Divider()
            Column(
                modifier = Modifier.padding(vertical = 20.dp).alpha(controlsAlpha),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Color", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = StripOnSurface, modifier = Modifier.weight(1f))
                    Text(preset?.name ?: "Custom", fontSize = 18.sp, color = StripOnSurfaceMuted)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ColorPresets.forEach { p ->
                        Swatch(p, selected = p == preset, onClick = { onColorPick(p.color) })
                    }
                }
                GradientSlider(
                    value = hue / 360f,
                    onValueChange = { dragHue = it * 360f; onHueChange(it * 360f) },
                    onValueChangeFinished = { dragHue = null },
                    track = Rainbow,
                    thumbFill = Color.Transparent,
                    thumbRing = StripOnSurface,
                    contentDescription = "Hue",
                )
            }
            Divider()
            Column(
                modifier = Modifier.padding(vertical = 20.dp).alpha(controlsAlpha),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Brightness", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = StripOnSurface, modifier = Modifier.weight(1f))
                    Text(
                        "${(brightness * 100).roundToInt().coerceAtLeast(1)}%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = stripColor,
                    )
                }
                GradientSlider(
                    value = brightness,
                    onValueChange = {
                        dragBrightness = it
                        onBrightnessChange((it * 100).roundToInt().coerceAtLeast(1))
                    },
                    onValueChangeFinished = { dragBrightness = null },
                    track = Brush.horizontalGradient(listOf(Color(0xFF1A1C21), stripColor)),
                    thumbFill = StripInk,
                    thumbRing = stripColor,
                    contentDescription = "Brightness",
                )
            }
        }
    }
}

@Composable
private fun Swatch(preset: ColorPreset, selected: Boolean, onClick: () -> Unit) {
    val ring by animateColorAsState(
        if (selected) StripAccent else Color.Transparent,
        tween(StripMotion.CONTROL_MILLIS), label = "swatch_ring",
    )
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .border(3.dp, ring, CircleShape)
            .clickable(role = Role.RadioButton, onClickLabel = preset.name, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(5.dp)
            .clip(CircleShape)
            .background(preset.color.toColor()),
    )
}
