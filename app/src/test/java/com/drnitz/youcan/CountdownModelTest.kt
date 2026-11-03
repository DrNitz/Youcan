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
}
