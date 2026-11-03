package com.drnitz.youcan.ui.streaks

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Button
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
import com.drnitz.youcan.widget.StreakWidgetReceiver
import java.time.LocalDate

@Composable
fun StreaksScreen(
    streakData: StreakData,
    sessions: List<FocusSessionEntity>,
    onFreezeStreak: (LocalDate) -> Unit,
    onManualLog: (subject: String, minutes: Int, note: String) -> Unit,
    onDeleteSession: (FocusSessionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showManualDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StreakHeroCard(
                streakData = streakData,
                onFreezeStreak = { onFreezeStreak(LocalDate.now()) },
                onShareCard = { showShareDialog = true }
            )
        }

        item {
            StudyHeatmapView(past30Days = streakData.past30Days)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Study History",
                    style = MaterialTheme.typography.titleLarge
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            WidgetPinningHelper.pinWidget(context, StreakWidgetReceiver::class.java, "Streak Widget")
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

                    Button(
                        onClick = { showManualDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Log Time")
                    }
                }
            }
        }

        if (sessions.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No study sessions recorded yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Complete a focus session or log manual study above",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(sessions, key = { it.id }) { session ->
                SessionHistoryItem(
                    session = session,
                    onDelete = { onDeleteSession(session) }
                )
            }
        }
    }

    if (showManualDialog) {
        ManualSessionEntryDialog(
            onDismiss = { showManualDialog = false },
            onSave = { subject, mins, note ->
                onManualLog(subject, mins, note)
                showManualDialog = false
            }
        )
    }

    if (showShareDialog) {
        ShareableStreakCardDialog(
            streakData = streakData,
            onDismiss = { showShareDialog = false }
        )
    }
}
