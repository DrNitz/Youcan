package com.drnitz.youcan.ui.countdowns

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.ZoneId

@Composable
fun AddCountdownDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, subject: String, targetEpochMillis: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var daysAheadText by remember { mutableStateOf("7") }
    var hourText by remember { mutableStateOf("9") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Exam or Deadline", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("Final Calculus Exam") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject (Optional)") },
                    placeholder = { Text("Mathematics") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = daysAheadText,
                        onValueChange = { daysAheadText = it },
                        label = { Text("Days from now") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(
                        value = hourText,
                        onValueChange = { hourText = it },
                        label = { Text("Hour (0-23)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
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
                    val days = daysAheadText.toIntOrNull()
                    val hour = hourText.toIntOrNull()
                    if (title.isBlank()) {
                        error = "Title cannot be empty"
                    } else if (days == null || days < 0) {
                        error = "Enter valid days ahead"
                    } else if (hour == null || hour !in 0..23) {
                        error = "Enter valid hour (0-23)"
                    } else {
                        val targetDateTime = LocalDateTime.now()
                            .plusDays(days.toLong())
                            .withHour(hour)
                            .withMinute(0)
                            .withSecond(0)
                        val targetMillis = targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        onSave(title.trim(), subject.trim(), targetMillis)
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
