package com.drnitz.youcan.service.timer

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import com.drnitz.youcan.domain.model.TimerPhase
import com.drnitz.youcan.domain.model.TimerSnapshot
import com.drnitz.youcan.domain.model.TimerStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TimerStateManager {

    private val _snapshot = MutableStateFlow(TimerSnapshot())
    val snapshot: StateFlow<TimerSnapshot> = _snapshot.asStateFlow()

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        val appContext = context.applicationContext
        val sp = appContext.getSharedPreferences("youcan_timer_state_prefs", Context.MODE_PRIVATE)
        prefs = sp

        val hasSaved = sp.contains("timer_status")
        if (hasSaved) {
            val statusStr = sp.getString("timer_status", TimerStatus.IDLE.name) ?: TimerStatus.IDLE.name
            val phaseStr = sp.getString("timer_phase", TimerPhase.FOCUS.name) ?: TimerPhase.FOCUS.name
            val remaining = sp.getInt("timer_remaining_seconds", 25 * 60)
            val total = sp.getInt("timer_total_seconds", 25 * 60)
            val currentCycle = sp.getInt("timer_current_cycle", 1)
            val totalCycles = sp.getInt("timer_total_cycles", 4)
            val subject = sp.getString("timer_subject", "General Study") ?: "General Study"
            val note = sp.getString("timer_note", "") ?: ""
            val targetEndRealtime = sp.getLong("timer_target_end_realtime", 0L)

            val status = runCatching { TimerStatus.valueOf(statusStr) }.getOrDefault(TimerStatus.IDLE)
            val phase = runCatching { TimerPhase.valueOf(phaseStr) }.getOrDefault(TimerPhase.FOCUS)

            if (status == TimerStatus.RUNNING && targetEndRealtime > 0L) {
                val nowRealtime = SystemClock.elapsedRealtime()
                val liveRemaining = ((targetEndRealtime - nowRealtime + 999L) / 1000L).toInt()
                if (liveRemaining > 0) {
                    _snapshot.value = TimerSnapshot(
                        phase = phase,
                        status = TimerStatus.RUNNING,
                        remainingSeconds = liveRemaining,
                        totalSeconds = total,
                        currentCycle = currentCycle,
                        totalCycles = totalCycles,
                        subject = subject,
                        note = note,
                        targetEndRealtime = targetEndRealtime
                    )
                } else {
                    _snapshot.value = TimerSnapshot(
                        phase = phase,
                        status = TimerStatus.IDLE,
                        remainingSeconds = total,
                        totalSeconds = total,
                        currentCycle = currentCycle,
                        totalCycles = totalCycles,
                        subject = subject,
                        note = note,
                        targetEndRealtime = 0L
                    )
                    persistState(_snapshot.value)
                }
            } else {
                _snapshot.value = TimerSnapshot(
                    phase = phase,
                    status = status,
                    remainingSeconds = remaining,
                    totalSeconds = total,
                    currentCycle = currentCycle,
                    totalCycles = totalCycles,
                    subject = subject,
                    note = note,
                    targetEndRealtime = targetEndRealtime
                )
            }
        }
    }

    fun updateSnapshot(newSnapshot: TimerSnapshot) {
        _snapshot.value = newSnapshot
        persistState(newSnapshot)
    }

    fun updateTime(remainingSeconds: Int) {
        val updated = _snapshot.value.copy(remainingSeconds = remainingSeconds)
        _snapshot.value = updated
        // Note: Do not call persistState() on every 1-second tick to prevent flash I/O churn and battery drain.
        // targetEndRealtime is already persisted on state transitions.
    }

    fun setStatus(status: TimerStatus) {
        val updated = _snapshot.value.copy(status = status)
        _snapshot.value = updated
        persistState(updated)
    }

    fun setSubject(subject: String) {
        val updated = _snapshot.value.copy(subject = subject)
        _snapshot.value = updated
        persistState(updated)
    }

    fun setNote(note: String) {
        val updated = _snapshot.value.copy(note = note)
        _snapshot.value = updated
        persistState(updated)
    }

    fun reset(focusDurationSeconds: Int = 25 * 60) {
        val resetSnapshot = TimerSnapshot(
            phase = TimerPhase.FOCUS,
            status = TimerStatus.IDLE,
            remainingSeconds = focusDurationSeconds,
            totalSeconds = focusDurationSeconds,
            currentCycle = 1,
            totalCycles = 4,
            subject = _snapshot.value.subject,
            note = "",
            targetEndRealtime = 0L
        )
        _snapshot.value = resetSnapshot
        persistState(resetSnapshot)
    }

    fun getSnapshot(context: Context? = null): TimerSnapshot {
        if (prefs == null && context != null) {
            init(context)
        }
        val current = _snapshot.value
        if (current.status == TimerStatus.RUNNING && current.targetEndRealtime > 0L) {
            val nowRealtime = SystemClock.elapsedRealtime()
            val liveRemaining = ((current.targetEndRealtime - nowRealtime + 999L) / 1000L).toInt()
            if (liveRemaining <= 0) {
                val expired = current.copy(
                    status = TimerStatus.IDLE,
                    remainingSeconds = current.totalSeconds,
                    targetEndRealtime = 0L
                )
                _snapshot.value = expired
                persistState(expired)
                return expired
            }
            return current.copy(remainingSeconds = liveRemaining)
        }
        return current
    }

    private fun persistState(snapshot: TimerSnapshot) {
        val sp = prefs ?: return
        try {
            sp.edit()
                .putString("timer_status", snapshot.status.name)
                .putString("timer_phase", snapshot.phase.name)
                .putInt("timer_remaining_seconds", snapshot.remainingSeconds)
                .putInt("timer_total_seconds", snapshot.totalSeconds)
                .putInt("timer_current_cycle", snapshot.currentCycle)
                .putInt("timer_total_cycles", snapshot.totalCycles)
                .putString("timer_subject", snapshot.subject)
                .putString("timer_note", snapshot.note)
                .putLong("timer_target_end_realtime", snapshot.targetEndRealtime)
                .apply()
            runCatching {
                val app = com.drnitz.youcan.YoucanApplication.instance
                com.drnitz.youcan.widget.WidgetUpdateHelper.updateTimerWidgets(app)
            }
        } catch (_: Exception) {}
    }
}
