package com.drnitz.youcan.ui.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddTaskDialog(
    initialDate: LocalDate = LocalDate.now(),
    onConfirm: (
        task: String,
        date: LocalDate,
        targetMinutes: Int,
        hasReminder: Boolean,
        reminderHour: Int?,
        reminderMinute: Int?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var taskText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var targetMinutes by remember { mutableIntStateOf(25) }
    var hasReminder by remember { mutableStateOf(false) }
    var reminderHour by remember { mutableIntStateOf(9) }
    var reminderMinute by remember { mutableIntStateOf(0) }

    var showCalendarPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d") }

    val reminderTimeFormatted = remember(reminderHour, reminderMinute) {
        val amPm = if (reminderHour >= 12) "PM" else "AM"
        val displayHour = when {
            reminderHour == 0 -> 12
            reminderHour > 12 -> reminderHour - 12
            else -> reminderHour
        }
        String.format("%d:%02d %s", displayHour, reminderMinute, amPm)
    }

    if (showCalendarPicker) {
        CustomCalendarPickerDialog(
            initialDate = selectedDate,
            onDateSelected = { selectedDate = it },
            onDismiss = { showCalendarPicker = false }
        )
    }

    if (showTimePicker) {
        CustomTimePickerDialog(
            initialHour = reminderHour,
            initialMinute = reminderMinute,
            onTimeSelected = { h, m ->
                reminderHour = h
                reminderMinute = m
            },
            onDismiss = { showTimePicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Study Task",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = taskText,
                    onValueChange = { taskText = it },
                    label = { Text("What are you studying?") },
                    placeholder = { Text("e.g. Read Physics Chapter 4") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Date Picker trigger
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCalendarPicker = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Date: ${selectedDate.format(dateFormatter)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            text = if (selectedDate == LocalDate.now()) "Today" else "Change",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Target minutes selector
                Column {
                    Text(text = "Target Study Time", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(0 to "None", 15 to "15m", 25 to "25m", 45 to "45m", 60 to "60m")) { (mins, label) ->
                            FilterChip(
                                selected = targetMinutes == mins,
                                onClick = { targetMinutes = mins },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                // Reminder section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    tint = if (hasReminder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "Set Reminder", style = MaterialTheme.typography.bodyMedium)
                            }
                            Switch(
                                checked = hasReminder,
                                onCheckedChange = { hasReminder = it }
                            )
                        }

                        if (hasReminder) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showTimePicker = true }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Alert at: $reminderTimeFormatted",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Change Time",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (taskText.isNotBlank()) {
                        onConfirm(
                            taskText.trim(),
                            selectedDate,
                            targetMinutes,
                            hasReminder,
                            if (hasReminder) reminderHour else null,
                            if (hasReminder) reminderMinute else null
                        )
                        onDismiss()
                    }
                },
                enabled = taskText.isNotBlank()
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
