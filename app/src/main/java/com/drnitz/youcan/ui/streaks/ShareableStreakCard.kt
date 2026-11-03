package com.drnitz.youcan.ui.streaks

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.drnitz.youcan.domain.model.StreakData

@Composable
fun ShareableStreakCardDialog(
    streakData: StreakData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share Your Streak", style = MaterialTheme.typography.titleLarge) },
        text = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141916))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1C2721), Color(0xFF121614))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color(0xFF263D31)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFE2B774),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "${streakData.currentStreak} Day Focus Streak",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFFF1F3F0)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Daily Goal: ${streakData.dailyGoalMinutes} Minutes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFA6B0A9)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Personal Best", style = MaterialTheme.typography.labelSmall, color = Color(0xFF737D76))
                                Text("${streakData.longestStreak} days", style = MaterialTheme.typography.titleMedium, color = Color(0xFFF1F3F0))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Today Studied", style = MaterialTheme.typography.labelSmall, color = Color(0xFF737D76))
                                Text("${streakData.todayMinutes} mins", style = MaterialTheme.typography.titleMedium, color = Color(0xFF7EB09B))
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Studied with Youcan Focus",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF737D76)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    shareStreakText(context, streakData)
                    onDismiss()
                }
            ) {
                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("Share")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private fun shareStreakText(context: Context, streakData: StreakData) {
    val message = "I am on a ${streakData.currentStreak}-day focus streak with Youcan! Today's study: ${streakData.todayMinutes} minutes. Keeping the momentum going."
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share your streak")
    context.startActivity(shareIntent)
}
