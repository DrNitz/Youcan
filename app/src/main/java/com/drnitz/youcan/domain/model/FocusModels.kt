package com.drnitz.youcan.domain.model

import java.time.LocalDate

enum class TimerPhase(val displayName: String) {
    FOCUS("Focus"),
    SHORT_BREAK("Short Break"),
    LONG_BREAK("Long Break")
}

enum class TimerStatus {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED
}

data class TimerSnapshot(
    val phase: TimerPhase = TimerPhase.FOCUS,
    val status: TimerStatus = TimerStatus.IDLE,
    val remainingSeconds: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val currentCycle: Int = 1,
    val totalCycles: Int = 4,
    val subject: String = "General Study",
    val note: String = "",
    val targetEndRealtime: Long = 0L
)

data class DayStudySummary(
    val date: LocalDate,
    val totalMinutes: Int,
    val isGoalMet: Boolean,
    val isFrozen: Boolean,
    val topAppName: String = ""
)

data class StreakData(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val todayMinutes: Int = 0,
    val dailyGoalMinutes: Int = 120,
    val isGoalMetToday: Boolean = false,
    val past30Days: List<DayStudySummary> = emptyList(),
    val totalFrozenDays: Int = 0
)

data class CountdownItem(
    val id: Long = 0L,
    val title: String,
    val targetEpochMillis: Long,
    val subject: String = "",
    val reminderOffsetsMinutes: List<Int> = listOf(1440, 60)
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() >= targetEpochMillis
}

data class StudyReminderItem(
    val id: Long = 0L,
    val title: String,
    val hour: Int,
    val minute: Int,
    val daysOfWeek: List<Int> = emptyList(), // 1=Mon .. 7=Sun
    val oneOffDate: LocalDate? = null,
    val isEnabled: Boolean = true
)

data class TaskItem(
    val id: Long = 0L,
    val task: String,
    val date: LocalDate = LocalDate.now(),
    val isCompleted: Boolean = false,
    val targetMinutes: Int = 0,
    val hasReminder: Boolean = false,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null
) {
    val reminderFormatted: String?
        get() {
            if (!hasReminder || reminderHour == null || reminderMinute == null) return null
            val h = reminderHour
            val m = reminderMinute
            val amPm = if (h >= 12) "PM" else "AM"
            val displayHour = when {
                h == 0 -> 12
                h > 12 -> h - 12
                else -> h
            }
            return String.format("%d:%02d %s", displayHour, m, amPm)
        }
}

typealias DailyPlanItem = TaskItem

data class Quote(
    val text: String,
    val author: String
)

object QuoteProvider {
    val quotes = listOf(
        Quote("Small steps made consistently create profound changes over time.", "James Clear"),
        Quote("Focus is a muscle. Every time you bring your attention back, you strengthen it.", "Cal Newport"),
        Quote("The secret of getting ahead is getting started.", "Mark Twain"),
        Quote("Deep work is the ability to focus without distraction on a cognitively demanding task.", "Cal Newport"),
        Quote("Do what you can, with what you have, where you are.", "Theodore Roosevelt"),
        Quote("Clarity precedes mastery. Quiet your environment to hear your thoughts.", "Robin Sharma"),
        Quote("It does not matter how slowly you go as long as you do not stop.", "Confucius"),
        Quote("Action is the foundational key to all success.", "Pablo Picasso"),
        Quote("You do not rise to the level of your goals. You fall to the level of your systems.", "James Clear"),
        Quote("Where your attention goes, your time goes. Where your time goes, your life goes.", "Oliver Burkeman")
    )

    fun getQuoteForDate(date: LocalDate = LocalDate.now()): Quote {
        val index = (date.toEpochDay() % quotes.size).toInt().let { if (it < 0) it + quotes.size else it }
        return quotes[index]
    }

    fun getQuoteByIndex(index: Int): Quote {
        val safeIndex = (index % quotes.size).let { if (it < 0) it + quotes.size else it }
        return quotes[safeIndex]
    }
}

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val totalTimeMillis: Long,
    val inSessionTimeMillis: Long = 0L,
    val outSessionTimeMillis: Long = 0L,
    val isDistracting: Boolean = false
)
