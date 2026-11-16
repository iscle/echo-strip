package com.github.iscle.echostrip.presentation.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.iscle.echostrip.ui.theme.StripAccent
import com.github.iscle.echostrip.ui.theme.StripBorder
import com.github.iscle.echostrip.ui.theme.StripDivider
import com.github.iscle.echostrip.ui.theme.StripInk
import com.github.iscle.echostrip.ui.theme.StripOnSurface
import com.github.iscle.echostrip.ui.theme.StripOnSurfaceFaint
import com.github.iscle.echostrip.ui.theme.StripSurfacePanel

/** Shared motion timings, as in Haven. */
object StripMotion {
    const val CONTROL_MILLIS = 180
    const val SCREEN_MILLIS = 260
}

/** The outlined panel every screen's controls sit in. */
fun Modifier.stripCard(cornerRadius: Dp = 22.dp): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return clip(shape).background(StripSurfacePanel).border(1.dp, StripBorder, shape)
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, color: Color = StripOnSurfaceFaint) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 2.4.sp,
        color = color,
    )
}

/** 68x38 pill switch with a 28dp thumb, as in Haven. */
@Composable
fun StripSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val track by animateColorAsState(
        if (checked) StripAccent else Color.White.copy(alpha = 0.16f),
        tween(StripMotion.CONTROL_MILLIS), label = "switch_track",
    )
    val thumb by animateColorAsState(
        if (checked) StripInk else Color.White.copy(alpha = 0.7f),
        tween(StripMotion.CONTROL_MILLIS), label = "switch_thumb",
    )
    val offset by animateDpAsState(
        if (checked) 30.dp else 0.dp,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow), label = "switch_offset",
    )
    Box(
        modifier = modifier
            .size(width = 68.dp, height = 38.dp)
            .clip(CircleShape)
            .background(track)
            .clickable(role = Role.Switch, onClickLabel = contentDescription) { onCheckedChange(!checked) }
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset { IntOffset(offset.roundToPx(), 0) }
                .size(28.dp)
                .clip(CircleShape)
                .background(thumb),
        )
    }
}

/** Light-on-dark primary action, e.g. "Set 23:00". */
@Composable
fun StripFilledButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, height: Dp = 56.dp) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 34.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = StripInk)
    }
}

@Composable
fun StripOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    contentColor: Color = StripOnSurface,
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .border(1.dp, Color.White.copy(alpha = 0.22f), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
    }
}

/** Title, supporting line and a trailing control, as in Haven's settings rows. */
@Composable
fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = StripOnSurface)
            if (subtitle != null) Text(subtitle, fontSize = 18.sp, color = StripOnSurfaceFaint)
        }
        trailing()
    }
}

@Composable
fun Divider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(StripDivider))
}
