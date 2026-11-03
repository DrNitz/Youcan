package com.drnitz.youcan.ui.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.drnitz.youcan.domain.model.TimerStatus

@Composable
fun TimerControls(
    status: TimerStatus,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stop Button
        if (status != TimerStatus.IDLE) {
            IconButton(
                onClick = onStop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Stop,
                    contentDescription = "Stop Session",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Main Action Button (Play / Pause / Resume)
        IconButton(
            onClick = {
                when (status) {
                    TimerStatus.IDLE -> onStart()
                    TimerStatus.RUNNING -> onPause()
                    TimerStatus.PAUSED -> onResume()
                    TimerStatus.COMPLETED -> onStart()
                }
            },
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            val icon = if (status == TimerStatus.RUNNING) {
                Icons.Rounded.Pause
            } else {
                Icons.Rounded.PlayArrow
            }
            val label = when (status) {
                TimerStatus.RUNNING -> "Pause Session"
                TimerStatus.PAUSED -> "Resume Session"
                else -> "Start Session"
            }
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(38.dp)
            )
        }

        // Skip Phase Button
        if (status != TimerStatus.IDLE) {
            IconButton(
                onClick = onSkip,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Skip to Next Phase",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
