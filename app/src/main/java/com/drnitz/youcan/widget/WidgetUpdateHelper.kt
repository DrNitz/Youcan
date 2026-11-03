package com.drnitz.youcan.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WidgetUpdateHelper {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun updateAll(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            try { YoucanGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { TimerGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { TasksGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { CountdownGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { StreakGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
            try { DashboardGlanceWidget().updateAll(appContext) } catch (_: Exception) {}
        }
    }
}
