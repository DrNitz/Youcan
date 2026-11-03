package com.drnitz.youcan.ui.timer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drnitz.youcan.domain.model.TimerPhase
import com.drnitz.youcan.domain.model.TimerSnapshot

@Composable
fun TimerRing(
    snapshot: TimerSnapshot,
    size: Dp = 260.dp,
    strokeWidth: Dp = 10.dp,
    modifier: Modifier = Modifier
) {
    val progressRatio = if (snapshot.totalSeconds > 0) {
        snapshot.remainingSeconds.toFloat() / snapshot.totalSeconds.toFloat()
    } else 1f

    val animatedSweep by animateFloatAsState(
        targetValue = progressRatio * 360f,
        label = "timer_sweep"
    )

    val ringColor = when (snapshot.phase) {
        TimerPhase.FOCUS -> MaterialTheme.colorScheme.primary
        TimerPhase.SHORT_BREAK -> Color(0xFF689FB8)
        TimerPhase.LONG_BREAK -> Color(0xFF9B87BA)
    }

    val minutes = snapshot.remainingSeconds / 60
    val seconds = snapshot.remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            // Track circle
            drawCircle(
                color = ringColor.copy(alpha = 0.15f),
                style = Stroke(width = strokePx)
            )

            // Progress arc
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = animatedSweep,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-1).sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${snapshot.phase.displayName} • Cycle ${snapshot.currentCycle}/${snapshot.totalCycles}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.75f)
            )
        }
    }
}
