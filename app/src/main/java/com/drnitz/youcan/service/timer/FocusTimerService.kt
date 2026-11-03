package com.drnitz.youcan.service.timer

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.drnitz.youcan.MainActivity
import com.drnitz.youcan.R
import com.drnitz.youcan.YoucanApplication
import com.drnitz.youcan.data.repository.SessionRepository
import com.drnitz.youcan.domain.model.TimerPhase
import com.drnitz.youcan.domain.model.TimerSnapshot
import com.drnitz.youcan.domain.model.TimerStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class FocusTimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var sessionStartTimeMillis: Long = 0L

    private val app by lazy { application as YoucanApplication }
    private val sessionRepo by lazy { SessionRepository(app.database.sessionDao()) }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        activeService = this
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_RESUME
        if (action != ACTION_STOP) {
            promoteToForeground()
        }
        when (action) {
            ACTION_START -> {
                val subject = intent?.getStringExtra(EXTRA_SUBJECT) ?: "General Study"
                val note = intent?.getStringExtra(EXTRA_NOTE) ?: ""
                startTimer(subject, note)
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopTimer()
            ACTION_SKIP -> skipToNextPhase()
            ACTION_ADD_MINUTES -> {
                val minutes = intent?.getIntExtra(EXTRA_MINUTES, 5) ?: 5
                addMinutes(this, minutes)
            }
        }
        return START_STICKY
    }

    private fun promoteToForeground() {
        try {
            val notification = buildNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            android.util.Log.e("FocusTimerService", "Failed to promote service to foreground", e)
        }
    }

    private fun startTimer(subject: String, note: String) {
        serviceScope.launch {
            val userPrefs = app.preferences.userPreferencesFlow.first()
            val defaultDurationSeconds = userPrefs.focusDurationMinutes * 60
            sessionStartTimeMillis = System.currentTimeMillis()
            val current = TimerStateManager.snapshot.value
            val totalSeconds = if (current.totalSeconds > 0) current.totalSeconds else defaultDurationSeconds
            val targetEnd = if (current.status == TimerStatus.RUNNING && current.targetEndRealtime > SystemClock.elapsedRealtime()) {
                current.targetEndRealtime
            } else {
                SystemClock.elapsedRealtime() + (totalSeconds * 1000L)
            }

            TimerStateManager.updateSnapshot(
                TimerSnapshot(
                    phase = TimerPhase.FOCUS,
                    status = TimerStatus.RUNNING,
                    remainingSeconds = totalSeconds,
                    totalSeconds = totalSeconds,
                    currentCycle = 1,
                    totalCycles = userPrefs.cyclesBeforeLongBreak,
                    subject = subject,
                    note = note,
                    targetEndRealtime = targetEnd
                )
            )

            promoteToForeground()
            com.drnitz.youcan.widget.WidgetUpdateHelper.updateTimerWidgets(this@FocusTimerService)
            runTimerLoop()
        }
    }

    private fun resumeTimer() {
        val current = TimerStateManager.snapshot.value
        val remaining = if (current.remainingSeconds > 0) current.remainingSeconds else (25 * 60)
        val targetEnd = if (current.status == TimerStatus.RUNNING && current.targetEndRealtime > SystemClock.elapsedRealtime()) {
            current.targetEndRealtime
        } else {
            SystemClock.elapsedRealtime() + (remaining * 1000L)
        }
        TimerStateManager.updateSnapshot(
            current.copy(
                status = TimerStatus.RUNNING,
                remainingSeconds = remaining,
                targetEndRealtime = targetEnd
            )
        )
        promoteToForeground()
        com.drnitz.youcan.widget.WidgetUpdateHelper.updateTimerWidgets(this@FocusTimerService)
        runTimerLoop()
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        val current = TimerStateManager.snapshot.value
        val now = SystemClock.elapsedRealtime()
        val remaining = if (current.targetEndRealtime > 0L) {
            ((current.targetEndRealtime - now + 999L) / 1000L).toInt().coerceAtLeast(0)
        } else {
            current.remainingSeconds
        }

        TimerStateManager.updateSnapshot(
            current.copy(
                status = TimerStatus.PAUSED,
                remainingSeconds = remaining,
                targetEndRealtime = 0L
            )
        )
        updateNotification()
        com.drnitz.youcan.widget.WidgetUpdateHelper.updateTimerWidgets(this@FocusTimerService)
    }

    private fun stopTimer() {
        timerJob?.cancel()
        val current = TimerStateManager.snapshot.value
        val now = SystemClock.elapsedRealtime()
        val remaining = if (current.targetEndRealtime > 0L) {
            ((current.targetEndRealtime - now + 999L) / 1000L).toInt().coerceAtLeast(0)
        } else {
            current.remainingSeconds
        }
        val elapsed = current.totalSeconds - remaining
        if (current.phase == TimerPhase.FOCUS && elapsed >= 60) {
            val startMillis = sessionStartTimeMillis
            val endMillis = System.currentTimeMillis()
            val subject = current.subject
            val note = current.note
            serviceScope.launch(Dispatchers.IO) {
                sessionRepo.logSession(
                    startTime = startMillis,
                    endTime = endMillis,
                    durationSeconds = elapsed,
                    subject = subject,
                    note = note,
                    isCompleted = false,
                    isManual = false
                )
                com.drnitz.youcan.widget.WidgetUpdateHelper.updateTimerWidgets(this@FocusTimerService)
            }
        }

        val focusDurationSeconds = if (current.totalSeconds > 0) current.totalSeconds else (25 * 60)

        TimerStateManager.reset(focusDurationSeconds)
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.cancel(NOTIFICATION_ID)
        com.drnitz.youcan.widget.WidgetUpdateHelper.updateTimerWidgets(this@FocusTimerService)
        stopSelf()
    }

    private fun skipToNextPhase() {
        timerJob?.cancel()
        val current = TimerStateManager.snapshot.value
        serviceScope.launch {
            advancePhase(current, completedNaturally = false)
        }
    }

    private fun runTimerLoop() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(500L)
                val current = TimerStateManager.snapshot.value
                if (current.status != TimerStatus.RUNNING) break

                val target = current.targetEndRealtime
                val nowRealtime = SystemClock.elapsedRealtime()
                val remaining = ((target - nowRealtime + 999L) / 1000L).toInt()

                if (remaining > 0) {
                    if (remaining != current.remainingSeconds) {
                        TimerStateManager.updateTime(remaining)
                        updateNotification()
                    }
                } else {
                    TimerStateManager.updateTime(0)
                    advancePhase(current, completedNaturally = true)
                    break
                }
            }
        }
    }

    private suspend fun advancePhase(current: TimerSnapshot, completedNaturally: Boolean) {
        val userPrefs = app.preferences.userPreferencesFlow.first()

        // 1. Play sound/vibration alert
        app.soundEffectManager.playPhaseEndAlert(
            soundEnabled = userPrefs.soundAlertEnabled,
            vibrationEnabled = userPrefs.vibrationAlertEnabled
        )

        // 2. Log session if FOCUS completed
        if (current.phase == TimerPhase.FOCUS && completedNaturally) {
            sessionRepo.logSession(
                startTime = sessionStartTimeMillis,
                endTime = System.currentTimeMillis(),
                durationSeconds = current.totalSeconds,
                subject = current.subject,
                note = current.note,
                isCompleted = true,
                isManual = false
            )
        }

        // 3. Determine next phase
        val nextPhase: TimerPhase
        var nextCycle = current.currentCycle
        val nextTotalSeconds: Int

        when (current.phase) {
            TimerPhase.FOCUS -> {
                if (current.currentCycle >= current.totalCycles) {
                    nextPhase = TimerPhase.LONG_BREAK
                    nextTotalSeconds = userPrefs.longBreakMinutes * 60
                } else {
                    nextPhase = TimerPhase.SHORT_BREAK
                    nextTotalSeconds = userPrefs.shortBreakMinutes * 60
                }
            }
            TimerPhase.SHORT_BREAK -> {
                nextPhase = TimerPhase.FOCUS
                nextCycle = current.currentCycle + 1
                nextTotalSeconds = userPrefs.focusDurationMinutes * 60
                sessionStartTimeMillis = System.currentTimeMillis()
            }
            TimerPhase.LONG_BREAK -> {
                nextPhase = TimerPhase.FOCUS
                nextCycle = 1
                nextTotalSeconds = userPrefs.focusDurationMinutes * 60
                sessionStartTimeMillis = System.currentTimeMillis()
            }
        }

        val nextTargetEnd = SystemClock.elapsedRealtime() + (nextTotalSeconds * 1000L)
        TimerStateManager.updateSnapshot(
            current.copy(
                phase = nextPhase,
                status = TimerStatus.RUNNING,
                remainingSeconds = nextTotalSeconds,
                totalSeconds = nextTotalSeconds,
                currentCycle = nextCycle,
                targetEndRealtime = nextTargetEnd
            )
        )

        updateNotification()
        com.drnitz.youcan.widget.WidgetUpdateHelper.updateAll(this@FocusTimerService)
        runTimerLoop()
    }

    private fun buildNotification(): Notification {
        val snapshot = TimerStateManager.snapshot.value
        val minutes = snapshot.remainingSeconds / 60
        val seconds = snapshot.remainingSeconds % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)
        val phaseName = snapshot.phase.displayName

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            data = android.net.Uri.parse("youcan://timer")
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(this, FocusTimerService::class.java).apply { action = ACTION_PAUSE }
        val pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val resumeIntent = Intent(this, FocusTimerService::class.java).apply { action = ACTION_RESUME }
        val resumePendingIntent = PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, FocusTimerService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, YoucanApplication.CHANNEL_TIMER_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$phaseName: $timeString")
            .setContentText("${snapshot.subject} • Cycle ${snapshot.currentCycle}/${snapshot.totalCycles}")
            .setContentIntent(openPendingIntent)
            .setOngoing(snapshot.status == TimerStatus.RUNNING)
            .setOnlyAlertOnce(true)
            .setProgress(snapshot.totalSeconds, snapshot.totalSeconds - snapshot.remainingSeconds, false)

        if (snapshot.status == TimerStatus.RUNNING) {
            builder.addAction(android.R.drawable.ic_media_pause, getString(R.string.action_pause), pausePendingIntent)
        } else if (snapshot.status == TimerStatus.PAUSED) {
            builder.addAction(android.R.drawable.ic_media_play, getString(R.string.action_resume), resumePendingIntent)
        }
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.action_stop), stopPendingIntent)

        return builder.build()
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Youcan:TimerWakeLock").apply {
                setReferenceCounted(false)
                acquire(24 * 60 * 60 * 1000L) // 24 hours max safeguard
            }
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (_: Exception) {}
        wakeLock = null
    }

    override fun onDestroy() {
        if (activeService === this) {
            activeService = null
        }
        timerJob?.cancel()
        releaseWakeLock()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.drnitz.youcan.action.START"
        const val ACTION_PAUSE = "com.drnitz.youcan.action.PAUSE"
        const val ACTION_RESUME = "com.drnitz.youcan.action.RESUME"
        const val ACTION_STOP = "com.drnitz.youcan.action.STOP"
        const val ACTION_SKIP = "com.drnitz.youcan.action.SKIP"
        const val ACTION_ADD_MINUTES = "com.drnitz.youcan.action.ADD_MINUTES"

        const val EXTRA_SUBJECT = "extra_subject"
        const val EXTRA_NOTE = "extra_note"
        const val EXTRA_MINUTES = "extra_minutes"

        @Volatile
        private var activeService: FocusTimerService? = null

        fun isRunning(): Boolean = activeService != null

        fun startTimer(context: Context, subject: String = "General Study", note: String = "") {
            TimerStateManager.init(context)
            val current = TimerStateManager.getSnapshot(context)
            val totalSeconds = if (current.totalSeconds > 0) current.totalSeconds else (25 * 60)
            val targetEnd = SystemClock.elapsedRealtime() + (totalSeconds * 1000L)

            TimerStateManager.updateSnapshot(
                TimerSnapshot(
                    phase = TimerPhase.FOCUS,
                    status = TimerStatus.RUNNING,
                    remainingSeconds = totalSeconds,
                    totalSeconds = totalSeconds,
                    currentCycle = 1,
                    totalCycles = current.totalCycles.coerceAtLeast(1),
                    subject = subject,
                    note = note,
                    targetEndRealtime = targetEnd
                )
            )

            val intent = Intent(context, FocusTimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SUBJECT, subject)
                putExtra(EXTRA_NOTE, note)
            }
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                android.util.Log.e("FocusTimerService", "Failed to start service from startTimer", e)
            }
            com.drnitz.youcan.widget.WidgetUpdateHelper.updateTimerWidgets(context)
        }

        fun pauseTimer(context: Context) {
            TimerStateManager.init(context)
            val active = activeService
            if (active != null) {
                active.pauseTimer()
            } else {
                val current = TimerStateManager.getSnapshot(context)
                val now = SystemClock.elapsedRealtime()
                val remaining = if (current.targetEndRealtime > 0L) {
                    ((current.targetEndRealtime - now + 999L) / 1000L).toInt().coerceAtLeast(0)
                } else {
                    current.remainingSeconds
                }
                TimerStateManager.updateSnapshot(
                    current.copy(
                        status = TimerStatus.PAUSED,
                        remainingSeconds = remaining,
                        targetEndRealtime = 0L
                    )
                )
                com.drnitz.youcan.widget.WidgetUpdateHelper.updateTimerWidgets(context)
            }
        }

        fun resumeTimer(context: Context) {
            TimerStateManager.init(context)
            val active = activeService
            if (active != null) {
                active.resumeTimer()
            } else {
                val current = TimerStateManager.getSnapshot(context)
                val remaining = if (current.remainingSeconds > 0) current.remainingSeconds else (25 * 60)
                val targetEnd = SystemClock.elapsedRealtime() + (remaining * 1000L)
                TimerStateManager.updateSnapshot(
                    current.copy(
                        status = TimerStatus.RUNNING,
                        remainingSeconds = remaining,
                        targetEndRealtime = targetEnd
                    )
                )
                val intent = Intent(context, FocusTimerService::class.java).apply { action = ACTION_RESUME }
                try {
                    ContextCompat.startForegroundService(context, intent)
                } catch (e: Exception) {
                    android.util.Log.e("FocusTimerService", "Failed to start service from resumeTimer", e)
                }
                com.drnitz.youcan.widget.WidgetUpdateHelper.updateTimerWidgets(context)
            }
        }

        fun addMinutes(context: Context, minutes: Int) {
            TimerStateManager.init(context)
            val current = TimerStateManager.getSnapshot(context)
            val addSeconds = minutes * 60
            val newRemaining = current.remainingSeconds + addSeconds
            val newTotal = current.totalSeconds + addSeconds
            val newTarget = if (current.status == TimerStatus.RUNNING) {
                val base = if (current.targetEndRealtime > 0L) current.targetEndRealtime else SystemClock.elapsedRealtime()
                base + (addSeconds * 1000L)
            } else {
                0L
            }
            TimerStateManager.updateSnapshot(
                current.copy(
                    remainingSeconds = newRemaining,
                    totalSeconds = newTotal,
                    targetEndRealtime = newTarget
                )
            )
            activeService?.updateNotification()
            com.drnitz.youcan.widget.WidgetUpdateHelper.updateTimerWidgets(context)
        }

        fun stopTimer(context: Context) {
            TimerStateManager.init(context)
            val active = activeService
            if (active != null) {
                active.stopTimer()
            } else {
                val current = TimerStateManager.getSnapshot(context)
                val focusDurationSeconds = if (current.totalSeconds > 0) current.totalSeconds else (25 * 60)
                TimerStateManager.reset(focusDurationSeconds)
                try {
                    val intent = Intent(context, FocusTimerService::class.java).apply { action = ACTION_STOP }
                    context.stopService(intent)
                } catch (_: Exception) {}
                com.drnitz.youcan.widget.WidgetUpdateHelper.updateTimerWidgets(context)
            }
        }

        fun skipPhase(context: Context) {
            val active = activeService
            if (active != null) {
                active.skipToNextPhase()
            } else {
                val intent = Intent(context, FocusTimerService::class.java).apply { action = ACTION_SKIP }
                try {
                    ContextCompat.startForegroundService(context, intent)
                } catch (_: Exception) {}
            }
        }
    }
}
