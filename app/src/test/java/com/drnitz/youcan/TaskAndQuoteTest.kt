package com.drnitz.youcan

import com.drnitz.youcan.domain.model.QuoteProvider
import com.drnitz.youcan.domain.model.TaskItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TaskAndQuoteTest {

    @Test
    fun taskReminderFormatted_correctForMorningAndAfternoon() {
        val morningTask = TaskItem(
            task = "Read pathology",
            hasReminder = true,
            reminderHour = 9,
            reminderMinute = 5
        )
        assertEquals("9:05 AM", morningTask.reminderFormatted)

        val afternoonTask = TaskItem(
            task = "Review anatomy",
            hasReminder = true,
            reminderHour = 14,
            reminderMinute = 30
        )
        assertEquals("2:30 PM", afternoonTask.reminderFormatted)

        val midnightTask = TaskItem(
            task = "Late review",
            hasReminder = true,
            reminderHour = 0,
            reminderMinute = 15
        )
        assertEquals("12:15 AM", midnightTask.reminderFormatted)

        val noonTask = TaskItem(
            task = "Noon session",
            hasReminder = true,
            reminderHour = 12,
            reminderMinute = 0
        )
        assertEquals("12:00 PM", noonTask.reminderFormatted)
    }

    @Test
    fun taskWithoutReminder_returnsNull() {
        val taskWithoutReminder = TaskItem(
            task = "Study physics",
            hasReminder = false,
            reminderHour = 9,
            reminderMinute = 0
        )
        assertNull(taskWithoutReminder.reminderFormatted)
    }

    @Test
    fun quoteProvider_returnsValidQuotesAndCyclesSafely() {
        assertTrue(QuoteProvider.quotes.isNotEmpty())

        val quote = QuoteProvider.getQuoteForDate(LocalDate.now())
        assertNotNull(quote.text)
        assertTrue(quote.text.isNotBlank())
        assertNotNull(quote.author)
        assertTrue(quote.author.isNotBlank())

        val q0 = QuoteProvider.getQuoteByIndex(0)
        val qNext = QuoteProvider.getQuoteByIndex(QuoteProvider.quotes.size)
        assertEquals(q0, qNext)

        val qNegative = QuoteProvider.getQuoteByIndex(-1)
        assertEquals(QuoteProvider.quotes.last(), qNegative)
    }
}
