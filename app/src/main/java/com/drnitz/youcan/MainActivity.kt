package com.drnitz.youcan

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drnitz.youcan.core.designsystem.YoucanTheme
import com.drnitz.youcan.core.util.PermissionHelper
import com.drnitz.youcan.data.preferences.UserPreferences
import com.drnitz.youcan.data.repository.CountdownRepository
import com.drnitz.youcan.data.repository.PlanRepository
import com.drnitz.youcan.data.repository.ReminderRepository
import com.drnitz.youcan.data.repository.SessionRepository
import com.drnitz.youcan.data.repository.StreakRepository
import com.drnitz.youcan.data.repository.UsageRepository
import com.drnitz.youcan.domain.model.AppUsageInfo
import com.drnitz.youcan.domain.model.StreakData
import com.drnitz.youcan.service.timer.FocusTimerService
import com.drnitz.youcan.ui.analytics.AnalyticsScreen
import com.drnitz.youcan.ui.countdowns.CountdownsScreen
import com.drnitz.youcan.ui.home.HomeScreen
import com.drnitz.youcan.ui.reminders.RemindersScreen
import com.drnitz.youcan.ui.settings.SettingsScreen
import com.drnitz.youcan.ui.streaks.StreaksScreen
import com.drnitz.youcan.ui.tasks.TasksScreen
import com.drnitz.youcan.ui.timer.TimerScreen
import com.drnitz.youcan.ui.usage.UsageScreen
import com.drnitz.youcan.voice.VoiceCommandHandler
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class AppDestination(val title: String, val icon: ImageVector) {
    TIMER("Timer", Icons.Rounded.Timer),
    HOME("Home", Icons.Rounded.Home),
    STREAKS("Streaks", Icons.Rounded.LocalFireDepartment),
    COUNTDOWNS("Countdowns", Icons.Rounded.Event),
    ANALYTICS("Analytics", Icons.Rounded.BarChart),
    TASKS("Tasks", Icons.Rounded.CheckCircle),
    USAGE("Usage", Icons.Rounded.PhoneAndroid),
    WIDGET_STUDIO("Widget Studio", Icons.Rounded.Widgets),
    SETTINGS("Settings", Icons.Rounded.Settings),
    MORE("More", Icons.Rounded.MoreHoriz)
}

class MainActivity : ComponentActivity() {

    private val app by lazy { application as YoucanApplication }
    private val sessionRepo by lazy { SessionRepository(app.database.sessionDao()) }
    private val streakRepo by lazy { StreakRepository(app.database.sessionDao(), app.database.streakFreezeDao()) }
    private val countdownRepo by lazy { CountdownRepository(app.database.countdownDao()) }
    private val reminderRepo by lazy { ReminderRepository(this, app.database.reminderDao()) }
    private val planRepo by lazy { PlanRepository(this, app.database.dailyPlanDao()) }
    private val usageRepo by lazy { UsageRepository(this, app.database.distractingAppDao(), sessionRepo) }

    private var initialDestination = mutableStateOf(AppDestination.TIMER)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        com.drnitz.youcan.core.util.ShortcutHelper.initDynamicShortcuts(this)
        handleIntent(intent)

        setContent {
            val userPrefs by app.preferences.userPreferencesFlow.collectAsStateWithLifecycle(UserPreferences())

            YoucanTheme(amoled = userPrefs.amoledModeEnabled) {
                MainAppScaffold(
                    initialDestination = initialDestination.value,
                    userPreferences = userPrefs,
                    sessionRepo = sessionRepo,
                    streakRepo = streakRepo,
                    countdownRepo = countdownRepo,
                    reminderRepo = reminderRepo,
                    planRepo = planRepo,
                    usageRepo = usageRepo,
                    preferences = app.preferences
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val action = VoiceCommandHandler.parseIntent(intent)
        VoiceCommandHandler.executeAction(this, action)
        when (action) {
            is VoiceCommandHandler.VoiceAction.StartTimer,
            is VoiceCommandHandler.VoiceAction.OpenTimer -> {
                initialDestination.value = AppDestination.TIMER
            }
            is VoiceCommandHandler.VoiceAction.OpenStats -> {
                initialDestination.value = AppDestination.ANALYTICS
            }
            is VoiceCommandHandler.VoiceAction.OpenWidgetStudio -> {
                initialDestination.value = AppDestination.WIDGET_STUDIO
            }
            is VoiceCommandHandler.VoiceAction.AddCountdown,
            is VoiceCommandHandler.VoiceAction.OpenCountdowns -> {
                initialDestination.value = AppDestination.COUNTDOWNS
            }
            is VoiceCommandHandler.VoiceAction.OpenStreaks -> {
                initialDestination.value = AppDestination.STREAKS
            }
            is VoiceCommandHandler.VoiceAction.OpenUsage -> {
                initialDestination.value = AppDestination.USAGE
            }
            is VoiceCommandHandler.VoiceAction.AddReminder,
            is VoiceCommandHandler.VoiceAction.AddTask,
            is VoiceCommandHandler.VoiceAction.OpenTasks -> {
                initialDestination.value = AppDestination.TASKS
            }
            is VoiceCommandHandler.VoiceAction.OpenHome -> {
                initialDestination.value = AppDestination.HOME
            }
            else -> {}
        }
    }
}

@Composable
fun MainAppScaffold(
    initialDestination: AppDestination,
    userPreferences: UserPreferences,
    sessionRepo: SessionRepository,
    streakRepo: StreakRepository,
    countdownRepo: CountdownRepository,
    reminderRepo: ReminderRepository,
    planRepo: PlanRepository,
    usageRepo: UsageRepository,
    preferences: com.drnitz.youcan.data.preferences.AppPreferences
) {
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    val coroutineScope = rememberCoroutineScope()
    var currentDestination by remember { mutableStateOf(initialDestination) }
    var activeMoreSubDestination by remember { mutableStateOf<AppDestination?>(null) }

    LaunchedEffect(initialDestination) {
        if (!isWideScreen && initialDestination in listOf(
                AppDestination.ANALYTICS,
                AppDestination.TASKS,
                AppDestination.USAGE,
                AppDestination.WIDGET_STUDIO,
                AppDestination.SETTINGS
            )
        ) {
            currentDestination = AppDestination.MORE
            activeMoreSubDestination = initialDestination
        } else {
            currentDestination = initialDestination
            activeMoreSubDestination = null
        }
    }

    BackHandler(enabled = activeMoreSubDestination != null || currentDestination != AppDestination.TIMER) {
        if (activeMoreSubDestination != null) {
            activeMoreSubDestination = null
        } else {
            currentDestination = AppDestination.TIMER
        }
    }

    val sessions by sessionRepo.allSessions.collectAsStateWithLifecycle(emptyList())
    val streakData by streakRepo.observeStreakData(userPreferences.dailyStudyGoalMinutes).collectAsStateWithLifecycle(StreakData())
    val countdowns by countdownRepo.allCountdowns.collectAsStateWithLifecycle(emptyList())
    val nearestCountdown by countdownRepo.observeNearestCountdown().collectAsStateWithLifecycle(null)
    val reminders by reminderRepo.allReminders.collectAsStateWithLifecycle(emptyList())
    val todayPlans by planRepo.getPlansForDay(LocalDate.now()).collectAsStateWithLifecycle(emptyList())
    var selectedTasksDate by remember { mutableStateOf(LocalDate.now()) }
    val tasksForSelectedDate by planRepo.getPlansForDay(selectedTasksDate).collectAsStateWithLifecycle(emptyList())

    val context = androidx.compose.ui.platform.LocalContext.current
    var usageList by remember { mutableStateOf<List<AppUsageInfo>>(emptyList()) }
    val hasUsagePermission = remember(currentDestination) { PermissionHelper.hasUsageStatsPermission(context) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionHelper.hasNotificationPermission(context)) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(currentDestination, hasUsagePermission) {
        if (currentDestination == AppDestination.USAGE || activeMoreSubDestination == AppDestination.USAGE) {
            if (hasUsagePermission) {
                usageList = usageRepo.getUsageForDay(LocalDate.now())
            }
        }
    }

    // For wide screens (tablets/foldables/landscape), all primary destinations appear in nav rail.
    // For compact phones, we display the top 4 + More.
    val visibleItems = if (isWideScreen) {
        listOf(
            AppDestination.TIMER,
            AppDestination.HOME,
            AppDestination.STREAKS,
            AppDestination.COUNTDOWNS,
            AppDestination.ANALYTICS,
            AppDestination.TASKS,
            AppDestination.USAGE,
            AppDestination.WIDGET_STUDIO,
            AppDestination.SETTINGS
        )
    } else {
        listOf(
            AppDestination.TIMER,
            AppDestination.HOME,
            AppDestination.STREAKS,
            AppDestination.COUNTDOWNS,
            AppDestination.MORE
        )
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            for (dest in visibleItems) {
                val isSelected = if (isWideScreen) {
                    currentDestination == dest
                } else {
                    if (dest == AppDestination.MORE) {
                        currentDestination in listOf(
                            AppDestination.MORE,
                            AppDestination.ANALYTICS,
                            AppDestination.TASKS,
                            AppDestination.USAGE,
                            AppDestination.WIDGET_STUDIO,
                            AppDestination.SETTINGS
                        )
                    } else {
                        currentDestination == dest && activeMoreSubDestination == null
                    }
                }

                item(
                    selected = isSelected,
                    onClick = {
                        if (!isWideScreen && dest == AppDestination.MORE) {
                            currentDestination = AppDestination.MORE
                            activeMoreSubDestination = null
                        } else {
                            currentDestination = dest
                            activeMoreSubDestination = null
                        }
                    },
                    icon = {
                        Icon(imageVector = dest.icon, contentDescription = dest.title)
                    },
                    label = {
                        Text(dest.title)
                    }
                )
            }
        }
    ) {
        val effectiveDest = if (!isWideScreen && currentDestination in listOf(
                AppDestination.MORE,
                AppDestination.ANALYTICS,
                AppDestination.TASKS,
                AppDestination.USAGE,
                AppDestination.WIDGET_STUDIO,
                AppDestination.SETTINGS
            )
        ) {
            activeMoreSubDestination ?: AppDestination.MORE
        } else {
            currentDestination
        }

        when (effectiveDest) {
            AppDestination.TIMER -> {
                TimerScreen(userPreferences = userPreferences)
            }
            AppDestination.HOME -> {
                AdaptiveScreenWrapper {
                    HomeScreen(
                        nearestCountdown = nearestCountdown,
                        plans = todayPlans,
                        onStartFocus = {
                            currentDestination = AppDestination.TIMER
                            FocusTimerService.startTimer(context)
                        },
                        onNavigateToCountdowns = {
                            currentDestination = AppDestination.COUNTDOWNS
                        },
                        onTogglePlan = { plan ->
                            coroutineScope.launch { planRepo.togglePlanCompletion(plan) }
                        },
                        onDeletePlan = { plan ->
                            coroutineScope.launch { planRepo.deletePlan(plan) }
                        },
                        onAddTask = { task, date, mins, hasRem, remH, remM ->
                            coroutineScope.launch { planRepo.addTask(date, task, mins, hasRem, remH, remM) }
                        },
                        onCopyPlanToTomorrow = {
                            coroutineScope.launch {
                                planRepo.copyPlansToDate(LocalDate.now(), LocalDate.now().plusDays(1))
                            }
                        }
                    )
                }
            }
            AppDestination.STREAKS -> {
                AdaptiveScreenWrapper {
                    StreaksScreen(
                        streakData = streakData,
                        sessions = sessions,
                        onFreezeStreak = { date ->
                            coroutineScope.launch { streakRepo.applyStreakFreeze(date) }
                        },
                        onManualLog = { subject, minutes, note ->
                            coroutineScope.launch {
                                val now = System.currentTimeMillis()
                                sessionRepo.logSession(
                                    startTime = now - (minutes * 60 * 1000L),
                                    endTime = now,
                                    durationSeconds = minutes * 60,
                                    subject = subject,
                                    note = note,
                                    isCompleted = true,
                                    isManual = true
                                )
                            }
                        },
                        onDeleteSession = { session ->
                            coroutineScope.launch { sessionRepo.deleteSession(session) }
                        }
                    )
                }
            }
            AppDestination.COUNTDOWNS -> {
                AdaptiveScreenWrapper {
                    CountdownsScreen(
                        countdowns = countdowns,
                        onAddCountdown = { title, subject, targetMillis ->
                            coroutineScope.launch {
                                countdownRepo.addCountdown(title, targetMillis, subject, listOf(1440, 60))
                            }
                        },
                        onDeleteCountdown = { item ->
                            coroutineScope.launch { countdownRepo.deleteCountdown(item) }
                        }
                    )
                }
            }
            AppDestination.ANALYTICS -> {
                MoreSubScreenContainer(
                    title = "Analytics",
                    onBack = { activeMoreSubDestination = null }
                ) {
                    AnalyticsScreen(
                        sessions = sessions,
                        streakData = streakData
                    )
                }
            }
            AppDestination.TASKS -> {
                MoreSubScreenContainer(
                    title = "Tasks & Reminders",
                    onBack = { activeMoreSubDestination = null }
                ) {
                    TasksScreen(
                        tasks = tasksForSelectedDate,
                        selectedDate = selectedTasksDate,
                        onDateChanged = { selectedTasksDate = it },
                        onAddTask = { task, date, targetMinutes, hasReminder, reminderHour, reminderMinute ->
                            coroutineScope.launch {
                                planRepo.addTask(date, task, targetMinutes, hasReminder, reminderHour, reminderMinute)
                            }
                        },
                        onToggleTask = { task ->
                            coroutineScope.launch { planRepo.togglePlanCompletion(task) }
                        },
                        onDeleteTask = { task ->
                            coroutineScope.launch { planRepo.deleteTask(task) }
                        },
                        onUpdateTaskReminder = { task, hasReminder, hour, minute ->
                            coroutineScope.launch {
                                planRepo.updateTask(task.copy(hasReminder = hasReminder, reminderHour = hour, reminderMinute = minute))
                            }
                        },
                        onCopyTasksToTomorrow = {
                            coroutineScope.launch {
                                planRepo.copyPlansToDate(selectedTasksDate, selectedTasksDate.plusDays(1))
                            }
                        },
                        reminders = reminders,
                        onToggleReminder = { item, isEnabled ->
                            coroutineScope.launch {
                                reminderRepo.updateReminder(item.copy(isEnabled = isEnabled))
                            }
                        },
                        onDeleteReminder = { item ->
                            coroutineScope.launch {
                                reminderRepo.deleteReminder(item)
                            }
                        },
                        onAddReminder = { title, hour, minute, daysOfWeek ->
                            coroutineScope.launch {
                                reminderRepo.addReminder(title, hour, minute, daysOfWeek)
                            }
                        }
                    )
                }
            }
            AppDestination.USAGE -> {
                MoreSubScreenContainer(
                    title = "App Usage",
                    onBack = { activeMoreSubDestination = null }
                ) {
                    UsageScreen(
                        usageList = usageList,
                        hasPermission = hasUsagePermission,
                        onToggleDistracting = { info, isDistracting ->
                            coroutineScope.launch {
                                usageRepo.setDistracting(info.packageName, info.appName, isDistracting)
                                usageList = usageRepo.getUsageForDay(LocalDate.now())
                            }
                        }
                    )
                }
            }
            AppDestination.WIDGET_STUDIO -> {
                MoreSubScreenContainer(
                    title = "Widget Studio",
                    onBack = { activeMoreSubDestination = null }
                ) {
                    com.drnitz.youcan.widget.WidgetStudioScreen(
                        isConfigActivity = false,
                        onBack = { activeMoreSubDestination = null }
                    )
                }
            }
            AppDestination.SETTINGS -> {
                MoreSubScreenContainer(
                    title = "Settings",
                    onBack = { activeMoreSubDestination = null }
                ) {
                    SettingsScreen(
                        userPreferences = userPreferences,
                        onUpdateDurations = { f, sb, lb, c ->
                            coroutineScope.launch { preferences.updateTimerDurations(f, sb, lb, c) }
                        },
                        onUpdateDailyGoal = { g ->
                            coroutineScope.launch { preferences.updateDailyGoal(g) }
                        },
                        onUpdateBackground = { bg, uri, blur, dim ->
                            coroutineScope.launch { preferences.updateBackground(bg, uri, blur, dim) }
                        },
                        onUpdateAlerts = { sound, vib ->
                            coroutineScope.launch { preferences.updateAlerts(sound, vib) }
                        },
                        onSetFloatingDock = { enabled ->
                            coroutineScope.launch { preferences.setFloatingDockEnabled(enabled) }
                        },
                        onSetAmoledMode = { enabled ->
                            coroutineScope.launch { preferences.setAmoledMode(enabled) }
                        },
                        onSetReducedMotion = { enabled ->
                            coroutineScope.launch { preferences.setReducedMotion(enabled) }
                        },
                        onNavigateToWidgetStudio = {
                            activeMoreSubDestination = AppDestination.WIDGET_STUDIO
                        }
                    )
                }
            }
            AppDestination.MORE -> {
                AdaptiveScreenWrapper {
                    MoreHubScreen(
                        onSelect = { sub -> activeMoreSubDestination = sub }
                    )
                }
            }
        }
    }
}

@Composable
fun AdaptiveScreenWrapper(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 840.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = maxWidth)
                .fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
fun MoreHubScreen(
    onSelect: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple(AppDestination.WIDGET_STUDIO, "Design live widgets, themes, transparency, and home screen pinning", Icons.Rounded.Widgets),
        Triple(AppDestination.ANALYTICS, "Weekly trends, distribution, peak study hours, and milestones", Icons.Rounded.BarChart),
        Triple(AppDestination.TASKS, "Scheduled study goals and custom task reminders", Icons.Rounded.CheckCircle),
        Triple(AppDestination.USAGE, "Screen time and distracting apps awareness", Icons.Rounded.PhoneAndroid),
        Triple(AppDestination.SETTINGS, "Timer intervals, ambient background, floating dock, and theme", Icons.Rounded.Settings)
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Tools & Preferences",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Analytics, reminders, usage awareness, and settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        for ((dest, desc, icon) in items) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onSelect(dest) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            Column {
                                Text(text = dest.title, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreSubScreenContainer(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    if (isWideScreen) {
        AdaptiveScreenWrapper {
            content()
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title, style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
            ) {
                AdaptiveScreenWrapper {
                    content()
                }
            }
        }
    }
}
