package com.drnitz.youcan.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.drnitz.youcan.core.util.WidgetPinningHelper
import com.drnitz.youcan.data.local.entity.FocusSessionEntity
import com.drnitz.youcan.domain.model.StreakData
import com.drnitz.youcan.widget.AnalyticsWidgetReceiver
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@Composable
fun AnalyticsScreen(
    sessions: List<FocusSessionEntity>,
    streakData: StreakData,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)

    // Compute 7-day trend
    val past7Days = (0..6).map { today.minusDays(it.toLong()) }
    val past7DaysMinutes = remember(sessions) {
        val map = past7Days.associateWith { 0 }.toMutableMap()
        for (session in sessions) {
            val date = Instant.ofEpochMilli(session.startTime).atZone(zone).toLocalDate()
            if (map.containsKey(date)) {
                map[date] = (map[date] ?: 0) + (session.durationSeconds / 60)
            }
        }
        map
    }

    // Compute subject breakdown
    val subjectMinutes = remember(sessions) {
        val map = mutableMapOf<String, Int>()
        for (session in sessions) {
            val sub = session.subject.ifBlank { "General Study" }
            map[sub] = (map[sub] ?: 0) + (session.durationSeconds / 60)
        }
        map.toList().sortedByDescending { it.second }.toMap()
    }

    // Compute peak study time slots
    val timeSlotMinutes = remember(sessions) {
        var morning = 0
        var afternoon = 0
        var evening = 0
        var night = 0

        for (session in sessions) {
            val hour = Instant.ofEpochMilli(session.startTime).atZone(zone).hour
            val mins = session.durationSeconds / 60
            when (hour) {
                in 6..11 -> morning += mins
                in 12..17 -> afternoon += mins
                in 18..21 -> evening += mins
                else -> night += mins
            }
        }

        mapOf(
            "Morning (6 AM - 12 PM)" to morning,
            "Afternoon (12 PM - 6 PM)" to afternoon,
            "Evening (6 PM - 10 PM)" to evening,
            "Night (10 PM - 6 AM)" to night
        )
    }

    val totalHoursStudied = sessions.sumOf { it.durationSeconds } / 3600
    val totalSessionsCount = sessions.size

    var celebrationParties by remember { mutableStateOf<List<Party>>(emptyList()) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Study Analytics",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Insights into your focus and habits",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                WidgetPinningHelper.pinWidget(context, AnalyticsWidgetReceiver::class.java, "Study Analytics Widget")
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Widgets,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pin Widget")
                        }

                        if (streakData.isGoalMetToday || streakData.currentStreak >= 3) {
                            Button(
                                onClick = {
                                    celebrationParties = listOf(
                                        Party(
                                            speed = 0f,
                                            maxSpeed = 30f,
                                            damping = 0.9f,
                                            spread = 360,
                                            colors = listOf(0xFF7EB09B.toInt(), 0xFFE2B774.toInt(), 0xFF689FB8.toInt()),
                                            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(80),
                                            position = Position.Relative(0.5, 0.3)
                                        )
                                    )
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Celebration,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text("Celebrate")
                            }
                        }
                    }
                }
            }

            // High-level stat cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Total Focused", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${totalHoursStudied}h", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Sessions Logged", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$totalSessionsCount", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }

            // 7-day trend
            item {
                WeeklyTrendBarChart(
                    past7DaysMinutes = past7DaysMinutes,
                    dailyGoalMinutes = streakData.dailyGoalMinutes
                )
            }

            // Subject Distribution
            item {
                SubjectDistributionChart(subjectMinutes = subjectMinutes)
            }

            // Peak Study Hours
            item {
                BestStudyHoursChart(timeSlotMinutes = timeSlotMinutes)
            }
        }

        if (celebrationParties.isNotEmpty()) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = celebrationParties
            )
        }
    }
}
