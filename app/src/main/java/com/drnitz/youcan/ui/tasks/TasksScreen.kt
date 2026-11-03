package com.drnitz.youcan.ui.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.drnitz.youcan.domain.model.TaskItem
import com.drnitz.youcan.widget.TasksWidgetReceiver
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TasksScreen(
    tasks: List<TaskItem>,
    selectedDate: LocalDate,
    onDateChanged: (LocalDate) -> Unit,
    onAddTask: (
        task: String,
        date: LocalDate,
        targetMinutes: Int,
        hasReminder: Boolean,
        reminderHour: Int?,
        reminderMinute: Int?
    ) -> Unit,
    onToggleTask: (TaskItem) -> Unit,
    onDeleteTask: (TaskItem) -> Unit,
    onUpdateTaskReminder: (TaskItem, hasReminder: Boolean, hour: Int?, minute: Int?) -> Unit,
    onCopyTasksToTomorrow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showCalendarPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val today = remember { LocalDate.now() }
    val tomorrow = remember { today.plusDays(1) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d") }

    if (showAddDialog) {
        AddTaskDialog(
            initialDate = selectedDate,
            onConfirm = { task, date, targetMinutes, hasReminder, reminderHour, reminderMinute ->
                onAddTask(task, date, targetMinutes, hasReminder, reminderHour, reminderMinute)
            },
            onDismiss = { showAddDialog = false }
        )
    }

    if (showCalendarPicker) {
        CustomCalendarPickerDialog(
            initialDate = selectedDate,
            onDateSelected = { onDateChanged(it) },
            onDismiss = { showCalendarPicker = false }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tasks & Reminders",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Schedule goals and set study alarms",
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
                            WidgetPinningHelper.pinWidget(context, TasksWidgetReceiver::class.java, "Tasks Widget")
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Widgets,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pin Widget")
                    }

                    Button(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("New")
                    }
                }
            }
        }

        // Date selection filter tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        FilterChip(
                            selected = selectedDate == today,
                            onClick = { onDateChanged(today) },
                            label = { Text("Today") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedDate == tomorrow,
                            onClick = { onDateChanged(tomorrow) },
                            label = { Text("Tomorrow") }
                        )
                    }
                    if (selectedDate != today && selectedDate != tomorrow) {
                        item {
                            FilterChip(
                                selected = true,
                                onClick = { showCalendarPicker = true },
                                label = { Text(selectedDate.format(dateFormatter)) }
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.clickable { showCalendarPicker = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = "Pick date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Calendar",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }

        // Task progress summary card
        if (tasks.isNotEmpty()) {
            item {
                val completedCount = tasks.count { it.isCompleted }
                val totalCount = tasks.size
                val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedDate.format(dateFormatter)} Progress",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "$completedCount of $totalCount completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }

        // Task list
        if (tasks.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No tasks for ${selectedDate.format(dateFormatter)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add Task with Reminder")
                    }
                }
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onToggleCompletion = onToggleTask,
                    onDelete = onDeleteTask,
                    onUpdateReminder = onUpdateTaskReminder
                )
            }

            if (selectedDate == today) {
                item {
                    Button(
                        onClick = onCopyTasksToTomorrow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Copy Incomplete Tasks to Tomorrow")
                    }
                }
            }
        }
    }
}
