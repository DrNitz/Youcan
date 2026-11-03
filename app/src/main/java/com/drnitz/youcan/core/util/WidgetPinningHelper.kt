package com.drnitz.youcan.core.util

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.glance.appwidget.GlanceAppWidgetReceiver

object WidgetPinningHelper {

    fun isPinningSupported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        val appWidgetManager = context.getSystemService(AppWidgetManager::class.java) ?: return false
        return appWidgetManager.isRequestPinAppWidgetSupported
    }

    fun pinWidget(
        context: Context,
        receiverClass: Class<out GlanceAppWidgetReceiver>,
        widgetName: String = "Widget"
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Toast.makeText(context, "Pinning widgets requires Android 8.0+", Toast.LENGTH_SHORT).show()
            return false
        }

        val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
        if (appWidgetManager == null || !appWidgetManager.isRequestPinAppWidgetSupported) {
            Toast.makeText(
                context,
                "Your launcher does not support direct widget pinning. Please add it from your home screen.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        val componentName = ComponentName(context, receiverClass)
        return try {
            val success = appWidgetManager.requestPinAppWidget(componentName, null, null)
            if (success) {
                Toast.makeText(context, "Adding $widgetName to Home Screen...", Toast.LENGTH_SHORT).show()
            }
            success
        } catch (e: Exception) {
            Toast.makeText(context, "Could not pin widget: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
