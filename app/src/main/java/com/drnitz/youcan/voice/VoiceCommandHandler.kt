package com.drnitz.youcan.voice

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.drnitz.youcan.service.timer.FocusTimerService

object VoiceCommandHandler {

    sealed class VoiceAction {
        data class StartTimer(val minutes: Int = 25, val subject: String = "General Study") : VoiceAction()
        object PauseTimer : VoiceAction()
        object ResumeTimer : VoiceAction()
        object StopTimer : VoiceAction()
        object OpenTimer : VoiceAction()
        object AddCountdown : VoiceAction()
        object OpenCountdowns : VoiceAction()
        object AddReminder : VoiceAction()
        object OpenTasks : VoiceAction()
        object AddTask : VoiceAction()
        object OpenStreaks : VoiceAction()
        object OpenUsage : VoiceAction()
        object OpenStats : VoiceAction()
        object None : VoiceAction()
    }

    fun parseUri(uriString: String?): VoiceAction {
        if (uriString.isNullOrBlank()) return VoiceAction.None
        val encoded = uriString.replace(" ", "%20")
        val uri = try { java.net.URI(encoded) } catch (_: Exception) { return VoiceAction.None }
        if (uri.scheme != "youcan") return VoiceAction.None

        val host = uri.host ?: ""
        val path = uri.path ?: ""
        val query = uri.query ?: ""
        val queryParams = query.split("&").mapNotNull {
            val parts = it.split("=")
            if (parts.size == 2) parts[0] to parts[1] else null
        }.toMap()

        return when {
            host == "timer" && (path.contains("start") || queryParams.containsKey("start")) -> {
                val minutes = queryParams["minutes"]?.toIntOrNull() ?: 25
                val subject = queryParams["subject"]?.replace("%20", " ")?.replace("+", " ") ?: "General Study"
                VoiceAction.StartTimer(minutes, subject)
            }
            host == "timer" && path.contains("pause") -> VoiceAction.PauseTimer
            host == "timer" && path.contains("resume") -> VoiceAction.ResumeTimer
            host == "timer" && path.contains("stop") -> VoiceAction.StopTimer
            host == "timer" -> VoiceAction.OpenTimer
            (host == "countdown" || host == "countdowns") && path.contains("add") -> VoiceAction.AddCountdown
            host == "countdown" || host == "countdowns" -> VoiceAction.OpenCountdowns
            host == "reminder" && path.contains("add") -> VoiceAction.AddReminder
            (host == "reminders" || host == "task" || host == "tasks") && path.contains("add") -> VoiceAction.AddTask
            host == "reminder" || host == "reminders" || host == "tasks" || host == "task" -> VoiceAction.OpenTasks
            host == "streak" || host == "streaks" -> VoiceAction.OpenStreaks
            host == "usage" -> VoiceAction.OpenUsage
            host == "stats" || host == "analytics" -> VoiceAction.OpenStats
            else -> VoiceAction.None
        }
    }

    fun parseIntent(intent: Intent?): VoiceAction {
        return parseUri(intent?.dataString)
    }

    fun executeAction(context: Context, action: VoiceAction) {
        when (action) {
            is VoiceAction.StartTimer -> {
                FocusTimerService.startTimer(context, action.subject)
            }
            is VoiceAction.PauseTimer -> {
                FocusTimerService.pauseTimer(context)
            }
            is VoiceAction.ResumeTimer -> {
                FocusTimerService.resumeTimer(context)
            }
            is VoiceAction.StopTimer -> {
                FocusTimerService.stopTimer(context)
            }
            else -> {}
        }
    }
}
