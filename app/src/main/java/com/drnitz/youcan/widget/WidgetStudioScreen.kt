package com.drnitz.youcan.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.drnitz.youcan.core.util.WidgetPinningHelper
import com.drnitz.youcan.domain.model.QuoteProvider
import kotlinx.coroutines.launch

data class WidgetPreset(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val mode: WidgetLayoutMode,
    val comp1: WidgetComponent,
    val comp2: WidgetComponent,
    val recommendedTheme: WidgetBackgroundTheme,
    val recommendedAlpha: Float = 0.95f
)

val WIDGET_PRESETS = listOf(
    WidgetPreset(
        id = "focus_dashboard",
        name = "Focus Dashboard",
        description = "Timer & Tasks (Split)",
        icon = Icons.Rounded.Dashboard,
        mode = WidgetLayoutMode.COMBO_HORIZONTAL,
        comp1 = WidgetComponent.TIMER,
        comp2 = WidgetComponent.TASKS,
        recommendedTheme = WidgetBackgroundTheme.FOREST,
        recommendedAlpha = 0.92f
    ),
    WidgetPreset(
        id = "frosted_focus",
        name = "Frosted Studio",
        description = "Translucent Split Glass",
        icon = Icons.Rounded.Tune,
        mode = WidgetLayoutMode.COMBO_HORIZONTAL,
        comp1 = WidgetComponent.TIMER,
        comp2 = WidgetComponent.TASKS,
        recommendedTheme = WidgetBackgroundTheme.FROSTED_GLASS,
        recommendedAlpha = 0.70f
    ),
    WidgetPreset(
        id = "mindful_milestone",
        name = "Mindful Milestone",
        description = "Quotes & Exam Countdown",
        icon = Icons.Rounded.Lightbulb,
        mode = WidgetLayoutMode.COMBO_VERTICAL,
        comp1 = WidgetComponent.QUOTE,
        comp2 = WidgetComponent.COUNTDOWN,
        recommendedTheme = WidgetBackgroundTheme.WARM_EARTH,
        recommendedAlpha = 0.90f
    ),
    WidgetPreset(
        id = "consistency_stats",
        name = "Consistency & Stats",
        description = "Streaks & Study Hours",
        icon = Icons.Rounded.LocalFireDepartment,
        mode = WidgetLayoutMode.COMBO_HORIZONTAL,
        comp1 = WidgetComponent.STREAK,
        comp2 = WidgetComponent.ANALYTICS,
        recommendedTheme = WidgetBackgroundTheme.CHARCOAL,
        recommendedAlpha = 0.95f
    ),
    WidgetPreset(
        id = "digital_balance",
        name = "Digital Balance",
        description = "Usage & Streaks",
        icon = Icons.Rounded.PhoneAndroid,
        mode = WidgetLayoutMode.COMBO_HORIZONTAL,
        comp1 = WidgetComponent.USAGE,
        comp2 = WidgetComponent.STREAK,
        recommendedTheme = WidgetBackgroundTheme.NAVY,
        recommendedAlpha = 0.90f
    ),
    WidgetPreset(
        id = "pure_timer",
        name = "Pure Timer",
        description = "Minimalist Pomodoro",
        icon = Icons.Rounded.HourglassTop,
        mode = WidgetLayoutMode.SINGLE,
        comp1 = WidgetComponent.TIMER,
        comp2 = WidgetComponent.COUNTDOWN,
        recommendedTheme = WidgetBackgroundTheme.AMOLED,
        recommendedAlpha = 1.0f
    ),
    WidgetPreset(
        id = "daily_tasks",
        name = "Study Checklist",
        description = "Tasks & Reminders",
        icon = Icons.Rounded.CheckCircle,
        mode = WidgetLayoutMode.SINGLE,
        comp1 = WidgetComponent.TASKS,
        comp2 = WidgetComponent.COUNTDOWN,
        recommendedTheme = WidgetBackgroundTheme.FOREST,
        recommendedAlpha = 0.92f
    )
)

fun componentIcon(comp: WidgetComponent): ImageVector = when (comp) {
    WidgetComponent.TIMER -> Icons.Rounded.Timer
    WidgetComponent.TASKS -> Icons.Rounded.CheckCircle
    WidgetComponent.COUNTDOWN -> Icons.Rounded.Event
    WidgetComponent.STREAK -> Icons.Rounded.LocalFireDepartment
    WidgetComponent.QUOTE -> Icons.Rounded.FormatQuote
    WidgetComponent.USAGE -> Icons.Rounded.PhoneAndroid
    WidgetComponent.ANALYTICS -> Icons.Rounded.BarChart
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetStudioScreen(
    appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
    isConfigActivity: Boolean = false,
    onBack: (() -> Unit)? = null,
    onSavedAndPlaced: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sp = remember { context.getSharedPreferences("youcan_widget_prefs", Context.MODE_PRIVATE) }

    val initialLayoutMode = remember {
        val key = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) "widget_${appWidgetId}_layout_mode" else "widget_layout_mode"
        val saved = sp.getString(key, null) ?: sp.getString("widget_layout_mode", WidgetLayoutMode.SINGLE.name)
        runCatching { WidgetLayoutMode.valueOf(saved ?: WidgetLayoutMode.SINGLE.name) }.getOrDefault(WidgetLayoutMode.SINGLE)
    }

    val initialComp1 = remember {
        val key = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) "widget_${appWidgetId}_comp1" else "widget_comp1"
        val saved = sp.getString(key, null) ?: sp.getString("widget_comp1", null) ?: sp.getString("widget_type", WidgetComponent.TIMER.name)
        runCatching { WidgetComponent.valueOf(saved ?: WidgetComponent.TIMER.name) }.getOrDefault(WidgetComponent.TIMER)
    }

    val initialComp2 = remember {
        val key = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) "widget_${appWidgetId}_comp2" else "widget_comp2"
        val saved = sp.getString(key, null) ?: sp.getString("widget_comp2", WidgetComponent.COUNTDOWN.name)
        runCatching { WidgetComponent.valueOf(saved ?: WidgetComponent.COUNTDOWN.name) }.getOrDefault(WidgetComponent.COUNTDOWN)
    }

    val initialTheme = remember {
        val key = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) "widget_${appWidgetId}_theme" else "widget_theme"
        val saved = sp.getString(key, null) ?: sp.getString("widget_theme", null)
        if (saved != null) {
            runCatching { WidgetBackgroundTheme.valueOf(saved) }.getOrDefault(WidgetBackgroundTheme.FOREST)
        } else if (sp.getBoolean("widget_amoled", false)) {
            WidgetBackgroundTheme.AMOLED
        } else {
            WidgetBackgroundTheme.FOREST
        }
    }

    val initialAlpha = remember {
        val key = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) "widget_${appWidgetId}_transparency" else "widget_transparency"
        sp.getFloat(key, sp.getFloat("widget_transparency", initialTheme.defaultAlpha)).coerceIn(0.20f, 1.0f)
    }

    var layoutMode by remember { mutableStateOf(initialLayoutMode) }
    var comp1 by remember { mutableStateOf(initialComp1) }
    var comp2 by remember { mutableStateOf(initialComp2) }
    var selectedTheme by remember { mutableStateOf(initialTheme) }
    var transparency by remember { mutableFloatStateOf(initialAlpha) }

    fun saveConfiguration(andPin: Boolean = false) {
        val editor = sp.edit()
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            editor.putString("widget_${appWidgetId}_layout_mode", layoutMode.name)
            editor.putString("widget_${appWidgetId}_comp1", comp1.name)
            editor.putString("widget_${appWidgetId}_comp2", comp2.name)
            editor.putString("widget_${appWidgetId}_theme", selectedTheme.name)
            editor.putFloat("widget_${appWidgetId}_transparency", transparency)
        }

        editor.putString("widget_layout_mode", layoutMode.name)
        editor.putString("widget_comp1", comp1.name)
        editor.putString("widget_comp2", comp2.name)
        editor.putString("widget_theme", selectedTheme.name)
        editor.putFloat("widget_transparency", transparency)
        editor.putString("widget_type", comp1.name)
        editor.putBoolean("widget_amoled", selectedTheme == WidgetBackgroundTheme.AMOLED)
        editor.apply()

        coroutineScope.launch {
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                try {
                    val glanceManager = GlanceAppWidgetManager(context)
                    val glanceId = glanceManager.getGlanceIdBy(appWidgetId)
                    editor.putString("widget_${glanceId}_layout_mode", layoutMode.name)
                    editor.putString("widget_${glanceId}_comp1", comp1.name)
                    editor.putString("widget_${glanceId}_comp2", comp2.name)
                    editor.putString("widget_${glanceId}_theme", selectedTheme.name)
                    editor.putFloat("widget_${glanceId}_transparency", transparency)
                    editor.apply()
                    YoucanGlanceWidget().update(context, glanceId)
                } catch (_: Exception) {}
            }

            WidgetUpdateHelper.updateAll(context)

            if (andPin) {
                val receiverClass = when {
                    layoutMode == WidgetLayoutMode.SINGLE && comp1 == WidgetComponent.TIMER -> TimerWidgetReceiver::class.java
                    layoutMode == WidgetLayoutMode.SINGLE && comp1 == WidgetComponent.TASKS -> TasksWidgetReceiver::class.java
                    layoutMode == WidgetLayoutMode.SINGLE && comp1 == WidgetComponent.COUNTDOWN -> CountdownWidgetReceiver::class.java
                    layoutMode == WidgetLayoutMode.SINGLE && comp1 == WidgetComponent.STREAK -> StreakWidgetReceiver::class.java
                    layoutMode == WidgetLayoutMode.SINGLE && comp1 == WidgetComponent.ANALYTICS -> AnalyticsWidgetReceiver::class.java
                    layoutMode == WidgetLayoutMode.SINGLE && comp1 == WidgetComponent.QUOTE -> QuoteWidgetReceiver::class.java
                    layoutMode == WidgetLayoutMode.SINGLE && comp1 == WidgetComponent.USAGE -> UsageWidgetReceiver::class.java
                    layoutMode != WidgetLayoutMode.SINGLE && comp1 == WidgetComponent.TIMER && comp2 == WidgetComponent.TASKS -> DashboardWidgetReceiver::class.java
                    else -> YoucanWidgetReceiver::class.java
                }
                val widgetName = when {
                    layoutMode == WidgetLayoutMode.SINGLE && comp1 == WidgetComponent.TIMER -> "Live Focus Timer"
                    layoutMode == WidgetLayoutMode.SINGLE && comp1 == WidgetComponent.TASKS -> "Tasks Checklist"
                    layoutMode == WidgetLayoutMode.SINGLE && comp1 == WidgetComponent.COUNTDOWN -> "Countdowns"
                    layoutMode == WidgetLayoutMode.SINGLE && comp1 == WidgetComponent.STREAK -> "Focus Streak"
                    layoutMode == WidgetLayoutMode.SINGLE && comp1 == WidgetComponent.ANALYTICS -> "Weekly Analytics"
                    layoutMode == WidgetLayoutMode.SINGLE && comp1 == WidgetComponent.QUOTE -> "Daily Quote"
                    layoutMode == WidgetLayoutMode.SINGLE && comp1 == WidgetComponent.USAGE -> "Digital Balance"
                    layoutMode != WidgetLayoutMode.SINGLE && comp1 == WidgetComponent.TIMER && comp2 == WidgetComponent.TASKS -> "Focus Dashboard"
                    else -> "Youcan Custom Widget"
                }
                WidgetPinningHelper.pinWidget(context, receiverClass, widgetName)
            } else {
                Toast.makeText(context, "Widget style saved & synchronized!", Toast.LENGTH_SHORT).show()
            }
            onSavedAndPlaced?.invoke()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Live Widget Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Design, preview, and customize your home screen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isConfigActivity) {
                        Button(
                            onClick = { saveConfiguration(andPin = false) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Apply & Place Widget", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { saveConfiguration(andPin = true) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Rounded.Widgets, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add to Home Screen", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { saveConfiguration(andPin = false) },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Style & Sync All Widgets")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Interactive Demo Card Section
            item {
                Text(
                    text = "Live Interactive Demo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Real-time interactive preview as it appears on your home screen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                InteractiveWidgetPreviewCard(
                    layoutMode = layoutMode,
                    comp1 = comp1,
                    comp2 = comp2,
                    theme = selectedTheme,
                    transparency = transparency
                )
            }

            // Quick Combos & Presets
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Curated Presets & Combos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(WIDGET_PRESETS) { preset ->
                        val isSelected = layoutMode == preset.mode &&
                                comp1 == preset.comp1 &&
                                (preset.mode == WidgetLayoutMode.SINGLE || comp2 == preset.comp2) &&
                                selectedTheme == preset.recommendedTheme

                        Card(
                            modifier = Modifier
                                .width(155.dp)
                                .clickable {
                                    layoutMode = preset.mode
                                    comp1 = preset.comp1
                                    comp2 = preset.comp2
                                    selectedTheme = preset.recommendedTheme
                                    transparency = preset.recommendedAlpha
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(
                                    imageVector = preset.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = preset.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Layout Mode Selector
            item {
                Text(
                    text = "Layout Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WidgetLayoutMode.entries.forEach { mode ->
                        val isSelected = layoutMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { layoutMode = mode },
                            label = { Text(mode.displayName, fontSize = 12.sp) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // Theme Selector with WCAG AA compliance
            item {
                Text(
                    text = "Background Theme",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Calibrated dark backgrounds with WCAG AA contrast ratio",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(WidgetBackgroundTheme.entries) { theme ->
                        val isSelected = selectedTheme == theme
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    selectedTheme = theme
                                    if (theme == WidgetBackgroundTheme.FROSTED_GLASS && transparency > 0.80f) {
                                        transparency = theme.defaultAlpha
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(theme.colorHex))
                                    .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
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

            // Customizable Transparency Slider
            item {
                val opacityPercent = (transparency * 100).toInt()
                val opacityLabel = when {
                    opacityPercent >= 95 -> "Solid Contrast"
                    opacityPercent >= 80 -> "Semi-Opaque"
                    opacityPercent >= 60 -> "Frosted Translucent"
                    else -> "Glass Minimal"
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Opacity & Transparency",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$opacityLabel • $opacityPercent% Opacity",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { transparency = selectedTheme.defaultAlpha }) {
                                Icon(Icons.Rounded.Refresh, contentDescription = "Reset Opacity", modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Slider(
                            value = transparency,
                            onValueChange = { transparency = it },
                            valueRange = 0.25f..1.0f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("25% Translucent", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("100% Solid", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Primary Component Selector
            item {
                Text(
                    text = if (layoutMode == WidgetLayoutMode.SINGLE) "Widget Content" else "Primary Component (Left / Top)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        WidgetComponent.entries.forEach { comp ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { comp1 = comp }
                                    .padding(vertical = 6.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = comp1 == comp,
                                    onClick = { comp1 = comp }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = componentIcon(comp),
                                    contentDescription = null,
                                    tint = if (comp1 == comp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = comp.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (comp1 == comp) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Secondary Component Selector
            if (layoutMode != WidgetLayoutMode.SINGLE) {
                item {
                    Text(
                        text = "Secondary Component (Right / Bottom)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            WidgetComponent.entries.forEach { comp ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { comp2 = comp }
                                        .padding(vertical = 6.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = comp2 == comp,
                                        onClick = { comp2 = comp }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = componentIcon(comp),
                                        contentDescription = null,
                                        tint = if (comp2 == comp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = comp.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (comp2 == comp) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveWidgetPreviewCard(
    layoutMode: WidgetLayoutMode,
    comp1: WidgetComponent,
    comp2: WidgetComponent,
    theme: WidgetBackgroundTheme,
    transparency: Float
) {
    // Simulated Home Screen Wallpaper Background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B2E33),
                        Color(0xFF0F1A1E),
                        Color(0xFF131D24)
                    )
                )
            )
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        // Subtle Home Screen hint indicators
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HOME SCREEN PREVIEW",
                    color = Color.White.copy(alpha = 0.40f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${(transparency * 100).toInt()}% OPACITY",
                    color = Color.White.copy(alpha = 0.40f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Widget Card with real transparency and chosen theme
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(162.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(theme.colorHex).copy(alpha = transparency)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (layoutMode) {
                        WidgetLayoutMode.SINGLE -> {
                            InteractiveComponentDemo(comp1, isCompact = false)
                        }
                        WidgetLayoutMode.COMBO_HORIZONTAL -> {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                    InteractiveComponentDemo(comp1, isCompact = true)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                    InteractiveComponentDemo(comp2, isCompact = true)
                                }
                            }
                        }
                        WidgetLayoutMode.COMBO_VERTICAL -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    InteractiveComponentDemo(comp1, isCompact = true)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    InteractiveComponentDemo(comp2, isCompact = true)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveComponentDemo(component: WidgetComponent, isCompact: Boolean) {
    val primaryColor = Color(0xFF7EB09B)
    val textLight = Color(0xFFF1F3F0)
    val textMuted = Color(0xFFA6B0A9)

    when (component) {
        WidgetComponent.TIMER -> {
            var isRunning by remember { mutableStateOf(false) }
            var remainingMins by remember { mutableIntStateOf(25) }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "FOCUS • General Study",
                    color = primaryColor,
                    fontSize = if (isCompact) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = String.format("%02d:00", remainingMins),
                    color = textLight,
                    fontSize = if (isCompact) 20.sp else 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isRunning) Color(0xFFD97706) else primaryColor)
                            .clickable { isRunning = !isRunning }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isRunning) "Pause" else "Start",
                            color = Color(0xFF161C19),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                            .clickable { remainingMins += 5 }
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "+5m",
                            color = textLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        WidgetComponent.TASKS -> {
            var task1Done by remember { mutableStateOf(true) }
            var task2Done by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Today's Tasks (${if (task1Done) 1 else 0 + if (task2Done) 1 else 0}/2)",
                    color = primaryColor,
                    fontSize = if (isCompact) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { task1Done = !task1Done }
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = if (task1Done) "[✓] " else "[  ] ",
                        color = if (task1Done) primaryColor else textLight,
                        fontSize = if (isCompact) 10.sp else 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Review Anatomy Ch. 4",
                        color = if (task1Done) textMuted else textLight,
                        fontSize = if (isCompact) 9.sp else 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { task2Done = !task2Done }
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = if (task2Done) "[✓] " else "[  ] ",
                        color = if (task2Done) primaryColor else textLight,
                        fontSize = if (isCompact) 10.sp else 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Solve Physics 9:00 AM",
                        color = if (task2Done) textMuted else textLight,
                        fontSize = if (isCompact) 9.sp else 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        WidgetComponent.COUNTDOWN -> {
            val countdowns = listOf(
                Triple("Final Exam Countdown", "14 DAYS LEFT", "Nov 15 • Medicine"),
                Triple("Physics Midterm", "5 DAYS LEFT", "Nov 6 • Physics"),
                Triple("Organic Chemistry Test", "TODAY!", "Today • Chemistry")
            )
            var index by remember { mutableIntStateOf(0) }
            val current = countdowns[index]

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Milestone",
                        color = primaryColor,
                        fontSize = if (isCompact) 10.sp else 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.14f))
                            .clickable { index = (index + 1) % countdowns.size }
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text("↻", color = textLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = current.second,
                    color = Color(0xFFFFB74D),
                    fontSize = if (isCompact) 18.sp else 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = current.first,
                    color = textLight,
                    fontSize = if (isCompact) 10.sp else 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        WidgetComponent.STREAK -> {
            var minutesLogged by remember { mutableIntStateOf(45) }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Consistency",
                    color = primaryColor,
                    fontSize = if (isCompact) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "7 Days Focus",
                    color = Color(0xFFFF8A65),
                    fontSize = if (isCompact) 18.sp else 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Today: $minutesLogged/120 min",
                    color = textLight.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                        .clickable { minutesLogged += 30 }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("+30m Log", color = primaryColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        WidgetComponent.QUOTE -> {
            var quoteIndex by remember { mutableIntStateOf(0) }
            val quote = QuoteProvider.getQuoteByIndex(quoteIndex)

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "“${quote.text}”",
                    color = textLight,
                    fontSize = if (isCompact) 10.sp else 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = quote.author,
                        color = primaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.14f))
                            .clickable { quoteIndex = (quoteIndex + 1) % QuoteProvider.quotes.size }
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text("↻", color = textLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        WidgetComponent.USAGE -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Screen Time",
                    color = primaryColor,
                    fontSize = if (isCompact) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "2h 45m",
                    color = textLight,
                    fontSize = if (isCompact) 18.sp else 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Balanced Focus",
                    color = textMuted,
                    fontSize = 10.sp
                )
            }
        }

        WidgetComponent.ANALYTICS -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Weekly Analytics",
                    color = primaryColor,
                    fontSize = if (isCompact) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "14.5 hrs",
                    color = textLight,
                    fontSize = if (isCompact) 18.sp else 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "6 sessions done",
                    color = textMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
