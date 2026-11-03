# Assumptions and Architectural Decisions

## Core Assumptions
1. **Target SDK & Minimum SDK**:
   - `compileSdk = 36`, `targetSdk = 36`, `minSdk = 26` (Android 8.0 Oreo). This covers virtually all active Android devices while allowing modern APIs (notification channels, foreground services, photo picker, dynamic shortcuts).
2. **Offline-First & Privacy**:
   - No network calls or user account logins. All user data (focus sessions, streaks, countdowns, reminders, plans, usage stats) is stored strictly on-device in Room SQLite and DataStore.
3. **Low-Memory Host Optimization**:
   - The development machine has 3.5 GB physical RAM and 2 CPU cores. Gradle daemon heap is constrained to 1280 MB, Kotlin compiler daemon to 1536 MB, single worker (`workers.max=1`), and configuration caching is enabled to ensure fast, reliable builds without host swapping.
4. **App-Usage Tracking Sanction**:
   - Relies exclusively on Android's official `UsageStatsManager` (`android.permission.PACKAGE_USAGE_STATS`). Never reads screen content and never uses accessibility services.
   - If permission is not granted, the app provides a clear, polite explanation and continues normal operation for timer, countdowns, streaks, and reminders.
5. **Foreground Service & Overlay Policies**:
   - Focus timer runs as a foreground service (`type="specialUse"` or `"systemExempted"` as compliant with Android 14/15/16).
   - Island-style compact pill and floating dock use `SYSTEM_ALERT_WINDOW` only after user activation, with an in-app explanation and deep link to system settings. Touch events outside the overlay bounds pass directly through to the OS.
6. **Voice Assistant**:
   - Implemented via dynamic and static Android shortcuts (`ShortcutManagerCompat`) and deep-link intent filters (`youcan://...`), with forward compatibility for Android 16 AppFunctions.
7. **Widgets**:
   - Built with Jetpack Glance (`androidx.glance:glance-appwidget`). Supports multiple instances, per-widget configuration, and static battery-friendly rendering.
