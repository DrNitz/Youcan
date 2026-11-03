package com.drnitz.youcan.ui.analytics

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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeeklyTrendBarChart(
    past7DaysMinutes: Map<LocalDate, Int>,
    dailyGoalMinutes: Int,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val today = LocalDate.now()
    val days = (0..6).map { today.minusDays(it.toLong()) }.reversed()
    val maxMinutes = (past7DaysMinutes.values.maxOrNull() ?: dailyGoalMinutes).coerceAtLeast(dailyGoalMinutes).coerceAtLeast(60)

    var selectedDate by remember { mutableStateOf<LocalDate?>(today) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d") }

    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.onSurface
    val trackColor = MaterialTheme.colorScheme.outlineVariant

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
                    text = "7-Day Study Trend",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Goal: ${dailyGoalMinutes}m/day",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .pointerInput(days) {
                        detectTapGestures { tapOffset ->
                            val barWidth = 24.dp.toPx()
                            val totalWidth = size.width
                            val spacing = (totalWidth - (days.size * barWidth)) / (days.size + 1)
                            for (index in days.indices) {
                                val barLeft = spacing + index * (barWidth + spacing)
                                val touchTolerance = spacing / 2f
                                if (tapOffset.x >= barLeft - touchTolerance && tapOffset.x <= barLeft + barWidth + touchTolerance) {
                                    selectedDate = days[index]
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    break
                                }
                            }
                        }
                    }
            ) {
                val barWidth = 24.dp.toPx()
                val totalWidth = size.width
                val availableHeight = size.height - 10.dp.toPx()
                val spacing = (totalWidth - (days.size * barWidth)) / (days.size + 1)

                days.forEachIndexed { index, date ->
                    val minutes = past7DaysMinutes[date] ?: 0
                    val ratio = (minutes.toFloat() / maxMinutes.toFloat()).coerceIn(0f, 1f)
                    val barHeight = (availableHeight * ratio).coerceAtLeast(6f)

                    val x = spacing + index * (barWidth + spacing)
                    val y = availableHeight - barHeight
                    val isSelected = (selectedDate == date)

                    // Background track bar
                    drawRoundRect(
                        color = trackColor.copy(alpha = 0.5f),
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, availableHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Active progress bar
                    drawRoundRect(
                        color = if (minutes >= dailyGoalMinutes) primaryColor else primaryColor.copy(alpha = 0.6f),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    if (isSelected) {
                        drawRoundRect(
                            color = outlineColor,
                            topLeft = Offset(x - 1.5f, -1.5f),
                            size = Size(barWidth + 3f, availableHeight + 3f),
                            cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx()),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (date in days) {
                    val label = if (date == today) "Today" else date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                    val isSelected = (selectedDate == date)
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            selectedDate?.let { date ->
                val minutes = past7DaysMinutes[date] ?: 0
                val goalDiff = minutes - dailyGoalMinutes
                Spacer(modifier = Modifier.height(12.dp))
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
                            text = date.format(dateFormatter),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when {
                                minutes == 0 -> "0 min studied"
                                goalDiff >= 0 -> "$minutes min (Goal Met)"
                                else -> "$minutes min (${-goalDiff}m to goal)"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (minutes >= dailyGoalMinutes) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
