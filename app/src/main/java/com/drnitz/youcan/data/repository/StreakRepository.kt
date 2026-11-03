package com.drnitz.youcan.data.repository

import com.drnitz.youcan.data.local.dao.SessionDao
import com.drnitz.youcan.data.local.dao.StreakFreezeDao
import com.drnitz.youcan.data.local.entity.StreakFreezeEntity
import com.drnitz.youcan.domain.model.DayStudySummary
import com.drnitz.youcan.domain.model.StreakData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class StreakRepository(
    private val sessionDao: SessionDao,
    private val streakFreezeDao: StreakFreezeDao
) {

    val allFreezes: Flow<List<StreakFreezeEntity>> = streakFreezeDao.getAllFreezes()

    suspend fun applyStreakFreeze(date: LocalDate) {
        streakFreezeDao.insertFreeze(
            StreakFreezeEntity(dateEpochDay = date.toEpochDay())
        )
    }

    suspend fun isDateFrozen(date: LocalDate): Boolean {
        return streakFreezeDao.isFrozen(date.toEpochDay()) > 0
    }

    fun observeStreakData(
        dailyGoalMinutes: Int,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Flow<StreakData> {
        return combine(
            sessionDao.getAllSessions(),
            streakFreezeDao.getAllFreezes()
        ) { sessions, freezes ->
            calculateStreak(sessions, freezes, dailyGoalMinutes, zoneId)
        }
    }

    fun calculateStreak(
        sessions: List<com.drnitz.youcan.data.local.entity.FocusSessionEntity>,
        freezes: List<StreakFreezeEntity>,
        dailyGoalMinutes: Int,
        zoneId: ZoneId = ZoneId.systemDefault(),
        today: LocalDate = LocalDate.now(zoneId)
    ): StreakData {
        val freezeEpochDays = freezes.map { it.dateEpochDay }.toSet()

        // Map sessions to local dates
        val dayMinutesMap = mutableMapOf<LocalDate, Int>()
        for (session in sessions) {
            val sessionDate = Instant.ofEpochMilli(session.startTime)
                .atZone(zoneId)
                .toLocalDate()
            val minutes = session.durationSeconds / 60
            dayMinutesMap[sessionDate] = (dayMinutesMap[sessionDate] ?: 0) + minutes
        }

        val todayMinutes = dayMinutesMap[today] ?: 0
        val isTodayMet = todayMinutes >= dailyGoalMinutes

        // Build past 30 days summaries for the heatmap calendar
        val past30Days = (0..29).map { offset ->
            val date = today.minusDays(offset.toLong())
            val mins = dayMinutesMap[date] ?: 0
            val isFrozen = freezeEpochDays.contains(date.toEpochDay())
            val met = mins >= dailyGoalMinutes
            DayStudySummary(
                date = date,
                totalMinutes = mins,
                isGoalMet = met,
                isFrozen = isFrozen,
                topAppName = ""
            )
        }.reversed()

        // Calculate current streak
        // Check backwards starting from today (or yesterday if today is not yet met)
        var currentStreak = 0
        var checkDate = if (isTodayMet) today else today.minusDays(1)

        while (true) {
            val mins = dayMinutesMap[checkDate] ?: 0
            val isFrozen = freezeEpochDays.contains(checkDate.toEpochDay())

            if (mins >= dailyGoalMinutes) {
                currentStreak++
                checkDate = checkDate.minusDays(1)
            } else if (isFrozen) {
                // Streak freeze protects this day without breaking or incrementing streak
                checkDate = checkDate.minusDays(1)
            } else {
                // If today wasn't met, and yesterday also failed, current streak is 0
                break
            }
        }

        // Calculate longest streak historically
        val sortedDates = dayMinutesMap.keys.union(
            freezeEpochDays.map { LocalDate.ofEpochDay(it) }
        ).sorted()

        var longestStreak = 0
        var tempStreak = 0
        var prevDate: LocalDate? = null

        for (date in sortedDates) {
            val mins = dayMinutesMap[date] ?: 0
            val isFrozen = freezeEpochDays.contains(date.toEpochDay())
            val qualifies = mins >= dailyGoalMinutes || isFrozen

            if (qualifies) {
                if (prevDate == null || date == prevDate.plusDays(1)) {
                    if (mins >= dailyGoalMinutes) tempStreak++
                } else {
                    tempStreak = if (mins >= dailyGoalMinutes) 1 else 0
                }
                prevDate = date
                if (tempStreak > longestStreak) {
                    longestStreak = tempStreak
                }
            } else {
                tempStreak = 0
                prevDate = null
            }
        }

        if (currentStreak > longestStreak) {
            longestStreak = currentStreak
        }

        return StreakData(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            todayMinutes = todayMinutes,
            dailyGoalMinutes = dailyGoalMinutes,
            isGoalMetToday = isTodayMet,
            past30Days = past30Days,
            totalFrozenDays = freezes.size
        )
    }
}
