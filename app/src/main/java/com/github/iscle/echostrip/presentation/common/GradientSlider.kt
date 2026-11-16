package com.github.iscle.echostrip.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/**
 * A fat, touch-friendly slider: the track is the gradient itself, so the hue slider shows the
 * rainbow and the brightness slider fades into the strip's colour. [value] is 0..1.
 */
@Composable
fun GradientSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    track: Brush,
    thumbFill: Color,
    thumbRing: Color,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onValueChangeFinished: () -> Unit = {},
    height: Dp = 44.dp,
) {
    val change by rememberUpdatedState(onValueChange)
    val finished by rememberUpdatedState(onValueChangeFinished)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .semantics {
                this.contentDescription = contentDescription
                progressBarRangeInfo = ProgressBarRangeInfo(value, 0f..1f)
                setProgress { change(it.coerceIn(0f, 1f)); finished(); true }
            },
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val thumbPx = constraints.maxHeight.toFloat()
        val travel = (widthPx - thumbPx).coerceAtLeast(1f)
        fun at(x: Float) = ((x - thumbPx / 2) / travel).coerceIn(0f, 1f)

        Box(
            Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(height / 2))
                .background(track)
                .pointerInput(travel) {
                    detectTapGestures { change(at(it.x)); finished() }
                }
                .pointerInput(travel) {
                    detectHorizontalDragGestures(
                        onDragStart = { change(at(it.x)) },
                        onDragEnd = { finished() },
                        onDragCancel = { finished() },
                    ) { input, _ -> change(at(input.position.x)) }
                },
        )
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset { IntOffset((value.coerceIn(0f, 1f) * travel).toInt(), 0) }
                .size(height)
                .clip(CircleShape)
                .background(thumbFill)
                .border(4.dp, thumbRing, CircleShape),
        )
    }
}
