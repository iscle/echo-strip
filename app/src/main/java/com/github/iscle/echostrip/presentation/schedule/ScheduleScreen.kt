package com.github.iscle.echostrip.presentation.schedule

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.iscle.echostrip.di.metroViewModel
import com.github.iscle.echostrip.domain.model.StripTimer
import com.github.iscle.echostrip.domain.model.TimerAction
import com.github.iscle.echostrip.presentation.common.Divider
import com.github.iscle.echostrip.presentation.common.SectionLabel
import com.github.iscle.echostrip.presentation.common.SettingsRow
import com.github.iscle.echostrip.presentation.common.StripMotion
import com.github.iscle.echostrip.presentation.common.StripSwitch
import com.github.iscle.echostrip.presentation.common.describeDays
import com.github.iscle.echostrip.presentation.common.formatTime
import com.github.iscle.echostrip.presentation.common.fullLabel
import com.github.iscle.echostrip.presentation.common.shortLabel
import com.github.iscle.echostrip.presentation.common.stripCard
import com.github.iscle.echostrip.ui.theme.StripAccent
import com.github.iscle.echostrip.ui.theme.StripBorder
import com.github.iscle.echostrip.ui.theme.StripOnSurface
import com.github.iscle.echostrip.ui.theme.StripOnSurfaceMuted
import java.time.DayOfWeek

val TimerAction.title get() = if (this == TimerAction.TurnOn) "Turn on" else "Turn off"

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = metroViewModel()) {
    val timers by viewModel.timers.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<TimerAction?>(null) }

    ScheduleContent(
        timers = timers,
        onEnabledChange = viewModel::onEnabledChange,
        onDayToggle = viewModel::onDayToggle,
        onEditTime = { editing = it.action },
    )

    val timer = timers.firstOrNull { it.action == editing }
    if (timer != null) {
        TimePickerDialog(
            title = "Strip ${timer.action.title.lowercase().replace("turn ", "turns ")}",
            initialHour = timer.hour,
            initialMinute = timer.minute,
            onDismiss = { editing = null },
            onConfirm = { hour, minute ->
                viewModel.onTimeChange(timer, hour, minute)
                editing = null
            },
        )
    }
}

@Composable
fun ScheduleContent(
    timers: List<StripTimer>,
    onEnabledChange: (StripTimer, Boolean) -> Unit,
    onDayToggle: (StripTimer, DayOfWeek) -> Unit,
    onEditTime: (StripTimer) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 44.dp, vertical = 34.dp),
    ) {
        Column(Modifier.fillMaxWidth().stripCard().padding(horizontal = 28.dp)) {
            timers.forEachIndexed { index, timer ->
                if (index > 0) Divider()
                TimerSection(
                    timer = timer,
                    onEnabledChange = { onEnabledChange(timer, it) },
                    onDayToggle = { onDayToggle(timer, it) },
                    onEditTime = { onEditTime(timer) },
                )
            }
        }
    }
}

@Composable
private fun TimerSection(
    timer: StripTimer,
    onEnabledChange: (Boolean) -> Unit,
    onDayToggle: (DayOfWeek) -> Unit,
    onEditTime: () -> Unit,
) {
    val alpha by animateFloatAsState(if (timer.isEnabled) 1f else 0.45f, label = "timer_alpha")
    Column(Modifier.padding(vertical = 22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SettingsRow(
            title = timer.action.title,
            subtitle = if (timer.isEnabled) describeDays(timer.days) else "Off",
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                TimeChip(
                    timer.hour,
                    timer.minute,
                    onEditTime,
                    "Change ${timer.action.title.lowercase()} time",
                    modifier = Modifier.alpha(alpha),
                )
                StripSwitch(timer.isEnabled, onEnabledChange, contentDescription = "${timer.action.title} timer")
            }
        }
        Row(
            modifier = Modifier.alpha(alpha),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DayOfWeek.entries.forEach { day ->
                DayPill(day, selected = day in timer.days, onClick = { onDayToggle(day) }, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TimeChip(hour: Int, minute: Int, onClick: () -> Unit, label: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .width(164.dp)
            .height(60.dp)
            .clip(shape)
            .border(1.dp, StripBorder, shape)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionLabel("At", Modifier.weight(1f), color = StripOnSurfaceMuted)
        Text(formatTime(hour, minute), fontSize = 30.sp, fontWeight = FontWeight.SemiBold, color = StripAccent)
    }
}

@Composable
private fun DayPill(day: DayOfWeek, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    val background by animateColorAsState(
        if (selected) StripAccent.copy(alpha = 0.14f) else Color.Transparent,
        tween(StripMotion.CONTROL_MILLIS), label = "day_bg",
    )
    val border by animateColorAsState(
        if (selected) StripAccent.copy(alpha = 0.45f) else StripBorder,
        tween(StripMotion.CONTROL_MILLIS), label = "day_border",
    )
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(shape)
            .background(background)
            .border(1.dp, border, shape)
            .clickable(role = Role.Checkbox, onClickLabel = day.fullLabel(), onClick = onClick)
            .semantics { this.selected = selected },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            day.shortLabel(),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) StripAccent else StripOnSurfaceMuted,
        )
    }
}
