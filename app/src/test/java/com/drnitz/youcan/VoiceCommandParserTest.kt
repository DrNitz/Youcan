package com.drnitz.youcan

import com.drnitz.youcan.voice.VoiceCommandHandler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceCommandParserTest {

    @Test
    fun nullOrEmptyReturnsNone() {
        assertEquals(VoiceCommandHandler.VoiceAction.None, VoiceCommandHandler.parseUri(null))
        assertEquals(VoiceCommandHandler.VoiceAction.None, VoiceCommandHandler.parseUri(""))
    }

    @Test
    fun nonYoucanSchemeReturnsNone() {
        assertEquals(VoiceCommandHandler.VoiceAction.None, VoiceCommandHandler.parseUri("https://google.com"))
    }

    @Test
    fun startTimerUriParsesParameters() {
        val action = VoiceCommandHandler.parseUri("youcan://timer/start?minutes=45&subject=Chemistry")
        assertTrue(action is VoiceCommandHandler.VoiceAction.StartTimer)
        val startAction = action as VoiceCommandHandler.VoiceAction.StartTimer
        assertEquals(45, startAction.minutes)
        assertEquals("Chemistry", startAction.subject)
    }

    @Test
    fun timerControlUrisParsedCorrectly() {
        assertEquals(
            VoiceCommandHandler.VoiceAction.PauseTimer,
            VoiceCommandHandler.parseUri("youcan://timer/pause")
        )
        assertEquals(
            VoiceCommandHandler.VoiceAction.ResumeTimer,
            VoiceCommandHandler.parseUri("youcan://timer/resume")
        )
        assertEquals(
            VoiceCommandHandler.VoiceAction.StopTimer,
            VoiceCommandHandler.parseUri("youcan://timer/stop")
        )
        assertEquals(
            VoiceCommandHandler.VoiceAction.OpenTimer,
            VoiceCommandHandler.parseUri("youcan://timer")
        )
    }

    @Test
    fun statsAndRemindersUrisParsed() {
        assertEquals(
            VoiceCommandHandler.VoiceAction.OpenStats,
            VoiceCommandHandler.parseUri("youcan://stats")
        )
        assertEquals(
            VoiceCommandHandler.VoiceAction.AddReminder,
            VoiceCommandHandler.parseUri("youcan://reminder/add")
        )
        assertEquals(
            VoiceCommandHandler.VoiceAction.AddCountdown,
            VoiceCommandHandler.parseUri("youcan://countdown/add")
        )
        assertEquals(
            VoiceCommandHandler.VoiceAction.OpenCountdowns,
            VoiceCommandHandler.parseUri("youcan://countdowns")
        )
        assertEquals(
            VoiceCommandHandler.VoiceAction.AddTask,
            VoiceCommandHandler.parseUri("youcan://tasks/add")
        )
        assertEquals(
            VoiceCommandHandler.VoiceAction.OpenTasks,
            VoiceCommandHandler.parseUri("youcan://tasks")
        )
        assertEquals(
            VoiceCommandHandler.VoiceAction.OpenStreaks,
            VoiceCommandHandler.parseUri("youcan://streaks")
        )
        assertEquals(
            VoiceCommandHandler.VoiceAction.OpenUsage,
            VoiceCommandHandler.parseUri("youcan://usage")
        )
    }
}
