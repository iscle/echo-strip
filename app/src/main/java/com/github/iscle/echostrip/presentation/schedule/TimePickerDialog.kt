package com.github.iscle.echostrip.presentation.schedule

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.iscle.echostrip.presentation.common.StripFilledButton
import com.github.iscle.echostrip.presentation.common.StripMotion
import com.github.iscle.echostrip.presentation.common.StripOutlinedButton
import com.github.iscle.echostrip.presentation.common.formatTime
import com.github.iscle.echostrip.ui.theme.StripAccent
import com.github.iscle.echostrip.ui.theme.StripDialog
import com.github.iscle.echostrip.ui.theme.StripOnSurface
import com.github.iscle.echostrip.ui.theme.StripOnSurfaceFaint
import com.github.iscle.echostrip.ui.theme.StripOnSurfaceMuted

/** Haven's hour/minute spinner dialog, minus the sunrise option. */
@Composable
fun TimePickerDialog(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
) {
    var hour by remember { mutableIntStateOf(initialHour) }
    var minute by remember { mutableIntStateOf(initialMinute) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier
                .width(820.dp)
                .background(StripDialog, RoundedCornerShape(28.dp))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 44.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 30.sp, fontWeight = FontWeight.SemiBold, color = StripOnSurface)
                Text(
                    "Stored on the strip, so it runs even when the Echo is asleep",
                    fontSize = 19.sp,
                    color = StripOnSurfaceFaint,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TimeSpinner(hour, highlighted = true, label = "hour") { hour = (it + 24) % 24 }
                Text(
                    ":",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Light,
                    color = StripOnSurface.copy(alpha = 0.35f),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                TimeSpinner(minute, highlighted = false, step = 5, label = "minute") { minute = (it + 60) % 60 }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.End),
            ) {
                StripOutlinedButton("Cancel", onDismiss, contentColor = StripOnSurfaceMuted)
                StripFilledButton("Set ${formatTime(hour, minute)}", { onConfirm(hour, minute) })
            }
        }
    }
}

@Composable
private fun TimeSpinner(
    value: Int,
    highlighted: Boolean,
    label: String,
    step: Int = 1,
    onChange: (Int) -> Unit,
) {
    var goingUp by remember { mutableStateOf(true) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            Icons.Default.KeyboardArrowUp,
            contentDescription = "Later $label",
            tint = StripOnSurface.copy(alpha = 0.4f),
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(role = Role.Button) { goingUp = true; onChange(value + step) }
                .padding(10.dp),
        )
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(104.dp)
                .background(
                    if (highlighted) StripAccent.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.06f),
                    RoundedCornerShape(20.dp),
                )
                .clip(RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    val direction = if (goingUp) -1 else 1
                    (slideInVertically(tween(StripMotion.CONTROL_MILLIS)) { it * direction } +
                        fadeIn(tween(StripMotion.CONTROL_MILLIS))) togetherWith
                        (slideOutVertically(tween(StripMotion.CONTROL_MILLIS)) { -it * direction } +
                            fadeOut(tween(StripMotion.CONTROL_MILLIS)))
                },
                label = "time_digit",
            ) { shown ->
                Text("%02d".format(shown), fontSize = 62.sp, color = if (highlighted) StripAccent else StripOnSurface)
            }
        }
        Icon(
            Icons.Default.KeyboardArrowDown,
            contentDescription = "Earlier $label",
            tint = StripOnSurface.copy(alpha = 0.4f),
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(role = Role.Button) { goingUp = false; onChange(value - step) }
                .padding(10.dp),
        )
    }
}
