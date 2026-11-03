package com.drnitz.youcan

import com.drnitz.youcan.domain.model.CountdownItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CountdownModelTest {

    @Test
    fun futureCountdownIsNotExpired() {
        val item = CountdownItem(
            id = 1,
            title = "Final Exam",
            targetEpochMillis = System.currentTimeMillis() + 86400_000L
        )
        assertFalse(item.isExpired)
    }

    @Test
    fun pastCountdownIsExpired() {
        val item = CountdownItem(
            id = 2,
            title = "Past Quiz",
            targetEpochMillis = System.currentTimeMillis() - 1000L
        )
        assertTrue(item.isExpired)
    }

    @Test
    fun countdownItem_defaultOffsetsInclude24hAnd1h() {
        val item = CountdownItem(
            id = 3,
            title = "Physics Midterm",
            targetEpochMillis = System.currentTimeMillis() + 86400_000L
        )
        assertTrue(item.reminderOffsetsMinutes.contains(1440)) // 24 hours
        assertTrue(item.reminderOffsetsMinutes.contains(60))   // 1 hour
    }
}
