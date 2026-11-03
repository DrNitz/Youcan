# Youcan — Offline-First Focus & Study Companion

Youcan is a calm, distraction-free study and focus Android app designed to help you build lasting study habits without accounts, tracking, or cloud sync. Everything stays on your device.

---

## Highlights

- **Hero Focus Timer**: Customizable Pomodoro intervals (Focus, Short Break, Long Break, Cycle count), quick subject chips, and session notes.
- **Background Foreground Service**: Sticky ongoing notification with live pause/resume/stop actions, audio cues, and vibration alerts that persist even if the app UI is dismissed.
- **Floating Island Dock**: Optional draggable pill overlay (`SYSTEM_ALERT_WINDOW`) allowing focus tracking while researching in other apps.
- **Streaks & Consistency Heatmap**: 30-day GitHub-style study heatmap, personal best records, streak freeze protections, manual session logging, and shareable streak cards.
- **Exam & Deadline Countdowns**: Milestone trackers counting down in days, hours, and minutes with customizable notifications.
- **Daily Study Plan Checklist**: Quick plan for today's tasks with a one-tap copy to tomorrow.
- **Phone Usage Awareness**: On-device screen time analytics via `UsageStatsManager` with distracting app tagging and focus-session correlation.
- **Interactive Glance Widgets**: Home screen widgets for timers, streaks, daily goals, countdowns, and study analytics. Includes zero-drift monotonic countdown synchronization (`SystemClock.elapsedRealtime()`), 1-tap launcher widget pinning from all primary screens (Timer, Tasks, Countdowns, Streaks, Analytics), and interactive controls (+5m extensions, phase skipping, countdown milestone cycling, task check-offs, and +30m quick study logging).
- **Calm Organic Aesthetics**: Deep Forest and Sage palette with True Black AMOLED mode, animated Canvas themes (Aurora, Waves, Rain, Stars), and battery-saving motion reduction.
- **Adaptive Layouts**: Automatic navigation bar on compact phones and navigation rail on tablets, foldables, and landscape orientations.
- **Voice Assistant Integration**: Android dynamic shortcuts and `youcan://...` deep links.

---

## Technical Stack

- **Language**: Kotlin 2.3.20 (JDK 17)
- **UI Toolkit**: Jetpack Compose & Material 3 Adaptive Navigation Suite
- **Storage**: Room SQLite database with KSP code generation & DataStore Preferences
- **Widgets**: Jetpack Glance (`androidx.glance:glance-appwidget`)
- **Graphics & Motion**: Handcrafted Compose Canvas engines & Konfetti milestone celebrations
- **SDK Targets**: compileSdk 36, targetSdk 36, minSdk 26 (Android 8.0 Oreo+)

---

## Getting Started

### Prerequisites
- Android SDK (API 36)
- Android physical device or tablet (minSdk 26+) connected with USB debugging enabled.

### Build the APK
```bash
./gradlew assembleDebug
```
The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Install on Device
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Launch
```bash
adb shell am start -n com.drnitz.youcan.debug/com.drnitz.youcan.MainActivity
```

---

## Deep Link Shortcuts
You can test voice shortcuts and intents directly using `adb`:
- **Start focus session**: `adb shell am start -a android.intent.action.VIEW -d "youcan://timer/start?minutes=25&subject=Math"`
- **Pause timer**: `adb shell am start -a android.intent.action.VIEW -d "youcan://timer/pause"`
- **Resume timer**: `adb shell am start -a android.intent.action.VIEW -d "youcan://timer/resume"`
- **Open analytics**: `adb shell am start -a android.intent.action.VIEW -d "youcan://stats"`
- **Add reminder**: `adb shell am start -a android.intent.action.VIEW -d "youcan://reminder/add"`
- **Add countdown**: `adb shell am start -a android.intent.action.VIEW -d "youcan://countdown/add"`

---

## Privacy
Youcan does not include any internet permissions or tracking SDKs. All session records, plans, reminders, and usage statistics remain exclusively in your device's local storage.
