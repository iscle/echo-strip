package com.github.iscle.echostrip.presentation.shell

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.github.iscle.echostrip.di.metroViewModel
import com.github.iscle.echostrip.domain.model.ConnectionState
import com.github.iscle.echostrip.domain.model.TimerAction
import com.github.iscle.echostrip.platform.BluetoothPermissions
import com.github.iscle.echostrip.presentation.common.SectionLabel
import com.github.iscle.echostrip.presentation.common.StripMotion
import com.github.iscle.echostrip.presentation.common.formatTime
import com.github.iscle.echostrip.presentation.common.toColor
import com.github.iscle.echostrip.presentation.light.LightScreen
import com.github.iscle.echostrip.presentation.schedule.ScheduleScreen
import com.github.iscle.echostrip.ui.theme.StripAccent
import com.github.iscle.echostrip.ui.theme.StripBorder
import com.github.iscle.echostrip.ui.theme.StripBorderFaint
import com.github.iscle.echostrip.ui.theme.StripOnSurface
import com.github.iscle.echostrip.ui.theme.StripOnSurfaceFaint
import com.github.iscle.echostrip.ui.theme.StripOnSurfaceMuted
import com.github.iscle.echostrip.ui.theme.StripSurface
import com.github.iscle.echostrip.ui.theme.StripSurfaceRaised

@Composable
fun StripApp(viewModel: ShellViewModel = metroViewModel()) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var permitted by remember { mutableStateOf(BluetoothPermissions.granted(context)) }
    val requestPermissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        permitted = BluetoothPermissions.granted(context)
    }
    LaunchedEffect(Unit) { if (!permitted) requestPermissions.launch(BluetoothPermissions.required) }

    // Hold the strip only while visible, so a phone can still reach it when the Echo is idle.
    LifecycleStartEffect(permitted) {
        viewModel.onVisibleChange(true)
        onStopOrDispose { viewModel.onVisibleChange(false) }
    }

    val backStack = rememberNavBackStack(Destination.Light)
    val current = backStack.lastOrNull() as? Destination ?: Destination.Light

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(StripSurface)
            .safeDrawingPadding(),
    ) {
        Rail(
            state = state,
            permitted = permitted,
            current = current,
            onSelect = { destination ->
                if (destination == current) return@Rail
                // The rail is flat: Light is home, Schedule sits on top of it.
                while (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                if (destination != Destination.Light) backStack.add(destination)
            },
            onFixPermission = {
                if (BluetoothPermissions.granted(context)) {
                    context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                } else {
                    requestPermissions.launch(BluetoothPermissions.required)
                }
            },
        )
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.weight(1f).fillMaxHeight(),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            transitionSpec = {
                (fadeIn(tween(StripMotion.SCREEN_MILLIS)) +
                    slideInVertically(tween(StripMotion.SCREEN_MILLIS)) { it / 12 }) togetherWith
                    fadeOut(tween(StripMotion.CONTROL_MILLIS))
            },
            popTransitionSpec = {
                fadeIn(tween(StripMotion.SCREEN_MILLIS)) togetherWith fadeOut(tween(StripMotion.CONTROL_MILLIS))
            },
            entryProvider = entryProvider {
                entry<Destination.Light> { LightScreen() }
                entry<Destination.Schedule> { ScheduleScreen() }
            },
        )
    }
}

@Composable
private fun Rail(
    state: ShellUiState,
    permitted: Boolean,
    current: Destination,
    onSelect: (Destination) -> Unit,
    onFixPermission: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight()
            .background(StripSurfaceRaised)
            .padding(horizontal = 20.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        RailHeader(state, permitted, onFixPermission)
        Destination.entries.forEach { destination ->
            RailItem(destination, selected = destination == current, onClick = { onSelect(destination) })
        }
        Spacer(Modifier.weight(1f))
        NextCard(state)
    }
}

@Composable
private fun RailHeader(state: ShellUiState, permitted: Boolean, onFixPermission: () -> Unit) {
    val light = state.light
    val glow by animateColorAsState(
        if (light.isOn) light.color.toColor() else Color.White.copy(alpha = 0.16f),
        tween(StripMotion.SCREEN_MILLIS), label = "rail_glow",
    )
    val (status, problem) = when (val c = state.connection) {
        is ConnectionState.Connected -> "Connected · ${c.name}" to false
        is ConnectionState.Connecting -> "Connecting to ${c.name}…" to false
        ConnectionState.Searching -> "Looking for the strip…" to false
        ConnectionState.Disconnected -> "Not connected" to false
        ConnectionState.Unavailable ->
            (if (permitted) "Bluetooth is off" else "Allow Bluetooth to connect") to true
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .then(if (problem) Modifier.clickable(onClick = onFixPermission) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(glow.copy(alpha = if (light.isOn) 0.14f else 0f))
                .border(1.dp, StripBorder, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(18.dp)
                    .shadow(if (light.isOn) 12.dp else 0.dp, CircleShape, ambientColor = glow, spotColor = glow)
                    .clip(CircleShape)
                    .background(glow),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Strip", fontSize = 32.sp, fontWeight = FontWeight.SemiBold, color = StripOnSurface)
            Text(status, fontSize = 17.sp, color = if (problem) StripAccent else StripOnSurfaceMuted, maxLines = 1)
        }
    }
}

@Composable
private fun RailItem(destination: Destination, selected: Boolean, onClick: () -> Unit) {
    val background by animateColorAsState(
        if (selected) Color.White.copy(alpha = 0.1f) else Color.Transparent,
        tween(StripMotion.CONTROL_MILLIS), label = "rail_selection",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(background, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            destination.icon,
            contentDescription = null,
            tint = if (selected) StripOnSurface else StripOnSurfaceMuted,
            modifier = Modifier.size(26.dp),
        )
        Text(
            destination.title,
            fontSize = 22.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) StripOnSurface else StripOnSurfaceMuted,
        )
    }
}

@Composable
private fun NextCard(state: ShellUiState) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StripBorderFaint, shape)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SectionLabel("Next")
        val next = state.next
        if (next == null) {
            Text("No timers on", fontSize = 19.sp, color = StripOnSurfaceFaint)
        } else {
            val verb = if (next.timer.action == TimerAction.TurnOn) "On" else "Off"
            Text(
                buildAnnotatedString {
                    append("$verb at ")
                    withStyle(SpanStyle(color = StripAccent, fontWeight = FontWeight.SemiBold)) {
                        append(formatTime(next.at.hour, next.at.minute))
                    }
                    append(" · ${relativeDay(next.at.toLocalDate(), state.now.toLocalDate())}")
                },
                fontSize = 19.sp,
                fontWeight = FontWeight.Medium,
                color = StripOnSurface,
            )
        }
    }
}
