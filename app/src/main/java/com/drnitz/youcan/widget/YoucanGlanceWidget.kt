package com.drnitz.youcan.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.drnitz.youcan.MainActivity
import com.drnitz.youcan.R
import com.drnitz.youcan.YoucanApplication
import com.drnitz.youcan.data.local.entity.DailyPlanEntity
import com.drnitz.youcan.data.local.entity.ExamCountdownEntity
import com.drnitz.youcan.data.preferences.UserPreferences
import com.drnitz.youcan.data.repository.SessionRepository
import com.drnitz.youcan.data.repository.StreakRepository
import com.drnitz.youcan.data.repository.UsageRepository
import com.drnitz.youcan.domain.model.Quote
import com.drnitz.youcan.domain.model.QuoteProvider
import com.drnitz.youcan.domain.model.StreakData
import com.drnitz.youcan.domain.model.TimerSnapshot
import com.drnitz.youcan.domain.model.TimerStatus
import com.drnitz.youcan.service.timer.FocusTimerService
import com.drnitz.youcan.service.timer.TimerStateManager
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class WidgetComponent(val displayName: String) {
    TIMER("Focus Timer"),
    TASKS("Tasks & Reminders"),
    STREAK("Focus Streak"),
    COUNTDOWN("Nearest Countdown"),
    QUOTE("Motivational Quote"),
    USAGE("Screen Time & Usage"),
    ANALYTICS("Weekly Analytics")
}

typealias WidgetType = WidgetComponent

enum class WidgetLayoutMode(val displayName: String) {
    SINGLE("Single Component"),
    COMBO_HORIZONTAL("Side-by-Side (Split)"),
    COMBO_VERTICAL("Stacked (Top & Bottom)")
}

enum class WidgetBackgroundTheme(val displayName: String, val colorHex: Long, val defaultAlpha: Float = 0.95f) {
    FOREST("Deep Forest (Sage)", 0xFF161C19, 0.95f),
    AMOLED("True Black AMOLED", 0xFF000000, 1.0f),
    NAVY("Midnight Navy", 0xFF0E1626, 0.95f),
    CHARCOAL("Dark Slate", 0xFF1E222A, 0.95f),
    WARM_EARTH("Warm Espresso", 0xFF221C18, 0.95f),
    FROSTED_GLASS("Frosted Glass", 0xFF202A26, 0.70f)
}

open class YoucanGlanceWidget(
    private val defaultComp1: WidgetComponent = WidgetComponent.TIMER,
    private val defaultComp2: WidgetComponent = WidgetComponent.COUNTDOWN,
    private val defaultLayoutMode: WidgetLayoutMode = WidgetLayoutMode.SINGLE,
    private val isPresetWidget: Boolean = false
) : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val sp = context.getSharedPreferences("youcan_widget_prefs", Context.MODE_PRIVATE)
        val glanceKey = id.toString()
        val appWidgetId = try {
            GlanceAppWidgetManager(context).getAppWidgetId(id)
        } catch (_: Exception) { -1 }

        val globalMode = if (!isPresetWidget) sp.getString("widget_layout_mode", null) else null
        val modeName = sp.getString("widget_${appWidgetId}_layout_mode", null)
            ?: sp.getString("widget_${glanceKey}_layout_mode", null)
            ?: globalMode
            ?: defaultLayoutMode.name
        val layoutMode = try { WidgetLayoutMode.valueOf(modeName) } catch (_: Exception) { defaultLayoutMode }

        val globalComp1 = if (!isPresetWidget) (sp.getString("widget_comp1", null) ?: sp.getString("widget_type", null)) else null
        val comp1Name = sp.getString("widget_${appWidgetId}_comp1", null)
            ?: sp.getString("widget_${glanceKey}_comp1", null)
            ?: globalComp1
            ?: defaultComp1.name
        val comp1 = try { WidgetComponent.valueOf(comp1Name) } catch (_: Exception) { defaultComp1 }

        val globalComp2 = if (!isPresetWidget) sp.getString("widget_comp2", null) else null
        val comp2Name = sp.getString("widget_${appWidgetId}_comp2", null)
            ?: sp.getString("widget_${glanceKey}_comp2", null)
            ?: globalComp2
            ?: defaultComp2.name
        val comp2 = try { WidgetComponent.valueOf(comp2Name) } catch (_: Exception) { defaultComp2 }

        val themeName = sp.getString("widget_${appWidgetId}_theme", null)
            ?: sp.getString("widget_${glanceKey}_theme", null)
            ?: sp.getString("widget_theme", null)
            ?: if (sp.getBoolean("widget_amoled", false)) WidgetBackgroundTheme.AMOLED.name else WidgetBackgroundTheme.FOREST.name
        val theme = try { WidgetBackgroundTheme.valueOf(themeName) } catch (_: Exception) { WidgetBackgroundTheme.FOREST }

        val transparency = when {
            sp.contains("widget_${appWidgetId}_transparency") -> sp.getFloat("widget_${appWidgetId}_transparency", theme.defaultAlpha)
            sp.contains("widget_${glanceKey}_transparency") -> sp.getFloat("widget_${glanceKey}_transparency", theme.defaultAlpha)
            sp.contains("widget_transparency") -> sp.getFloat("widget_transparency", theme.defaultAlpha)
            else -> theme.defaultAlpha
        }.coerceIn(0.20f, 1.0f)

        val activeComponents = when (layoutMode) {
            WidgetLayoutMode.SINGLE -> setOf(comp1)
            else -> setOf(comp1, comp2)
        }

        // Load real dynamic data from Room & System selectively based on active components
        val app = context.applicationContext as? YoucanApplication
        val db = app?.database
        val userPrefs = app?.preferences?.userPreferencesFlow?.firstOrNull() ?: UserPreferences()
        val today = LocalDate.now()
        val nowMillis = System.currentTimeMillis()

        // 1. Live Timer Snapshot
        val timerSnapshot = TimerStateManager.getSnapshot(context)

        // 2. Active Countdowns
        val upcomingCountdowns = if (activeComponents.contains(WidgetComponent.COUNTDOWN)) {
            try {
                db?.countdownDao()?.getAllUpcomingCountdownsSync(nowMillis) ?: emptyList()
            } catch (_: Exception) { emptyList() }
        } else emptyList()
        val countdownIndex = sp.getInt("countdown_index_$glanceKey", 0)
        val nearestCountdown = if (upcomingCountdowns.isNotEmpty()) {
            upcomingCountdowns[((countdownIndex % upcomingCountdowns.size) + upcomingCountdowns.size) % upcomingCountdowns.size]
        } else null
        val upcomingCountdownsCount = upcomingCountdowns.size

        // 3. Focus Streak Data
        val streakData = if (activeComponents.contains(WidgetComponent.STREAK) && db != null) {
            try {
                val sessions = db.sessionDao().getAllSessionsSync()
                val freezes = db.streakFreezeDao().getAllFreezesSync()
                StreakRepository(db.sessionDao(), db.streakFreezeDao()).calculateStreak(
                    sessions = sessions,
                    freezes = freezes,
                    dailyGoalMinutes = userPrefs.dailyStudyGoalMinutes
                )
            } catch (_: Exception) { StreakData() }
        } else StreakData()

        // 4. Today's Tasks
        val todayTasks = if (activeComponents.contains(WidgetComponent.TASKS)) {
            try {
                db?.dailyPlanDao()?.getPlansForDaySync(today.toEpochDay()) ?: emptyList()
            } catch (_: Exception) { emptyList() }
        } else emptyList()

        // 5. Quote
        val quote = if (activeComponents.contains(WidgetComponent.QUOTE)) {
            val defaultQuoteIndex = ((today.toEpochDay() % QuoteProvider.quotes.size).toInt() + QuoteProvider.quotes.size) % QuoteProvider.quotes.size
            val quoteIndex = when {
                sp.contains("quote_index_$appWidgetId") -> sp.getInt("quote_index_$appWidgetId", defaultQuoteIndex)
                sp.contains("quote_index_$glanceKey") -> sp.getInt("quote_index_$glanceKey", defaultQuoteIndex)
                sp.contains("quote_index_global") -> sp.getInt("quote_index_global", defaultQuoteIndex)
                else -> defaultQuoteIndex
            }
            QuoteProvider.getQuoteByIndex(quoteIndex)
        } else QuoteProvider.quotes[0]

        // 6. Weekly Analytics
        val (weekTotalMinutes, weekCompletedCount) = if (activeComponents.contains(WidgetComponent.ANALYTICS)) {
            val zone = ZoneId.systemDefault()
            val startOfWeekMillis = today.minusDays(today.dayOfWeek.value.toLong() - 1).atStartOfDay(zone).toInstant().toEpochMilli()
            val weekSessions = try {
                db?.sessionDao()?.getSessionsBetweenSync(startOfWeekMillis, nowMillis) ?: emptyList()
            } catch (_: Exception) { emptyList() }
            (weekSessions.sumOf { it.durationSeconds } / 60) to weekSessions.count { it.isCompleted }
        } else (0 to 0)

        // 7. Usage Info
        val usageSummary = if (activeComponents.contains(WidgetComponent.USAGE) && db != null) {
            try {
                val usageList = UsageRepository(context, db.distractingAppDao(), SessionRepository(db.sessionDao())).getUsageForDay(today)
                val totalMins = usageList.sumOf { it.totalTimeMillis } / 60_000
                val topDistracting = usageList.firstOrNull { it.isDistracting }?.appName ?: ""
                totalMins to topDistracting
            } catch (_: Exception) { 0L to "" }
        } else (0L to "")

        provideContent {
            val size = LocalSize.current
            val isWide = size.width >= 250.dp
            val isTall = size.height >= 150.dp

            val baseColor = Color(theme.colorHex)
            val bgColor = baseColor.copy(alpha = transparency)
            val primarySage = Color(0xFF7EB09B)
            val textLight = Color(0xFFF1F3F0)
            val textMuted = Color(0xFFA6B0A9)

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(20.dp)
                    .background(bgColor)
                    .padding(12.dp)
            ) {
                when (layoutMode) {
                    WidgetLayoutMode.SINGLE -> {
                        RenderComponent(
                            component = comp1,
                            glanceKey = glanceKey,
                            timerSnapshot = timerSnapshot,
                            nearestCountdown = nearestCountdown,
                            upcomingCountdownsCount = upcomingCountdownsCount,
                            streakData = streakData,
                            todayTasks = todayTasks,
                            quote = quote,
                            weekTotalMinutes = weekTotalMinutes,
                            weekCompletedCount = weekCompletedCount,
                            usageTotalMins = usageSummary.first,
                            topDistractingApp = usageSummary.second,
                            primary = primarySage,
                            textLight = textLight,
                            textMuted = textMuted,
                            isCompact = false,
                            isWide = isWide,
                            isTall = isTall
                        )
                    }
                    WidgetLayoutMode.COMBO_HORIZONTAL -> {
                        Row(
                            modifier = GlanceModifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = GlanceModifier.defaultWeight().fillMaxHeight()) {
                                RenderComponent(
                                    component = comp1,
                                    glanceKey = glanceKey,
                                    timerSnapshot = timerSnapshot,
                                    nearestCountdown = nearestCountdown,
                                    upcomingCountdownsCount = upcomingCountdownsCount,
                                    streakData = streakData,
                                    todayTasks = todayTasks,
                                    quote = quote,
                                    weekTotalMinutes = weekTotalMinutes,
                                    weekCompletedCount = weekCompletedCount,
                                    usageTotalMins = usageSummary.first,
                                    topDistractingApp = usageSummary.second,
                                    primary = primarySage,
                                    textLight = textLight,
                                    textMuted = textMuted,
                                    isCompact = true,
                                    isWide = false,
                                    isTall = isTall
                                )
                            }
                            Spacer(modifier = GlanceModifier.width(10.dp))
                            Box(modifier = GlanceModifier.defaultWeight().fillMaxHeight()) {
                                RenderComponent(
                                    component = comp2,
                                    glanceKey = glanceKey,
                                    timerSnapshot = timerSnapshot,
                                    nearestCountdown = nearestCountdown,
                                    upcomingCountdownsCount = upcomingCountdownsCount,
                                    streakData = streakData,
                                    todayTasks = todayTasks,
                                    quote = quote,
                                    weekTotalMinutes = weekTotalMinutes,
                                    weekCompletedCount = weekCompletedCount,
                                    usageTotalMins = usageSummary.first,
                                    topDistractingApp = usageSummary.second,
                                    primary = primarySage,
                                    textLight = textLight,
                                    textMuted = textMuted,
                                    isCompact = true,
                                    isWide = false,
                                    isTall = isTall
                                )
                            }
                        }
                    }
                    WidgetLayoutMode.COMBO_VERTICAL -> {
                        Column(
                            modifier = GlanceModifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(modifier = GlanceModifier.defaultWeight().fillMaxWidth()) {
                                RenderComponent(
                                    component = comp1,
                                    glanceKey = glanceKey,
                                    timerSnapshot = timerSnapshot,
                                    nearestCountdown = nearestCountdown,
                                    upcomingCountdownsCount = upcomingCountdownsCount,
                                    streakData = streakData,
                                    todayTasks = todayTasks,
                                    quote = quote,
                                    weekTotalMinutes = weekTotalMinutes,
                                    weekCompletedCount = weekCompletedCount,
                                    usageTotalMins = usageSummary.first,
                                    topDistractingApp = usageSummary.second,
                                    primary = primarySage,
                                    textLight = textLight,
                                    textMuted = textMuted,
                                    isCompact = true,
                                    isWide = isWide,
                                    isTall = false
                                )
                            }
                            Spacer(modifier = GlanceModifier.height(8.dp))
                            Box(modifier = GlanceModifier.defaultWeight().fillMaxWidth()) {
                                RenderComponent(
                                    component = comp2,
                                    glanceKey = glanceKey,
                                    timerSnapshot = timerSnapshot,
                                    nearestCountdown = nearestCountdown,
                                    upcomingCountdownsCount = upcomingCountdownsCount,
                                    streakData = streakData,
                                    todayTasks = todayTasks,
                                    quote = quote,
                                    weekTotalMinutes = weekTotalMinutes,
                                    weekCompletedCount = weekCompletedCount,
                                    usageTotalMins = usageSummary.first,
                                    topDistractingApp = usageSummary.second,
                                    primary = primarySage,
                                    textLight = textLight,
                                    textMuted = textMuted,
                                    isCompact = true,
                                    isWide = isWide,
                                    isTall = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RenderComponent(
        component: WidgetComponent,
        glanceKey: String,
        timerSnapshot: TimerSnapshot,
        nearestCountdown: ExamCountdownEntity?,
        upcomingCountdownsCount: Int = 0,
        streakData: StreakData,
        todayTasks: List<DailyPlanEntity>,
        quote: Quote,
        weekTotalMinutes: Int,
        weekCompletedCount: Int,
        usageTotalMins: Long,
        topDistractingApp: String,
        primary: Color,
        textLight: Color,
        textMuted: Color,
        isCompact: Boolean,
        isWide: Boolean = false,
        isTall: Boolean = false
    ) {
        when (component) {
            WidgetComponent.TIMER -> TimerWidgetContent(
                snapshot = timerSnapshot,
                primary = primary,
                textLight = textLight,
                textMuted = textMuted,
                isCompact = isCompact,
                isWide = isWide
            )
            WidgetComponent.TASKS -> TasksWidgetContent(
                tasks = todayTasks,
                primary = primary,
                textLight = textLight,
                textMuted = textMuted,
                isCompact = isCompact,
                isWide = isWide,
                isTall = isTall
            )
            WidgetComponent.COUNTDOWN -> CountdownWidgetContent(
                countdown = nearestCountdown,
                glanceKey = glanceKey,
                upcomingCount = upcomingCountdownsCount,
                primary = primary,
                textLight = textLight,
                textMuted = textMuted,
                isCompact = isCompact,
                isWide = isWide
            )
            WidgetComponent.STREAK -> StreakWidgetContent(
                streakData = streakData,
                primary = primary,
                textLight = textLight,
                textMuted = textMuted,
                isCompact = isCompact,
                isWide = isWide
            )
            WidgetComponent.QUOTE -> QuoteWidgetContent(
                quote = quote,
                glanceKey = glanceKey,
                primary = primary,
                textLight = textLight,
                textMuted = textMuted,
                isCompact = isCompact
            )
            WidgetComponent.USAGE -> UsageWidgetContent(
                totalMins = usageTotalMins,
                topDistractingApp = topDistractingApp,
                primary = primary,
                textLight = textLight,
                textMuted = textMuted,
                isCompact = isCompact,
                isWide = isWide
            )
            WidgetComponent.ANALYTICS -> AnalyticsWidgetContent(
                weekTotalMinutes = weekTotalMinutes,
                weekCompletedCount = weekCompletedCount,
                primary = primary,
                textLight = textLight,
                textMuted = textMuted,
                isCompact = isCompact,
                isWide = isWide
            )
        }
    }

    @Composable
    private fun TimerWidgetContent(
        snapshot: TimerSnapshot,
        primary: Color,
        textLight: Color,
        textMuted: Color,
        isCompact: Boolean,
        isWide: Boolean = false
    ) {
        val mins = snapshot.remainingSeconds / 60
        val secs = snapshot.remainingSeconds % 60
        val timeStr = String.format("%02d:%02d", mins, secs)

        val statusText = when (snapshot.status) {
            TimerStatus.RUNNING -> "In Focus"
            TimerStatus.PAUSED -> "Paused"
            else -> "Ready"
        }

        if (isWide && !isCompact) {
            // Responsive Wide 4x2 / 3x2 layout
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight()
                        .clickable(openAppAction("youcan://timer")),
                    horizontalAlignment = Alignment.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${snapshot.phase.displayName} • ${snapshot.subject}",
                        style = TextStyle(
                            color = ColorProvider(primary),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    LiveTimerDisplay(
                        snapshot = snapshot,
                        fontSizeSp = 36f,
                        textColor = textLight
                    )
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = "$statusText • Cycle ${snapshot.currentCycle}/${snapshot.totalCycles}",
                        style = TextStyle(
                            color = ColorProvider(textMuted),
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }

                Spacer(modifier = GlanceModifier.width(16.dp))

                Column(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimerActionButtons(
                        snapshot = snapshot,
                        primary = primary,
                        isCompact = false
                    )
                }
            }
        } else {
            // Standard / Compact layout
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(openAppAction("youcan://timer")),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${snapshot.phase.displayName} • ${snapshot.subject}",
                        style = TextStyle(
                            color = ColorProvider(primary),
                            fontSize = if (isCompact) 11.sp else 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )

                    Spacer(modifier = GlanceModifier.height(2.dp))

                    LiveTimerDisplay(
                        snapshot = snapshot,
                        fontSizeSp = if (isCompact) 24f else 32f,
                        textColor = textLight
                    )

                    Spacer(modifier = GlanceModifier.height(2.dp))

                    Text(
                        text = "$statusText (Cycle ${snapshot.currentCycle}/${snapshot.totalCycles})",
                        style = TextStyle(
                            color = ColorProvider(textMuted),
                            fontSize = if (isCompact) 10.sp else 11.sp
                        ),
                        maxLines = 1
                    )
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                TimerActionButtons(
                    snapshot = snapshot,
                    primary = primary,
                    isCompact = isCompact
                )
            }
        }
    }

    @Composable
    private fun LiveTimerDisplay(
        snapshot: TimerSnapshot,
        fontSizeSp: Float,
        textColor: Color
    ) {
        val context = LocalContext.current
        val rv = RemoteViews(context.packageName, R.layout.widget_live_timer)
        val textColorInt = textColor.toArgb()

        val mins = snapshot.remainingSeconds / 60
        val secs = snapshot.remainingSeconds % 60
        val timeStr = if (mins >= 60) {
            String.format("%02d:%02d:%02d", mins / 60, mins % 60, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("youcan://timer")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        rv.setOnClickPendingIntent(R.id.widget_chronometer, pi)
        rv.setOnClickPendingIntent(R.id.widget_timer_text, pi)

        val nowRealtime = SystemClock.elapsedRealtime()
        val hasValidTarget = snapshot.targetEndRealtime > nowRealtime
        val isRunning = snapshot.status == TimerStatus.RUNNING && (hasValidTarget || (snapshot.targetEndRealtime == 0L && snapshot.remainingSeconds > 0))

        if (isRunning) {
            val baseTime = if (hasValidTarget) {
                snapshot.targetEndRealtime
            } else {
                nowRealtime + (snapshot.remainingSeconds * 1000L)
            }
            rv.setViewVisibility(R.id.widget_chronometer, View.VISIBLE)
            rv.setViewVisibility(R.id.widget_timer_text, View.GONE)
            rv.setChronometerCountDown(R.id.widget_chronometer, true)
            rv.setChronometer(R.id.widget_chronometer, baseTime, null, true)
            rv.setTextViewTextSize(R.id.widget_chronometer, TypedValue.COMPLEX_UNIT_SP, fontSizeSp)
            rv.setTextColor(R.id.widget_chronometer, textColorInt)
        } else {
            rv.setViewVisibility(R.id.widget_chronometer, View.GONE)
            rv.setViewVisibility(R.id.widget_timer_text, View.VISIBLE)
            rv.setChronometer(R.id.widget_chronometer, 0L, null, false)
            rv.setTextViewText(R.id.widget_timer_text, timeStr)
            rv.setTextViewTextSize(R.id.widget_timer_text, TypedValue.COMPLEX_UNIT_SP, fontSizeSp)
            rv.setTextColor(R.id.widget_timer_text, textColorInt)
        }

        AndroidRemoteViews(remoteViews = rv, modifier = GlanceModifier)
    }

    @Composable
    private fun TimerActionButtons(
        snapshot: TimerSnapshot,
        primary: Color,
        isCompact: Boolean
    ) {
        Row(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (snapshot.status) {
                TimerStatus.RUNNING -> {
                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(10.dp)
                            .background(Color(0xFFE0A96D))
                            .clickable(actionRunCallback<TimerPauseActionCallback>())
                            .padding(horizontal = if (isCompact) 10.dp else 14.dp, vertical = if (isCompact) 8.dp else 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pause",
                            style = TextStyle(
                                color = ColorProvider(Color.Black),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(6.dp))

                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(10.dp)
                            .background(Color.White.copy(alpha = 0.16f))
                            .clickable(actionRunCallback<TimerAddMinutesActionCallback>())
                            .padding(horizontal = if (isCompact) 8.dp else 12.dp, vertical = if (isCompact) 8.dp else 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+5m",
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(6.dp))

                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(10.dp)
                            .background(Color(0xFFE57373))
                            .clickable(actionRunCallback<TimerStopActionCallback>())
                            .padding(horizontal = if (isCompact) 10.dp else 14.dp, vertical = if (isCompact) 8.dp else 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Stop",
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                TimerStatus.PAUSED -> {
                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(10.dp)
                            .background(primary)
                            .clickable(actionRunCallback<TimerResumeActionCallback>())
                            .padding(horizontal = if (isCompact) 10.dp else 14.dp, vertical = if (isCompact) 8.dp else 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Resume",
                            style = TextStyle(
                                color = ColorProvider(Color.Black),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(6.dp))

                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(10.dp)
                            .background(Color.White.copy(alpha = 0.16f))
                            .clickable(actionRunCallback<TimerAddMinutesActionCallback>())
                            .padding(horizontal = if (isCompact) 8.dp else 12.dp, vertical = if (isCompact) 8.dp else 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+5m",
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(6.dp))

                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(10.dp)
                            .background(Color(0xFFE57373))
                            .clickable(actionRunCallback<TimerStopActionCallback>())
                            .padding(horizontal = if (isCompact) 10.dp else 14.dp, vertical = if (isCompact) 8.dp else 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Stop",
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(12.dp)
                            .background(primary)
                            .clickable(actionRunCallback<TimerStartActionCallback>())
                            .padding(horizontal = if (isCompact) 12.dp else 18.dp, vertical = if (isCompact) 9.dp else 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "▶ Start",
                            style = TextStyle(
                                color = ColorProvider(Color.Black),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(12.dp)
                            .background(Color.White.copy(alpha = 0.16f))
                            .clickable(actionRunCallback<TimerAddMinutesActionCallback>())
                            .padding(horizontal = if (isCompact) 10.dp else 14.dp, vertical = if (isCompact) 9.dp else 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+5m",
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun TasksWidgetContent(
        tasks: List<DailyPlanEntity>,
        primary: Color,
        textLight: Color,
        textMuted: Color,
        isCompact: Boolean,
        isWide: Boolean = false,
        isTall: Boolean = false
    ) {
        val completedCount = tasks.count { it.isCompleted }
        val totalCount = tasks.size
        val maxItemsToShow = when {
            isCompact -> 2
            isWide && isTall -> 5
            isTall -> 4
            isWide -> 4
            else -> 3
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(openAppAction("youcan://tasks")),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Tasks",
                    style = TextStyle(
                        color = ColorProvider(primary),
                        fontSize = if (isCompact) 11.sp else 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = "$completedCount/$totalCount",
                    style = TextStyle(color = ColorProvider(textMuted), fontSize = 11.sp)
                )
                if (isWide && !isCompact) {
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(6.dp)
                            .background(Color.White.copy(alpha = 0.12f))
                            .clickable(openAppAction("youcan://tasks/add"))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+ Add",
                            style = TextStyle(color = ColorProvider(primary), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            if (tasks.isEmpty()) {
                Text(
                    text = "No study tasks scheduled for today.",
                    style = TextStyle(color = ColorProvider(textMuted), fontSize = 11.sp)
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Box(
                    modifier = GlanceModifier
                        .cornerRadius(8.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable(openAppAction("youcan://tasks/add"))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+ Add Task with Reminder",
                        style = TextStyle(color = ColorProvider(primary), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                }
            } else {
                for (task in tasks.take(maxItemsToShow)) {
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable(
                                actionRunCallback<ToggleTaskActionCallback>(
                                    actionParametersOf(TaskIdActionKey to task.id)
                                )
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (task.isCompleted) "[✓] " else "[ ] ",
                            style = TextStyle(
                                color = ColorProvider(if (task.isCompleted) primary else textMuted),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = task.task,
                            style = TextStyle(
                                color = ColorProvider(if (task.isCompleted) textMuted else textLight),
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                        if (task.hasReminder && task.reminderHour != null && task.reminderMinute != null) {
                            Spacer(modifier = GlanceModifier.width(4.dp))
                            val amPm = if (task.reminderHour >= 12) "PM" else "AM"
                            val h = when {
                                task.reminderHour == 0 -> 12
                                task.reminderHour > 12 -> task.reminderHour - 12
                                else -> task.reminderHour
                            }
                            Text(
                                text = "⏰$h:${String.format("%02d", task.reminderMinute)}$amPm",
                                style = TextStyle(color = ColorProvider(primary), fontSize = 9.sp)
                            )
                        }
                        if (isWide && task.targetMinutes > 0) {
                            Spacer(modifier = GlanceModifier.width(4.dp))
                            Text(
                                text = "(${task.targetMinutes}m)",
                                style = TextStyle(color = ColorProvider(textMuted), fontSize = 9.sp)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun CountdownWidgetContent(
        countdown: ExamCountdownEntity?,
        glanceKey: String,
        upcomingCount: Int,
        primary: Color,
        textLight: Color,
        textMuted: Color,
        isCompact: Boolean,
        isWide: Boolean = false
    ) {
        if (isWide && !isCompact && countdown != null) {
            val now = System.currentTimeMillis()
            val diffDays = ((countdown.targetEpochMillis - now) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
            val badgeText = when {
                diffDays == 0L -> "TODAY!"
                diffDays == 1L -> "TOMORROW!"
                else -> "$diffDays DAYS"
            }
            val dateStr = Instant.ofEpochMilli(countdown.targetEpochMillis)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("MMM d"))

            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(openAppAction("youcan://countdown")),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = badgeText,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFFFB74D)),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "REMAINING",
                        style = TextStyle(color = ColorProvider(textMuted), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = GlanceModifier.width(12.dp))

                Column(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    horizontalAlignment = Alignment.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Upcoming Milestone",
                            style = TextStyle(color = ColorProvider(primary), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        )
                        if (upcomingCount > 1) {
                            Spacer(modifier = GlanceModifier.defaultWeight())
                            Box(
                                modifier = GlanceModifier
                                    .cornerRadius(6.dp)
                                    .background(Color.White.copy(alpha = 0.14f))
                                    .clickable(
                                        actionRunCallback<CycleCountdownActionCallback>(
                                            actionParametersOf(GlanceKeyActionKey to glanceKey)
                                        )
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Next ↻",
                                    style = TextStyle(color = ColorProvider(textLight), fontSize = 9.sp, fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = countdown.title,
                        style = TextStyle(color = ColorProvider(textLight), fontSize = 13.sp, fontWeight = FontWeight.Medium),
                        maxLines = 1
                    )
                    Text(
                        text = "$dateStr • ${countdown.subject}",
                        style = TextStyle(color = ColorProvider(textMuted), fontSize = 10.sp),
                        maxLines = 1
                    )
                }
            }
        } else {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(openAppAction("youcan://countdown")),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Upcoming Milestone",
                        style = TextStyle(
                            color = ColorProvider(primary),
                            fontSize = if (isCompact) 11.sp else 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (upcomingCount > 1) {
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Box(
                            modifier = GlanceModifier
                                .cornerRadius(6.dp)
                                .background(Color.White.copy(alpha = 0.14f))
                                .clickable(
                                    actionRunCallback<CycleCountdownActionCallback>(
                                        actionParametersOf(GlanceKeyActionKey to glanceKey)
                                    )
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "↻",
                                style = TextStyle(color = ColorProvider(textLight), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                if (countdown == null) {
                    Text(
                        text = "No upcoming deadlines",
                        style = TextStyle(color = ColorProvider(textLight), fontSize = 13.sp)
                    )
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = "Tap to add exam countdown",
                        style = TextStyle(color = ColorProvider(textMuted), fontSize = 10.sp)
                    )
                } else {
                    val now = System.currentTimeMillis()
                    val diffDays = ((countdown.targetEpochMillis - now) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)

                    val badgeText = when {
                        diffDays == 0L -> "TODAY!"
                        diffDays == 1L -> "TOMORROW!"
                        else -> "$diffDays DAYS"
                    }

                    Text(
                        text = badgeText,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFFFB74D)),
                            fontSize = if (isCompact) 20.sp else 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Text(
                        text = countdown.title,
                        style = TextStyle(
                            color = ColorProvider(textLight),
                            fontSize = if (isCompact) 11.sp else 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1
                    )

                    val dateStr = Instant.ofEpochMilli(countdown.targetEpochMillis)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("MMM d"))

                    Text(
                        text = "$dateStr • ${countdown.subject}",
                        style = TextStyle(color = ColorProvider(textMuted), fontSize = 10.sp),
                        maxLines = 1
                    )
                }
            }
        }
    }

    @Composable
    private fun StreakWidgetContent(
        streakData: StreakData,
        primary: Color,
        textLight: Color,
        textMuted: Color,
        isCompact: Boolean,
        isWide: Boolean = false
    ) {
        if (isWide && !isCompact) {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(openAppAction("youcan://streaks")),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${streakData.currentStreak}",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFFF8A65)),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "DAY STREAK",
                        style = TextStyle(color = ColorProvider(textMuted), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = GlanceModifier.width(12.dp))

                Column(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    horizontalAlignment = Alignment.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Consistency",
                        style = TextStyle(color = ColorProvider(primary), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = "Today: ${streakData.todayMinutes}/${streakData.dailyGoalMinutes} min",
                        style = TextStyle(
                            color = ColorProvider(if (streakData.isGoalMetToday) primary else textLight),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = if (streakData.isGoalMetToday) "Daily Goal Achieved" else "Best: ${streakData.longestStreak} days",
                        style = TextStyle(color = ColorProvider(textMuted), fontSize = 10.sp)
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(8.dp)
                            .background(Color.White.copy(alpha = 0.14f))
                            .clickable(actionRunCallback<QuickLogStreakActionCallback>())
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+30m Log",
                            style = TextStyle(color = ColorProvider(primary), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(openAppAction("youcan://streaks")),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Consistency Streak",
                    style = TextStyle(
                        color = ColorProvider(primary),
                        fontSize = if (isCompact) 11.sp else 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.height(2.dp))

                Text(
                    text = "${streakData.currentStreak} Days",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFFF8A65)),
                        fontSize = if (isCompact) 22.sp else 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "Today: ${streakData.todayMinutes}/${streakData.dailyGoalMinutes} min",
                    style = TextStyle(
                        color = ColorProvider(if (streakData.isGoalMetToday) primary else textLight),
                        fontSize = if (isCompact) 10.sp else 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Text(
                    text = if (streakData.isGoalMetToday) "Daily Goal Achieved" else "Best: ${streakData.longestStreak} days",
                    style = TextStyle(color = ColorProvider(textMuted), fontSize = 10.sp)
                )

                Spacer(modifier = GlanceModifier.height(4.dp))
                Box(
                    modifier = GlanceModifier
                        .cornerRadius(8.dp)
                        .background(Color.White.copy(alpha = 0.14f))
                        .clickable(actionRunCallback<QuickLogStreakActionCallback>())
                        .padding(horizontal = if (isCompact) 8.dp else 12.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+30m Log",
                        style = TextStyle(color = ColorProvider(primary), fontSize = if (isCompact) 9.sp else 10.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    @Composable
    private fun QuoteWidgetContent(
        quote: Quote,
        glanceKey: String,
        primary: Color,
        textLight: Color,
        textMuted: Color,
        isCompact: Boolean
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(openAppAction("youcan://home")),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "“${quote.text}”",
                    style = TextStyle(
                        color = ColorProvider(textLight),
                        fontSize = if (isCompact) 11.sp else 12.sp
                    ),
                    maxLines = if (isCompact) 3 else 4
                )
            }

            Spacer(modifier = GlanceModifier.height(3.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quote.author,
                    style = TextStyle(
                        color = ColorProvider(primary),
                        fontSize = if (isCompact) 9.sp else 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    modifier = GlanceModifier.clickable(openAppAction("youcan://home"))
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Box(
                    modifier = GlanceModifier
                        .cornerRadius(8.dp)
                        .background(Color.White.copy(alpha = 0.16f))
                        .clickable(
                            actionRunCallback<CycleQuoteActionCallback>(
                                actionParametersOf(GlanceKeyActionKey to glanceKey)
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Next ↻",
                        style = TextStyle(
                            color = ColorProvider(textLight),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }

    @Composable
    private fun UsageWidgetContent(
        totalMins: Long,
        topDistractingApp: String,
        primary: Color,
        textLight: Color,
        textMuted: Color,
        isCompact: Boolean,
        isWide: Boolean = false
    ) {
        val hours = totalMins / 60
        val mins = totalMins % 60
        val screenTimeStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(openAppAction("youcan://usage")),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Screen Time Today",
                style = TextStyle(
                    color = ColorProvider(primary),
                    fontSize = if (isCompact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(2.dp))

            Text(
                text = screenTimeStr,
                style = TextStyle(
                    color = ColorProvider(textLight),
                    fontSize = if (isCompact) 20.sp else 26.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = if (topDistractingApp.isNotBlank()) "Top: $topDistractingApp" else "Clean study environment",
                style = TextStyle(color = ColorProvider(textMuted), fontSize = 10.sp),
                maxLines = 1
            )
        }
    }

    @Composable
    private fun AnalyticsWidgetContent(
        weekTotalMinutes: Int,
        weekCompletedCount: Int,
        primary: Color,
        textLight: Color,
        textMuted: Color,
        isCompact: Boolean,
        isWide: Boolean = false
    ) {
        val weekHours = String.format("%.1f", weekTotalMinutes / 60.0)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(openAppAction("youcan://analytics")),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Weekly Analytics",
                style = TextStyle(
                    color = ColorProvider(primary),
                    fontSize = if (isCompact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(2.dp))

            Text(
                text = "$weekHours hrs",
                style = TextStyle(
                    color = ColorProvider(textLight),
                    fontSize = if (isCompact) 20.sp else 26.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = "$weekCompletedCount focus sessions completed",
                style = TextStyle(color = ColorProvider(textMuted), fontSize = 10.sp),
                maxLines = 1
            )
        }
    }

    private fun openAppAction(uriString: String): androidx.glance.action.Action {
        return actionRunCallback<OpenDeepLinkActionCallback>(
            actionParametersOf(UriActionKey to uriString)
        )
    }
}

// Specialized Preset Widget Implementations
class TimerGlanceWidget : YoucanGlanceWidget(
    defaultComp1 = WidgetComponent.TIMER,
    defaultLayoutMode = WidgetLayoutMode.SINGLE,
    isPresetWidget = true
)

class TasksGlanceWidget : YoucanGlanceWidget(
    defaultComp1 = WidgetComponent.TASKS,
    defaultLayoutMode = WidgetLayoutMode.SINGLE,
    isPresetWidget = true
)

class CountdownGlanceWidget : YoucanGlanceWidget(
    defaultComp1 = WidgetComponent.COUNTDOWN,
    defaultLayoutMode = WidgetLayoutMode.SINGLE,
    isPresetWidget = true
)

class StreakGlanceWidget : YoucanGlanceWidget(
    defaultComp1 = WidgetComponent.STREAK,
    defaultLayoutMode = WidgetLayoutMode.SINGLE,
    isPresetWidget = true
)

class DashboardGlanceWidget : YoucanGlanceWidget(
    defaultComp1 = WidgetComponent.TIMER,
    defaultComp2 = WidgetComponent.TASKS,
    defaultLayoutMode = WidgetLayoutMode.COMBO_HORIZONTAL,
    isPresetWidget = true
)

class AnalyticsGlanceWidget : YoucanGlanceWidget(
    defaultComp1 = WidgetComponent.ANALYTICS,
    defaultLayoutMode = WidgetLayoutMode.SINGLE,
    isPresetWidget = true
)

class QuoteGlanceWidget : YoucanGlanceWidget(
    defaultComp1 = WidgetComponent.QUOTE,
    defaultLayoutMode = WidgetLayoutMode.SINGLE,
    isPresetWidget = true
)

class UsageGlanceWidget : YoucanGlanceWidget(
    defaultComp1 = WidgetComponent.USAGE,
    defaultLayoutMode = WidgetLayoutMode.SINGLE,
    isPresetWidget = true
)

// Specialized Receivers
class TimerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TimerGlanceWidget()
}

class TasksWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TasksGlanceWidget()
}

class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownGlanceWidget()
}

class StreakWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StreakGlanceWidget()
}

class DashboardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DashboardGlanceWidget()
}

class AnalyticsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AnalyticsGlanceWidget()
}

class QuoteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuoteGlanceWidget()
}

class UsageWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = UsageGlanceWidget()
}
