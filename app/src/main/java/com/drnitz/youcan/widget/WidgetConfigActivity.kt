package com.drnitz.youcan.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.drnitz.youcan.core.designsystem.YoucanTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class WidgetPreset(
    val id: String,
    val name: String,
    val description: String,
    val iconEmoji: String,
    val mode: WidgetLayoutMode,
    val comp1: WidgetComponent,
    val comp2: WidgetComponent,
    val recommendedTheme: WidgetBackgroundTheme
)

val WIDGET_PRESETS = listOf(
    WidgetPreset(
        id = "focus_dashboard",
        name = "Focus Dashboard",
        description = "Timer & Tasks (Split)",
        iconEmoji = "🎯",
        mode = WidgetLayoutMode.COMBO_HORIZONTAL,
        comp1 = WidgetComponent.TIMER,
        comp2 = WidgetComponent.TASKS,
        recommendedTheme = WidgetBackgroundTheme.FOREST
    ),
    WidgetPreset(
        id = "mindful_milestone",
        name = "Mindful Milestone",
        description = "Quotes & Exam Countdown",
        iconEmoji = "💡",
        mode = WidgetLayoutMode.COMBO_VERTICAL,
        comp1 = WidgetComponent.QUOTE,
        comp2 = WidgetComponent.COUNTDOWN,
        recommendedTheme = WidgetBackgroundTheme.WARM_EARTH
    ),
    WidgetPreset(
        id = "consistency_stats",
        name = "Consistency & Stats",
        description = "Streaks & Study Hours",
        iconEmoji = "🔥",
        mode = WidgetLayoutMode.COMBO_HORIZONTAL,
        comp1 = WidgetComponent.STREAK,
        comp2 = WidgetComponent.ANALYTICS,
        recommendedTheme = WidgetBackgroundTheme.CHARCOAL
    ),
    WidgetPreset(
        id = "digital_balance",
        name = "Digital Balance",
        description = "Usage & Streaks",
        iconEmoji = "📱",
        mode = WidgetLayoutMode.COMBO_HORIZONTAL,
        comp1 = WidgetComponent.USAGE,
        comp2 = WidgetComponent.STREAK,
        recommendedTheme = WidgetBackgroundTheme.NAVY
    ),
    WidgetPreset(
        id = "pure_timer",
        name = "Pure Timer",
        description = "Minimalist Pomodoro",
        iconEmoji = "⏱️",
        mode = WidgetLayoutMode.SINGLE,
        comp1 = WidgetComponent.TIMER,
        comp2 = WidgetComponent.COUNTDOWN,
        recommendedTheme = WidgetBackgroundTheme.AMOLED
    ),
    WidgetPreset(
        id = "daily_tasks",
        name = "Study Checklist",
        description = "Tasks & Reminders",
        iconEmoji = "📝",
        mode = WidgetLayoutMode.SINGLE,
        comp1 = WidgetComponent.TASKS,
        comp2 = WidgetComponent.COUNTDOWN,
        recommendedTheme = WidgetBackgroundTheme.FOREST
    )
)

class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        setContent {
            YoucanTheme {
                WidgetConfigScreen(
                    isStandalone = appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID,
                    onSave = { mode, comp1, comp2, theme ->
                        saveWidgetPrefs(mode, comp1, comp2, theme)
                    }
                )
            }
        }
    }

    private fun saveWidgetPrefs(
        mode: WidgetLayoutMode,
        comp1: WidgetComponent,
        comp2: WidgetComponent,
        theme: WidgetBackgroundTheme
    ) {
        val sp = getSharedPreferences("youcan_widget_prefs", Context.MODE_PRIVATE)
        val editor = sp.edit()

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            editor.putString("widget_${appWidgetId}_layout_mode", mode.name)
            editor.putString("widget_${appWidgetId}_comp1", comp1.name)
            editor.putString("widget_${appWidgetId}_comp2", comp2.name)
            editor.putString("widget_${appWidgetId}_theme", theme.name)
        }

        // Also save fallback global keys
        editor.putString("widget_layout_mode", mode.name)
        editor.putString("widget_comp1", comp1.name)
        editor.putString("widget_comp2", comp2.name)
        editor.putString("widget_theme", theme.name)
        editor.putString("widget_type", comp1.name)
        editor.putBoolean("widget_amoled", theme == WidgetBackgroundTheme.AMOLED)
        editor.apply()

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(Activity.RESULT_OK, resultValue)

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val glanceManager = GlanceAppWidgetManager(this@WidgetConfigActivity)
                    val glanceId = glanceManager.getGlanceIdBy(appWidgetId)
                    editor.putString("widget_${glanceId}_layout_mode", mode.name)
                    editor.putString("widget_${glanceId}_comp1", comp1.name)
                    editor.putString("widget_${glanceId}_comp2", comp2.name)
                    editor.putString("widget_${glanceId}_theme", theme.name)
                    editor.apply()

                    YoucanGlanceWidget().update(this@WidgetConfigActivity, glanceId)
                } catch (_: Exception) {}
                WidgetUpdateHelper.updateAll(this@WidgetConfigActivity)
                finish()
            }
        } else {
            WidgetUpdateHelper.updateAll(this@WidgetConfigActivity)
            Toast.makeText(this, "Default widget style saved & widgets updated!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

@Composable
fun WidgetConfigScreen(
    isStandalone: Boolean = false,
    onSave: (WidgetLayoutMode, WidgetComponent, WidgetComponent, WidgetBackgroundTheme) -> Unit
) {
    var layoutMode by remember { mutableStateOf(WidgetLayoutMode.SINGLE) }
    var comp1 by remember { mutableStateOf(WidgetComponent.TIMER) }
    var comp2 by remember { mutableStateOf(WidgetComponent.COUNTDOWN) }
    var selectedTheme by remember { mutableStateOf(WidgetBackgroundTheme.FOREST) }

    Scaffold(
        bottomBar = {
            Box(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Button(
                    onClick = { onSave(layoutMode, comp1, comp2, selectedTheme) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (isStandalone) "Save Default Widget Style" else "Apply & Place Widget")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Configure Live Widget",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Choose combo presets, split layouts, and custom background themes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Combos & Presets Row
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp).padding(end = 4.dp)
                    )
                    Text("Pre-configured Combos & Presets", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(WIDGET_PRESETS) { preset ->
                        val isSelected = layoutMode == preset.mode && comp1 == preset.comp1 &&
                                (preset.mode == WidgetLayoutMode.SINGLE || comp2 == preset.comp2)

                        Card(
                            modifier = Modifier
                                .width(150.dp)
                                .clickable {
                                    layoutMode = preset.mode
                                    comp1 = preset.comp1
                                    comp2 = preset.comp2
                                    selectedTheme = preset.recommendedTheme
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(preset.iconEmoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = preset.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Live Interactive Preview
            item {
                Text("Live Preview", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(selectedTheme.colorHex))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (layoutMode) {
                            WidgetLayoutMode.SINGLE -> {
                                ComponentPreview(comp1, isCompact = false)
                            }
                            WidgetLayoutMode.COMBO_HORIZONTAL -> {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        ComponentPreview(comp1, isCompact = true)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        ComponentPreview(comp2, isCompact = true)
                                    }
                                }
                            }
                            WidgetLayoutMode.COMBO_VERTICAL -> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        ComponentPreview(comp1, isCompact = true)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        ComponentPreview(comp2, isCompact = true)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Layout Mode Selector
            item {
                Text("Layout Mode", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(WidgetLayoutMode.entries) { mode ->
                        FilterChip(
                            selected = layoutMode == mode,
                            onClick = { layoutMode = mode },
                            label = { Text(mode.displayName) }
                        )
                    }
                }
            }

            // Theme Selector
            item {
                Text("Background Theme", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(WidgetBackgroundTheme.entries) { theme ->
                        val isSelected = selectedTheme == theme
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedTheme = theme }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(theme.colorHex))
                                    .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = theme.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Primary Component Selector
            item {
                Text(
                    text = if (layoutMode == WidgetLayoutMode.SINGLE) "Widget Component" else "Component 1 (Primary / Left)",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            items(WidgetComponent.entries) { comp ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { comp1 = comp }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = comp1 == comp,
                        onClick = { comp1 = comp }
                    )
                    Text(
                        text = comp.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Secondary Component Selector (for Combo)
            if (layoutMode != WidgetLayoutMode.SINGLE) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Component 2 (Secondary / Right)",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                items(WidgetComponent.entries) { comp ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { comp2 = comp }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = comp2 == comp,
                            onClick = { comp2 = comp }
                        )
                        Text(
                            text = comp.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ComponentPreview(component: WidgetComponent, isCompact: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        val primaryColor = Color(0xFF7EB09B)
        when (component) {
            WidgetComponent.TIMER -> {
                Text(
                    text = "FOCUS • General Study",
                    color = primaryColor,
                    fontSize = if (isCompact) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "25:00",
                    color = Color.White,
                    fontSize = if (isCompact) 20.sp else 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "▶ Start Focus",
                    color = primaryColor,
                    fontSize = if (isCompact) 10.sp else 11.sp
                )
            }
            WidgetComponent.TASKS -> {
                Text(
                    text = "Today's Tasks (2/3)",
                    color = primaryColor,
                    fontSize = if (isCompact) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "[✓] Review Anatomy Ch. 4",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = if (isCompact) 9.sp else 11.sp
                )
                Text(
                    text = "[ ] Solve Physics Problems ⏰ 9:00 AM",
                    color = Color.White,
                    fontSize = if (isCompact) 9.sp else 11.sp
                )
            }
            WidgetComponent.COUNTDOWN -> {
                Text(
                    text = "Final Exam Countdown",
                    color = primaryColor,
                    fontSize = if (isCompact) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "14 DAYS LEFT",
                    color = Color(0xFFFFB74D),
                    fontSize = if (isCompact) 18.sp else 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Nov 15 • Medicine",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
            WidgetComponent.STREAK -> {
                Text(
                    text = "Consistency Streak",
                    color = primaryColor,
                    fontSize = if (isCompact) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "🔥 7 Days",
                    color = Color(0xFFFF8A65),
                    fontSize = if (isCompact) 18.sp else 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Today: 45/120 min",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
            WidgetComponent.QUOTE -> {
                Text(
                    text = "“Small steps made consistently create profound changes.”",
                    color = Color.White,
                    fontSize = if (isCompact) 10.sp else 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "— James Clear  ↻",
                    color = primaryColor,
                    fontSize = 10.sp
                )
            }
            WidgetComponent.USAGE -> {
                Text(
                    text = "Screen Time Today",
                    color = primaryColor,
                    fontSize = if (isCompact) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "2h 45m",
                    color = Color.White,
                    fontSize = if (isCompact) 18.sp else 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Balanced Focus",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
            WidgetComponent.ANALYTICS -> {
                Text(
                    text = "Weekly Analytics",
                    color = primaryColor,
                    fontSize = if (isCompact) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "14.5 hrs",
                    color = Color.White,
                    fontSize = if (isCompact) 18.sp else 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "6 sessions completed",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }
    }
}
