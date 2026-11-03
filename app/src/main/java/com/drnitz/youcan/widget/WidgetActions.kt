package com.drnitz.youcan.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.drnitz.youcan.YoucanApplication
import com.drnitz.youcan.domain.model.QuoteProvider
import com.drnitz.youcan.service.timer.FocusTimerService

val TaskIdActionKey = ActionParameters.Key<Long>("task_id")
val GlanceKeyActionKey = ActionParameters.Key<String>("glance_key")
val UriActionKey = ActionParameters.Key<String>("deep_link_uri")

class OpenDeepLinkActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val uriStr = parameters[UriActionKey] ?: "youcan://timer"
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uriStr)).apply {
            setClass(context, com.drnitz.youcan.MainActivity::class.java)
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    }
}

class TimerStartActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        FocusTimerService.startTimer(context)
    }
}

class TimerPauseActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        FocusTimerService.pauseTimer(context)
    }
}

class TimerResumeActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        FocusTimerService.resumeTimer(context)
    }
}

class TimerStopActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        FocusTimerService.stopTimer(context)
    }
}

class TimerAddMinutesActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        FocusTimerService.addMinutes(context, 5)
    }
}

class TimerSkipActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        FocusTimerService.skipPhase(context)
    }
}

class ToggleTaskActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val taskId = parameters[TaskIdActionKey] ?: return
        val app = context.applicationContext as? YoucanApplication ?: return
        val repo = com.drnitz.youcan.data.repository.PlanRepository(context, app.database.dailyPlanDao())
        repo.togglePlanCompletion(taskId)
        WidgetUpdateHelper.updateTaskWidgets(context)
    }
}

class CycleQuoteActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val glanceKey = parameters[GlanceKeyActionKey] ?: glanceId.toString()
        val sp = context.getSharedPreferences("youcan_widget_prefs", Context.MODE_PRIVATE)
        val today = java.time.LocalDate.now()
        val defaultIndex = ((today.toEpochDay() % QuoteProvider.quotes.size).toInt() + QuoteProvider.quotes.size) % QuoteProvider.quotes.size
        val currentIndex = sp.getInt("quote_index_$glanceKey", sp.getInt("quote_index_global", defaultIndex))
        val nextIndex = (currentIndex + 1) % QuoteProvider.quotes.size
        sp.edit()
            .putInt("quote_index_$glanceKey", nextIndex)
            .putInt("quote_index_global", nextIndex)
            .apply()
        WidgetUpdateHelper.updateAll(context)
    }
}

class CycleCountdownActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val glanceKey = parameters[GlanceKeyActionKey] ?: glanceId.toString()
        val sp = context.getSharedPreferences("youcan_widget_prefs", Context.MODE_PRIVATE)
        val currentIndex = sp.getInt("countdown_index_$glanceKey", 0)
        val app = context.applicationContext as? YoucanApplication
        val count = try {
            app?.database?.countdownDao()?.getAllUpcomingCountdownsSync(System.currentTimeMillis())?.size ?: 1
        } catch (_: Exception) { 1 }
        val nextIndex = if (count > 0) (currentIndex + 1) % count else 0
        sp.edit().putInt("countdown_index_$glanceKey", nextIndex).apply()
        WidgetUpdateHelper.updateCountdownWidgets(context)
    }
}

class QuickLogStreakActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val app = context.applicationContext as? YoucanApplication ?: return
        val now = System.currentTimeMillis()
        val thirtyMins = 30 * 60
        try {
            app.database.sessionDao().insertSession(
                com.drnitz.youcan.data.local.entity.FocusSessionEntity(
                    startTime = now - (thirtyMins * 1000L),
                    endTime = now,
                    durationSeconds = thirtyMins,
                    subject = "General Study",
                    note = "Quick logged from widget",
                    isCompleted = true,
                    isManual = true
                )
            )
            WidgetUpdateHelper.updateStreakWidgets(context)
        } catch (_: Exception) {}
    }
}
