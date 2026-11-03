package com.drnitz.youcan.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WidgetUpdateHelper {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun updateTimerWidgets(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            try { TimerGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { DashboardGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { YoucanGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
        }
    }

    fun updateTaskWidgets(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            try { TasksGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { DashboardGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { YoucanGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
        }
    }

    fun updateCountdownWidgets(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            try { CountdownGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { YoucanGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
        }
    }

    fun updateStreakWidgets(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            try { StreakGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { YoucanGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
        }
    }

    fun updateAnalyticsWidgets(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            try { AnalyticsGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { DashboardGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { YoucanGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
        }
    }

    fun updateAll(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            try { YoucanGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { TimerGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { TasksGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { CountdownGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { StreakGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { DashboardGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { AnalyticsGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { QuoteGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { UsageGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
        }
    }
}
