package com.drnitz.youcan.ui.analytics

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeeklyTrendBarChart(
    past7DaysMinutes: Map<LocalDate, Int>,
    dailyGoalMinutes: Int,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val days = (0..6).map { today.minusDays(it.toLong()) }.reversed()
    val maxMinutes = (past7DaysMinutes.values.maxOrNull() ?: dailyGoalMinutes).coerceAtLeast(dailyGoalMinutes).coerceAtLeast(60)

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "7-Day Study Trend",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
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
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (date in days) {
                    val label = if (date == today) "Today" else date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (date == today) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
