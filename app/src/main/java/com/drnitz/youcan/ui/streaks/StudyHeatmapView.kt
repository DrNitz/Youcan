package com.drnitz.youcan.ui.streaks

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.drnitz.youcan.domain.model.DayStudySummary
import java.time.format.DateTimeFormatter

@Composable
fun StudyHeatmapView(
    past30Days: List<DayStudySummary>,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var selectedSummary by remember { mutableStateOf<DayStudySummary?>(null) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d") }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Consistency Map (Last 30 Days)",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Tap cell to inspect",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            val sagePrimary = MaterialTheme.colorScheme.primary
            val outlineColor = MaterialTheme.colorScheme.onSurface
            val emptyColor = MaterialTheme.colorScheme.outlineVariant
            val frozenColor = Color(0xFF689FB8)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .pointerInput(past30Days) {
                        detectTapGestures { tapOffset ->
                            val totalDays = past30Days.size.coerceAtLeast(1)
                            val cols = 10
                            val rows = 3
                            val gap = 6.dp.toPx()
                            val cellSize = ((size.width - (cols - 1) * gap) / cols)
                                .coerceAtMost((size.height - (rows - 1) * gap) / rows)
                            val gridWidth = cols * cellSize + (cols - 1) * gap
                            val startX = (size.width - gridWidth) / 2f

                            val relativeX = tapOffset.x - startX
                            val relativeY = tapOffset.y

                            if (relativeX >= 0 && relativeY >= 0) {
                                val col = (relativeX / (cellSize + gap)).toInt()
                                val row = (relativeY / (cellSize + gap)).toInt()
                                if (col in 0 until cols && row in 0 until rows) {
                                    val cellLeft = col * (cellSize + gap)
                                    val cellTop = row * (cellSize + gap)
                                    if (relativeX - cellLeft <= cellSize && relativeY - cellTop <= cellSize) {
                                        val index = row * cols + col
                                        if (index in past30Days.indices) {
                                            selectedSummary = past30Days[index]
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    }
                                }
                            }
                        }
                    }
            ) {
                val totalDays = past30Days.size.coerceAtLeast(1)
                val cols = 10
                val rows = 3
                val gap = 6.dp.toPx()
                val cellSize = ((size.width - (cols - 1) * gap) / cols).coerceAtMost((size.height - (rows - 1) * gap) / rows)
                val gridWidth = cols * cellSize + (cols - 1) * gap
                val startX = (size.width - gridWidth) / 2f

                for (index in 0 until totalDays) {
                    val col = index % cols
                    val row = index / cols
                    if (row >= rows) break

                    val daySummary = past30Days[index]
                    val cellColor = when {
                        daySummary.isFrozen -> frozenColor
                        daySummary.totalMinutes == 0 -> emptyColor
                        daySummary.totalMinutes < 30 -> sagePrimary.copy(alpha = 0.35f)
                        daySummary.totalMinutes < 60 -> sagePrimary.copy(alpha = 0.6f)
                        daySummary.totalMinutes < 120 -> sagePrimary.copy(alpha = 0.85f)
                        else -> sagePrimary
                    }

                    val x = startX + col * (cellSize + gap)
                    val y = row * (cellSize + gap)

                    drawRoundRect(
                        color = cellColor,
                        topLeft = Offset(x, y),
                        size = Size(cellSize, cellSize),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    if (selectedSummary == daySummary) {
                        drawRoundRect(
                            color = outlineColor,
                            topLeft = Offset(x - 1.5f, y - 1.5f),
                            size = Size(cellSize + 3f, cellSize + 3f),
                            cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx()),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }

            selectedSummary?.let { summary ->
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = summary.date.format(dateFormatter),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when {
                                summary.isFrozen -> "Streak Frozen"
                                summary.totalMinutes == 0 -> "0 min (Rest day)"
                                summary.isGoalMet -> "${summary.totalMinutes} min (Goal met)"
                                else -> "${summary.totalMinutes} min focused"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (summary.isGoalMet) sagePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Less active", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Goal achieved", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
