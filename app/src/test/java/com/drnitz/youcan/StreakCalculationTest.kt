package com.drnitz.youcan

import com.drnitz.youcan.data.local.dao.SessionDao
import com.drnitz.youcan.data.local.dao.StreakFreezeDao
import com.drnitz.youcan.data.local.entity.FocusSessionEntity
import com.drnitz.youcan.data.local.entity.StreakFreezeEntity
import com.drnitz.youcan.data.repository.StreakRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class StreakCalculationTest {

    private val zoneId = ZoneId.of("UTC")
    private val today = LocalDate.of(2026, 11, 2)

    // Dummy DAO mocks for calculateStreak unit tests (which only uses the pure calculation function)
    private val streakRepo = StreakRepository(
        sessionDao = object : SessionDao {
            override suspend fun insertSession(session: FocusSessionEntity) = 1L
            override suspend fun updateSession(session: FocusSessionEntity) {}
            override suspend fun deleteSession(session: FocusSessionEntity) {}
            override fun getAllSessions() = kotlinx.coroutines.flow.emptyFlow<List<FocusSessionEntity>>()
            override fun getSessionsBetween(startMillis: Long, endMillis: Long) = kotlinx.coroutines.flow.emptyFlow<List<FocusSessionEntity>>()
            override suspend fun getSessionsBetweenSync(startMillis: Long, endMillis: Long) = emptyList<FocusSessionEntity>()
            override fun getDistinctSubjects() = kotlinx.coroutines.flow.emptyFlow<List<String>>()
            override fun getTotalStudySeconds(startMillis: Long, endMillis: Long) = kotlinx.coroutines.flow.emptyFlow<Long?>()
            override suspend fun getTotalStudySecondsSync(startMillis: Long, endMillis: Long): Long? = null
            override suspend fun getAllSessionsSync(): List<FocusSessionEntity> = emptyList()
        },
        streakFreezeDao = object : StreakFreezeDao {
            override suspend fun insertFreeze(freeze: StreakFreezeEntity) {}
            override fun getAllFreezes() = kotlinx.coroutines.flow.emptyFlow<List<StreakFreezeEntity>>()
            override suspend fun getAllFreezesSync() = emptyList<StreakFreezeEntity>()
            override suspend fun isFrozen(epochDay: Long) = 0
        }
    )

    private fun createSession(date: LocalDate, minutes: Int): FocusSessionEntity {
        val startMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli() + 3600_000L
        return FocusSessionEntity(
            startTime = startMillis,
            endTime = startMillis + (minutes * 60 * 1000L),
            durationSeconds = minutes * 60,
            subject = "Math",
            isCompleted = true
        )
    }

    @Test
    fun streakIncreasesWhenConsecutiveDaysMeetGoal() {
        val sessions = listOf(
            createSession(today.minusDays(2), 60),
            createSession(today.minusDays(1), 60),
            createSession(today, 60)
        )

        val result = streakRepo.calculateStreak(
            sessions = sessions,
            freezes = emptyList(),
            dailyGoalMinutes = 60,
            zoneId = zoneId,
            today = today
        )

        assertEquals(3, result.currentStreak)
        assertEquals(3, result.longestStreak)
        assertTrue(result.isGoalMetToday)
    }

    @Test
    fun streakFreezePreservesStreakAcrossMissedDay() {
        val missedDay = today.minusDays(1)
        val sessions = listOf(
            createSession(today.minusDays(2), 60),
            // Missed yesterday, but applied streak freeze
            createSession(today, 60)
        )
        val freezes = listOf(StreakFreezeEntity(dateEpochDay = missedDay.toEpochDay()))

        val result = streakRepo.calculateStreak(
            sessions = sessions,
            freezes = freezes,
            dailyGoalMinutes = 60,
            zoneId = zoneId,
            today = today
        )

        assertEquals(2, result.currentStreak)
        assertEquals(2, result.longestStreak)
    }

    @Test
    fun streakResetsWhenDayMissedWithoutFreeze() {
        val sessions = listOf(
            createSession(today.minusDays(5), 60),
            createSession(today.minusDays(4), 60),
            // Missed 3 days ago, 2 days ago, yesterday
            createSession(today, 60)
        )

        val result = streakRepo.calculateStreak(
            sessions = sessions,
            freezes = emptyList(),
            dailyGoalMinutes = 60,
            zoneId = zoneId,
            today = today
        )

        assertEquals(1, result.currentStreak)
        assertEquals(2, result.longestStreak)
    }
}
