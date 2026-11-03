package com.drnitz.youcan.ui.streaks

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
import com.drnitz.youcan.domain.model.DayStudySummary

@Composable
fun StudyHeatmapView(
    past30Days: List<DayStudySummary>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Consistency Map (Last 30 Days)",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(14.dp))

            val sagePrimary = MaterialTheme.colorScheme.primary
            val emptyColor = MaterialTheme.colorScheme.outlineVariant
            val frozenColor = Color(0xFF689FB8)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                // Render 5 columns of 6 rows, or 6 columns of 5 rows
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
