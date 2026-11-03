package com.drnitz.youcan.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SubjectDistributionChart(
    subjectMinutes: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val totalMinutes = subjectMinutes.values.sum().coerceAtLeast(1)
    val colors = listOf(
        Color(0xFF7EB09B),
        Color(0xFFE2B774),
        Color(0xFF689FB8),
        Color(0xFF9B87BA),
        Color(0xFFD67A66),
        Color(0xFF88A282)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Subject Breakdown",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (subjectMinutes.isEmpty() || totalMinutes == 0) {
                Text(
                    text = "No study sessions recorded yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Segmented bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                ) {
                    var colorIdx = 0
                    for ((_, minutes) in subjectMinutes) {
                        val weight = (minutes.toFloat() / totalMinutes.toFloat()).coerceAtLeast(0.01f)
                        Box(
                            modifier = Modifier
                                .weight(weight)
                                .height(14.dp)
                                .background(colors[colorIdx % colors.size])
                        )
                        colorIdx++
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Legend
                var legendIdx = 0
                for ((subject, minutes) in subjectMinutes) {
                    val percent = (minutes * 100) / totalMinutes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(colors[legendIdx % colors.size])
                            )
                            Text(
                                text = subject,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Text(
                            text = "$minutes mins ($percent%)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    legendIdx++
                }
            }
        }
    }
}
