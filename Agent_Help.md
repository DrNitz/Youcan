# Youcan Study & Focus App — Agent & Developer Guide

## Architecture Overview

**Youcan** is an offline-first, private study companion built entirely with Kotlin, Jetpack Compose, Material 3, Room, DataStore, and Jetpack Glance.

### Principles Followed
1. **Offline-First & Zero Tracking**: No accounts, no network calls, no analytics beacons. All user data lives exclusively in on-device SQLite (`Room`) and `DataStore`.
2. **Adaptive Material 3 Navigation Suite**: Renders a bottom navigation bar on compact phones and transitions automatically to a navigation rail on wide screens (tablets, foldables, and landscape mode).
3. **Low Line Counts & Modular Files**: Each screen is split into small, focused composables (<150 lines per file).
4. **Anti-AI-Slop Visual Design**: Calm organic palette (Deep Forest `#111513`, Sage `#7EB09B`, Warm Honey `#E2B774`, Paper surfaces), true black AMOLED mode, custom wallpapers with WCAG AA scrims, and handcrafted Canvas charts (60fps without heavy chart dependencies).
5. **Battery Efficiency**: Canvas animations respect OS Power Saver and `animator_duration_scale` (reduced motion).
6. **Robust Background Service**: Foreground service with `specialUse` type, wake locks, audio/vibration phase cues, and state restoration.

---

## Directory & Package Map

```
app/src/main/java/com/drnitz/youcan/
├── YoucanApplication.kt           # App setup, notification channels (timer, alerts, reminders, overlay)
├── MainActivity.kt                # Adaptive NavigationSuiteScaffold, deep link routing
├── core/
│   ├── designsystem/              # Color, Type, Shape, Theme (Dark, Light, AMOLED)
│   ├── backgrounds/               # Canvas background engines (Aurora, Waves, Rain, Stars, Custom)
│   ├── sound/                     # SoundEffectManager (audio tones and vibration waveform)
│   └── util/                      # PermissionHelper (usage stats, overlay, exact alarm)
├── data/
│   ├── local/                     # Room database, entities, and DAOs
│   ├── preferences/               # DataStore AppPreferences (timer intervals, wallpaper, toggles)
│   └── repository/                # Session, Streak, Countdown, Reminder, Plan, and Usage repositories
├── domain/
│   └── model/                     # TimerSnapshot, StreakData, CountdownItem, ReminderItem, PlanItem
├── receiver/                      # ExactAlarmReceiver, BootCompletedReceiver
├── service/
│   ├── timer/                     # FocusTimerService, TimerStateManager
│   └── overlay/                   # FloatingOverlayService (draggable floating dock)
├── voice/                         # VoiceCommandHandler (deep link / shortcut parser)
├── widget/                        # YoucanGlanceWidget, WidgetConfigActivity, Receiver
└── ui/
    ├── timer/                     # TimerScreen, TimerRing, TimerControls, SubjectTagPicker
    ├── home/                      # HomeScreen, NearestCountdownCard, MotivationalQuoteCard, TodayPlan
    ├── streaks/                   # StreaksScreen, StreakHeroCard, StudyHeatmapView, ShareCard, LogDialog
    ├── countdowns/                # CountdownsScreen, CountdownCard, AddCountdownDialog
    ├── reminders/                 # RemindersScreen, ReminderCard, AddReminderDialog
    ├── usage/                     # UsageScreen, UsageComponents
    ├── analytics/                 # AnalyticsScreen, WeeklyTrendBarChart, SubjectDistribution, PeakHours
    └── settings/                  # SettingsScreen
```

---

## Key Subsystems

### 1. Foreground Timer Service (`FocusTimerService`)
- Runs as a persistent foreground service with an ongoing notification.
- Supports pause, resume, stop, and skip actions directly from notification buttons.
- State broadcasted via `TimerStateManager.snapshot` StateFlow.
- Phase end cues: Dual alert (audible alarm tone + gentle haptic vibration waveform).
- Automatically records sessions to `FocusSessionEntity` upon completion or stoppage (>60s).

### 2. Draggable Floating Dock (`FloatingOverlayService`)
- Uses `SYSTEM_ALERT_WINDOW` with runtime permission check.
- Snaps and drags smoothly across the screen via Compose `pointerInput`.
- Collapsed state: Compact capsule pill showing remaining time.
- Expanded state: Direct pause/resume/stop controls and subject label.
- Touch events outside bounds pass through freely to underlying applications.

### 3. Jetpack Glance Widgets (`YoucanGlanceWidget`)
- Supports multiple home screen widget instances with individual configuration.
- Types: `Focus Timer`, `Today's Goal Progress`, `Streak Counter`, `Nearest Countdown`, `Motivational Quote`, `Weekly Mini-Chart`.
- Includes `WidgetConfigActivity` with live preview and AMOLED toggle.

### 4. App Usage Awareness (`UsageRepository`)
- Uses official Android `UsageStatsManager` with permission guard (`PermissionHelper`).
- Never inspects screen content or accessibility services.
- Classifies apps into distracting vs. productive, tracking in-session and out-of-session time.

### 5. Voice Assistant & Deep Links (`VoiceCommandHandler`)
Supported deep links:
- `youcan://timer/start?minutes=30&subject=Math`
- `youcan://timer/pause`
- `youcan://timer/resume`
- `youcan://timer/stop`
- `youcan://timer`
- `youcan://stats`
- `youcan://countdown/add`
- `youcan://reminder/add`

### 6. Adaptive Layouts & Insets
- `AdaptiveScreenWrapper` wraps content screens with `safeDrawingPadding()` and centers them with a `840.dp` maximum width on wide displays (tablets, foldables, and landscape mode).
- Landscape tablets render an adaptive two-column layout on the hero `TimerScreen` with an expanded 350dp timer ring and side-by-side controls.
- 30-day consistency heatmap in `StudyHeatmapView` calculates dynamic cell sizes and centers the 10x3 grid horizontally.

---

## Build & Testing Commands

Build debug APK:
```bash
./gradlew assembleDebug
```
Output APK location:
```
app/build/outputs/apk/debug/app-debug.apk
```

Run unit tests:
```bash
./gradlew testDebugUnitTest
```

Install on connected physical tablet/phone:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Launch app:
```bash
adb shell am start -n com.drnitz.youcan.debug/com.drnitz.youcan.MainActivity
```

Test voice deep link:
```bash
adb shell am start -a android.intent.action.VIEW -d "youcan://timer/start?minutes=25&subject=Physics"
```
