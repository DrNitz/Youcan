package com.drnitz.youcan.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drnitz.youcan.domain.model.CountdownItem
import com.drnitz.youcan.domain.model.TaskItem
import com.drnitz.youcan.ui.tasks.AddTaskDialog
import java.time.LocalDate

@Composable
fun HomeScreen(
    nearestCountdown: CountdownItem?,
    plans: List<TaskItem>,
    onStartFocus: () -> Unit,
    onNavigateToCountdowns: () -> Unit,
    onTogglePlan: (TaskItem) -> Unit,
    onDeletePlan: (TaskItem) -> Unit,
    onAddTask: (task: String, date: LocalDate, targetMinutes: Int, hasReminder: Boolean, reminderHour: Int?, reminderMinute: Int?) -> Unit,
    onCopyPlanToTomorrow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddTaskDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Fast Start CTA
        item {
            Button(
                onClick = onStartFocus,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).padding(end = 6.dp)
                )
                Text(
                    text = "Start Focus Session",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // Nearest Countdown Milestone
        if (nearestCountdown != null) {
            item {
                NearestCountdownCard(
                    countdown = nearestCountdown,
                    onClick = onNavigateToCountdowns
                )
            }
        }

        // Daily Motivational Quote
        item {
            MotivationalQuoteCard()
        }

        // Today's Study Plan Checklist
        item {
            TodayPlanSection(
                plans = plans,
                onTogglePlan = onTogglePlan,
                onDeletePlan = onDeletePlan,
                onAddTask = { showAddTaskDialog = true },
                onCopyPlanToTomorrow = onCopyPlanToTomorrow
            )
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            initialDate = LocalDate.now(),
            onConfirm = { task, date, targetMinutes, hasReminder, reminderHour, reminderMinute ->
                onAddTask(task, date, targetMinutes, hasReminder, reminderHour, reminderMinute)
                showAddTaskDialog = false
            },
            onDismiss = { showAddTaskDialog = false }
        )
    }
}
