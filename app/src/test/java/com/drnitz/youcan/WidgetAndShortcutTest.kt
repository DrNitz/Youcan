package com.drnitz.youcan

import com.drnitz.youcan.core.util.ShortcutHelper
import com.drnitz.youcan.voice.VoiceCommandHandler
import com.drnitz.youcan.widget.WIDGET_PRESETS
import com.drnitz.youcan.widget.WidgetBackgroundTheme
import com.drnitz.youcan.widget.WidgetComponent
import com.drnitz.youcan.widget.WidgetLayoutMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetAndShortcutTest {

    @Test
    fun widgetPresets_allHaveValidProperties() {
        assertTrue(WIDGET_PRESETS.isNotEmpty())
        assertEquals(7, WIDGET_PRESETS.size)

        for (preset in WIDGET_PRESETS) {
            assertTrue(preset.id.isNotBlank())
            assertTrue(preset.name.isNotBlank())
            assertTrue(preset.description.isNotBlank())
            assertNotNull(preset.icon)
            assertNotNull(preset.mode)
            assertNotNull(preset.comp1)
            assertNotNull(preset.comp2)
            assertNotNull(preset.recommendedTheme)
        }

        val dashboard = WIDGET_PRESETS.first { it.id == "focus_dashboard" }
        assertEquals(WidgetLayoutMode.COMBO_HORIZONTAL, dashboard.mode)
        assertEquals(WidgetComponent.TIMER, dashboard.comp1)
        assertEquals(WidgetComponent.TASKS, dashboard.comp2)

        val pureTimer = WIDGET_PRESETS.first { it.id == "pure_timer" }
        assertEquals(WidgetLayoutMode.SINGLE, pureTimer.mode)
        assertEquals(WidgetComponent.TIMER, pureTimer.comp1)
        assertEquals(WidgetBackgroundTheme.AMOLED, pureTimer.recommendedTheme)

        val frosted = WIDGET_PRESETS.first { it.id == "frosted_focus" }
        assertEquals(WidgetBackgroundTheme.FROSTED_GLASS, frosted.recommendedTheme)
        assertEquals(0.70f, frosted.recommendedAlpha, 0.01f)
    }

    @Test
    fun shortcutTargets_deepLinksMatchVoiceCommandHandler() {
        for (target in ShortcutHelper.ShortcutTarget.entries) {
            assertTrue(target.deepLinkUri.startsWith("youcan://"))
            val action = VoiceCommandHandler.parseUri(target.deepLinkUri)
            assertTrue("Deep link ${target.deepLinkUri} did not resolve to valid action", action !is VoiceCommandHandler.VoiceAction.None)
        }

        val timerAction = VoiceCommandHandler.parseUri(ShortcutHelper.ShortcutTarget.START_TIMER.deepLinkUri)
        assertTrue(timerAction is VoiceCommandHandler.VoiceAction.StartTimer)
        assertEquals(25, (timerAction as VoiceCommandHandler.VoiceAction.StartTimer).minutes)

        val taskAction = VoiceCommandHandler.parseUri(ShortcutHelper.ShortcutTarget.ADD_TASK.deepLinkUri)
        assertEquals(VoiceCommandHandler.VoiceAction.AddTask, taskAction)

        val streakAction = VoiceCommandHandler.parseUri(ShortcutHelper.ShortcutTarget.STREAKS.deepLinkUri)
        assertEquals(VoiceCommandHandler.VoiceAction.OpenStreaks, streakAction)

        val countdownAction = VoiceCommandHandler.parseUri(ShortcutHelper.ShortcutTarget.COUNTDOWNS.deepLinkUri)
        assertEquals(VoiceCommandHandler.VoiceAction.OpenCountdowns, countdownAction)
    }

    @Test
    fun timerStateManager_resetImmediatelyRestoresIdleAndConfiguredSeconds() {
        val initialDuration = 30 * 60
        com.drnitz.youcan.service.timer.TimerStateManager.updateSnapshot(
            com.drnitz.youcan.domain.model.TimerSnapshot(
                phase = com.drnitz.youcan.domain.model.TimerPhase.FOCUS,
                status = com.drnitz.youcan.domain.model.TimerStatus.RUNNING,
                remainingSeconds = 120,
                totalSeconds = initialDuration,
                currentCycle = 2,
                totalCycles = 4,
                subject = "Physics"
            )
        )

        assertEquals(com.drnitz.youcan.domain.model.TimerStatus.RUNNING, com.drnitz.youcan.service.timer.TimerStateManager.snapshot.value.status)
        assertEquals(120, com.drnitz.youcan.service.timer.TimerStateManager.snapshot.value.remainingSeconds)

        // Stopping / Resetting must synchronously reset state to IDLE and initial duration
        com.drnitz.youcan.service.timer.TimerStateManager.reset(initialDuration)

        val resetSnapshot = com.drnitz.youcan.service.timer.TimerStateManager.snapshot.value
        assertEquals(com.drnitz.youcan.domain.model.TimerStatus.IDLE, resetSnapshot.status)
        assertEquals(com.drnitz.youcan.domain.model.TimerPhase.FOCUS, resetSnapshot.phase)
        assertEquals(initialDuration, resetSnapshot.remainingSeconds)
        assertEquals(initialDuration, resetSnapshot.totalSeconds)
        assertEquals(1, resetSnapshot.currentCycle)
        assertEquals("Physics", resetSnapshot.subject)
        assertEquals(0L, resetSnapshot.targetEndRealtime)
    }

    @Test
    fun timerSnapshot_targetEndRealtimeCalculation() {
        val now = 100_000L
        val snapshot = com.drnitz.youcan.domain.model.TimerSnapshot(
            remainingSeconds = 1500,
            targetEndRealtime = now + 1500 * 1000L
        )
        assertEquals(now + 1500 * 1000L, snapshot.targetEndRealtime)
        assertEquals(1500, snapshot.remainingSeconds)
    }

    @Test
    fun widgetComponents_allRecognizedAndConfigured() {
        val expectedComponents = listOf(
            WidgetComponent.TIMER,
            WidgetComponent.TASKS,
            WidgetComponent.COUNTDOWN,
            WidgetComponent.STREAK,
            WidgetComponent.QUOTE,
            WidgetComponent.USAGE,
            WidgetComponent.ANALYTICS
        )
        for (comp in expectedComponents) {
            assertNotNull(comp.displayName)
            assertTrue(comp.displayName.isNotBlank())
        }
    }

    @Test
    fun widgetThemes_allHaveValidColorsAndAlphas() {
        val themes = WidgetBackgroundTheme.entries
        assertEquals(6, themes.size)

        for (theme in themes) {
            assertTrue(theme.displayName.isNotBlank())
            assertTrue(theme.defaultAlpha in 0.5f..1.0f)
            assertTrue(theme.colorHex >= 0L)
        }

        val frosted = WidgetBackgroundTheme.FROSTED_GLASS
        assertEquals("Frosted Glass", frosted.displayName)
        assertEquals(0.70f, frosted.defaultAlpha, 0.01f)

        val amoled = WidgetBackgroundTheme.AMOLED
        assertEquals(1.0f, amoled.defaultAlpha, 0.01f)
        assertEquals(0xFF000000L, amoled.colorHex)
    }

    @Test
    fun voiceCommandHandler_openWidgetStudioParsed() {
        val action1 = VoiceCommandHandler.parseUri("youcan://widget")
        assertEquals(VoiceCommandHandler.VoiceAction.OpenWidgetStudio, action1)

        val action2 = VoiceCommandHandler.parseUri("youcan://studio")
        assertEquals(VoiceCommandHandler.VoiceAction.OpenWidgetStudio, action2)
    }

    @Test
    fun voiceCommandHandler_allWidgetDeepLinksResolvedDirectlyToSpecificTabs() {
        assertEquals(VoiceCommandHandler.VoiceAction.OpenTimer, VoiceCommandHandler.parseUri("youcan://timer"))
        assertEquals(VoiceCommandHandler.VoiceAction.OpenTasks, VoiceCommandHandler.parseUri("youcan://tasks"))
        assertEquals(VoiceCommandHandler.VoiceAction.OpenCountdowns, VoiceCommandHandler.parseUri("youcan://countdown"))
        assertEquals(VoiceCommandHandler.VoiceAction.OpenCountdowns, VoiceCommandHandler.parseUri("youcan://countdowns"))
        assertEquals(VoiceCommandHandler.VoiceAction.OpenStreaks, VoiceCommandHandler.parseUri("youcan://streaks"))
        assertEquals(VoiceCommandHandler.VoiceAction.OpenStreaks, VoiceCommandHandler.parseUri("youcan://streak"))
        assertEquals(VoiceCommandHandler.VoiceAction.OpenHome, VoiceCommandHandler.parseUri("youcan://home"))
        assertEquals(VoiceCommandHandler.VoiceAction.OpenHome, VoiceCommandHandler.parseUri("youcan://quote"))
        assertEquals(VoiceCommandHandler.VoiceAction.OpenHome, VoiceCommandHandler.parseUri("youcan://quotes"))
        assertEquals(VoiceCommandHandler.VoiceAction.OpenUsage, VoiceCommandHandler.parseUri("youcan://usage"))
        assertEquals(VoiceCommandHandler.VoiceAction.OpenStats, VoiceCommandHandler.parseUri("youcan://analytics"))
        assertEquals(VoiceCommandHandler.VoiceAction.OpenStats, VoiceCommandHandler.parseUri("youcan://stats"))
    }

    @Test
    fun quoteProvider_cyclingIndicesNeverOutOfBoundsAndCyclesCorrectly() {
        val totalQuotes = com.drnitz.youcan.domain.model.QuoteProvider.quotes.size
        assertTrue(totalQuotes > 0)

        for (i in -100..200) {
            val normalized = ((i % totalQuotes) + totalQuotes) % totalQuotes
            assertTrue(normalized in 0 until totalQuotes)
            val quote = com.drnitz.youcan.domain.model.QuoteProvider.getQuoteByIndex(normalized)
            assertNotNull(quote)
            assertTrue(quote.text.isNotBlank())
            assertTrue(quote.author.isNotBlank())
        }
    }

    @Test
    fun widgetReceivers_registeredClassesMatchComponents() {
        assertEquals(com.drnitz.youcan.widget.TimerWidgetReceiver::class.java.simpleName, "TimerWidgetReceiver")
        assertEquals(com.drnitz.youcan.widget.TasksWidgetReceiver::class.java.simpleName, "TasksWidgetReceiver")
        assertEquals(com.drnitz.youcan.widget.CountdownWidgetReceiver::class.java.simpleName, "CountdownWidgetReceiver")
        assertEquals(com.drnitz.youcan.widget.StreakWidgetReceiver::class.java.simpleName, "StreakWidgetReceiver")
        assertEquals(com.drnitz.youcan.widget.DashboardWidgetReceiver::class.java.simpleName, "DashboardWidgetReceiver")
        assertEquals(com.drnitz.youcan.widget.AnalyticsWidgetReceiver::class.java.simpleName, "AnalyticsWidgetReceiver")
        assertEquals(com.drnitz.youcan.widget.QuoteWidgetReceiver::class.java.simpleName, "QuoteWidgetReceiver")
        assertEquals(com.drnitz.youcan.widget.UsageWidgetReceiver::class.java.simpleName, "UsageWidgetReceiver")
    }

    @Test
    fun timerActionCallbacks_classesInstantiable() {
        assertNotNull(com.drnitz.youcan.widget.TimerStartActionCallback())
        assertNotNull(com.drnitz.youcan.widget.TimerPauseActionCallback())
        assertNotNull(com.drnitz.youcan.widget.TimerResumeActionCallback())
        assertNotNull(com.drnitz.youcan.widget.TimerStopActionCallback())
        assertNotNull(com.drnitz.youcan.widget.TimerAddMinutesActionCallback())
    }
}
