package com.drnitz.youcan

import com.drnitz.youcan.domain.model.TimerPhase
import com.drnitz.youcan.domain.model.TimerSnapshot
import com.drnitz.youcan.domain.model.TimerStatus
import com.drnitz.youcan.service.timer.TimerStateManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TimerStateTest {

    @Before
    fun setUp() {
        TimerStateManager.reset(25 * 60)
    }

    @Test
    fun initialStateIsIdleFocusPhase() {
        val snapshot = TimerStateManager.snapshot.value
        assertEquals(TimerStatus.IDLE, snapshot.status)
        assertEquals(TimerPhase.FOCUS, snapshot.phase)
        assertEquals(25 * 60, snapshot.remainingSeconds)
        assertEquals(1, snapshot.currentCycle)
    }

    @Test
    fun updateTimeDecrementsCorrectly() {
        TimerStateManager.updateTime(20 * 60)
        assertEquals(20 * 60, TimerStateManager.snapshot.value.remainingSeconds)
    }

    @Test
    fun subjectAndNoteUpdateState() {
        TimerStateManager.setSubject("Physics")
        TimerStateManager.setNote("Exam prep question 4")

        assertEquals("Physics", TimerStateManager.snapshot.value.subject)
        assertEquals("Exam prep question 4", TimerStateManager.snapshot.value.note)
    }

    @Test
    fun resetRestoresDefaultDurations() {
        TimerStateManager.updateSnapshot(
            TimerSnapshot(
                phase = TimerPhase.SHORT_BREAK,
                status = TimerStatus.RUNNING,
                remainingSeconds = 120,
                totalSeconds = 300,
                currentCycle = 2,
                totalCycles = 4,
                subject = "History"
            )
        )

        TimerStateManager.reset(30 * 60)
        val resetState = TimerStateManager.snapshot.value

        assertEquals(TimerPhase.FOCUS, resetState.phase)
        assertEquals(TimerStatus.IDLE, resetState.status)
        assertEquals(30 * 60, resetState.remainingSeconds)
        assertEquals(1, resetState.currentCycle)
    }
}
