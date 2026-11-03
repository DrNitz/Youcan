package com.drnitz.youcan.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.drnitz.youcan.core.util.PermissionHelper
import com.drnitz.youcan.core.util.ShortcutHelper
import com.drnitz.youcan.data.preferences.BackgroundType
import com.drnitz.youcan.data.preferences.UserPreferences
import com.drnitz.youcan.service.overlay.FloatingOverlayService
import com.drnitz.youcan.widget.WidgetConfigActivity

@Composable
fun SettingsScreen(
    userPreferences: UserPreferences,
    onUpdateDurations: (focus: Int, shortBreak: Int, longBreak: Int, cycles: Int) -> Unit,
    onUpdateDailyGoal: (Int) -> Unit,
    onUpdateBackground: (type: BackgroundType, customUri: String, blur: Float, dim: Float) -> Unit,
    onUpdateAlerts: (sound: Boolean, vibration: Boolean) -> Unit,
    onSetFloatingDock: (Boolean) -> Unit,
    onSetAmoledMode: (Boolean) -> Unit,
    onSetReducedMotion: (Boolean) -> Unit,
    onNavigateToWidgetStudio: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var focusMinutesText by remember(userPreferences.focusDurationMinutes) {
        mutableStateOf(userPreferences.focusDurationMinutes.toString())
    }
    var shortBreakText by remember(userPreferences.shortBreakMinutes) {
        mutableStateOf(userPreferences.shortBreakMinutes.toString())
    }
    var longBreakText by remember(userPreferences.longBreakMinutes) {
        mutableStateOf(userPreferences.longBreakMinutes.toString())
    }
    var cyclesText by remember(userPreferences.cyclesBeforeLongBreak) {
        mutableStateOf(userPreferences.cyclesBeforeLongBreak.toString())
    }
    var dailyGoalText by remember(userPreferences.dailyStudyGoalMinutes) {
        mutableStateOf(userPreferences.dailyStudyGoalMinutes.toString())
    }

    var selectedBackground by remember(userPreferences.backgroundType) {
        mutableStateOf(userPreferences.backgroundType)
    }
    var customUri by remember(userPreferences.customWallpaperUri) {
        mutableStateOf(userPreferences.customWallpaperUri)
    }
    var wallpaperDim by remember(userPreferences.wallpaperDimAlpha) {
        mutableFloatStateOf(userPreferences.wallpaperDimAlpha)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            customUri = uri.toString()
            selectedBackground = BackgroundType.CUSTOM
            onUpdateBackground(BackgroundType.CUSTOM, uri.toString(), 10f, wallpaperDim)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Configure intervals, ambient styles, and overlay docks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Pomodoro Intervals Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Timer Intervals", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = focusMinutesText,
                            onValueChange = { focusMinutesText = it },
                            label = { Text("Focus (min)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = shortBreakText,
                            onValueChange = { shortBreakText = it },
                            label = { Text("Short Break") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = longBreakText,
                            onValueChange = { longBreakText = it },
                            label = { Text("Long Break") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = cyclesText,
                            onValueChange = { cyclesText = it },
                            label = { Text("Cycles") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = dailyGoalText,
                        onValueChange = { dailyGoalText = it },
                        label = { Text("Daily Study Goal (min)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val f = focusMinutesText.toIntOrNull() ?: 25
                            val sb = shortBreakText.toIntOrNull() ?: 5
                            val lb = longBreakText.toIntOrNull() ?: 15
                            val c = cyclesText.toIntOrNull() ?: 4
                            val g = dailyGoalText.toIntOrNull() ?: 120
                            onUpdateDurations(f, sb, lb, c)
                            onUpdateDailyGoal(g)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Interval Durations")
                    }
                }
            }
        }

        // Ambient Background Engine
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Ambient Background", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(10.dp))

                    for (bg in BackgroundType.entries) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedBackground = bg
                                    onUpdateBackground(bg, customUri, 10f, wallpaperDim)
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedBackground == bg,
                                onClick = {
                                    selectedBackground = bg
                                    onUpdateBackground(bg, customUri, 10f, wallpaperDim)
                                }
                            )
                            Text(
                                text = bg.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    if (selectedBackground == BackgroundType.CUSTOM) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                            Text("Select Wallpaper Photo")
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Dark Contrast Scrim: ${(wallpaperDim * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = wallpaperDim,
                            onValueChange = {
                                wallpaperDim = it
                                onUpdateBackground(selectedBackground, customUri, 10f, it)
                            },
                            valueRange = 0.2f..0.85f
                        )
                    }
                }
            }
        }

        // Audio & Haptic Alerts
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Sound & Vibration", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Phase Completion Audio Cue", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = userPreferences.soundAlertEnabled,
                            onCheckedChange = { onUpdateAlerts(it, userPreferences.vibrationAlertEnabled) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Phase End Gentle Vibration", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = userPreferences.vibrationAlertEnabled,
                            onCheckedChange = { onUpdateAlerts(userPreferences.soundAlertEnabled, it) }
                        )
                    }
                }
            }
        }

        // Floating Overlay Dock & Island
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Draggable Floating Dock", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Show a floating compact timer pill over other apps during active study.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val hasOverlayPermission = PermissionHelper.hasOverlayPermission(context)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Floating Overlay", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = userPreferences.floatingDockEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    if (hasOverlayPermission) {
                                        onSetFloatingDock(true)
                                        FloatingOverlayService.start(context)
                                    } else {
                                        PermissionHelper.openOverlaySettings(context)
                                    }
                                } else {
                                    onSetFloatingDock(false)
                                    FloatingOverlayService.stop(context)
                                }
                            }
                        )
                    }

                    if (!hasOverlayPermission) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { PermissionHelper.openOverlaySettings(context) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Grant Overlay Permission")
                        }
                    }
                }
            }
        }

        // Widgets & Desktop Shortcuts
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Widgets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp).padding(end = 6.dp)
                        )
                        Text("Widgets & Home Shortcuts", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Customize live Glance widgets, combo presets, and pin 1-tap desktop shortcuts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            if (onNavigateToWidgetStudio != null) {
                                onNavigateToWidgetStudio()
                            } else {
                                val intent = Intent(context, WidgetConfigActivity::class.java)
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(18.dp).padding(end = 6.dp))
                        Text("Live Widget Customizer & Presets")
                    }

                    if (ShortcutHelper.isPinningSupported(context)) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    ShortcutHelper.requestPinShortcut(context, ShortcutHelper.ShortcutTarget.START_TIMER)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.PushPin, contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                                Text("Pin Timer", maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = {
                                    ShortcutHelper.requestPinShortcut(context, ShortcutHelper.ShortcutTarget.ADD_TASK)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.PushPin, contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                                Text("Pin Task", maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // Display & Battery Optimization
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Battery & Performance", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("True Black AMOLED Mode", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = userPreferences.amoledModeEnabled,
                            onCheckedChange = onSetAmoledMode
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reduce Canvas Motion", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = userPreferences.reducedMotionOverride,
                            onCheckedChange = onSetReducedMotion
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { PermissionHelper.openUsageAccessSettings(context) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Configure App Usage Access")
                    }
                }
            }
        }
    }
}
