package com.drnitz.youcan.ui.reminders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, hour: Int, minute: Int, daysOfWeek: List<Int>) -> Unit
) {
    var title by remember { mutableStateOf("Study Focus Time") }
    var hourText by remember { mutableStateOf("18") }
    var minuteText by remember { mutableStateOf("00") }
    val selectedDays = remember { mutableStateListOf(1, 2, 3, 4, 5) } // Mon-Fri by default
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Study Reminder", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = hourText,
                        onValueChange = { hourText = it },
                        label = { Text("Hour (0-23)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(
                        value = minuteText,
                        onValueChange = { minuteText = it },
                        label = { Text("Minute (0-59)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Repeat on days (empty for one-off):",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 1..7) {
                        val isSelected = selectedDays.contains(i)
                        val dayLabel = DayOfWeek.of(i).getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedDays.remove(i) else selectedDays.add(i)
                            },
                            label = { Text(dayLabel) }
                        )
                    }
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hour = hourText.trim().toIntOrNull()
                    val minute = minuteText.trim().toIntOrNull()
                    if (title.isBlank()) {
                        error = "Title cannot be empty"
                    } else if (hour == null || hour !in 0..23) {
                        error = "Hour must be between 0 and 23"
                    } else if (minute == null || minute !in 0..59) {
                        error = "Minute must be between 0 and 59"
                    } else {
                        onSave(title.trim(), hour, minute, selectedDays.sorted())
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
