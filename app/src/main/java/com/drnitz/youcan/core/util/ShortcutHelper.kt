package com.drnitz.youcan.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.drnitz.youcan.MainActivity
import com.drnitz.youcan.R

object ShortcutHelper {

    enum class ShortcutTarget(
        val id: String,
        val labelShort: String,
        val labelLong: String,
        val deepLinkUri: String
    ) {
        START_TIMER(
            id = "shortcut_start_focus",
            labelShort = "Start Focus",
            labelLong = "Start 25m Focus Session",
            deepLinkUri = "youcan://timer/start?minutes=25&subject=General%20Study"
        ),
        ADD_TASK(
            id = "shortcut_add_task",
            labelShort = "Add Task",
            labelLong = "Add Study Task with Reminder",
            deepLinkUri = "youcan://tasks/add"
        ),
        STREAKS(
            id = "shortcut_streaks",
            labelShort = "Streaks",
            labelLong = "Study Consistency Streaks",
            deepLinkUri = "youcan://streaks"
        ),
        COUNTDOWNS(
            id = "shortcut_countdowns",
            labelShort = "Milestones",
            labelLong = "Upcoming Exam Countdowns",
            deepLinkUri = "youcan://countdowns"
        )
    }

    fun initDynamicShortcuts(context: Context) {
        try {
            val shortcuts = ShortcutTarget.entries.map { target ->
                createShortcutInfo(context, target)
            }
            ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
        } catch (_: Exception) {}
    }

    fun isPinningSupported(context: Context): Boolean {
        return ShortcutManagerCompat.isRequestPinShortcutSupported(context)
    }

    fun requestPinShortcut(context: Context, target: ShortcutTarget) {
        if (!isPinningSupported(context)) {
            Toast.makeText(context, "Home screen shortcut pinning is not supported on this launcher", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val shortcut = createShortcutInfo(context, target)
            val pinned = ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
            if (pinned) {
                Toast.makeText(context, "Adding \"${target.labelShort}\" shortcut to home screen...", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not add shortcut: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createShortcutInfo(context: Context, target: ShortcutTarget): ShortcutInfoCompat {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(target.deepLinkUri)).apply {
            setClass(context, MainActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        return ShortcutInfoCompat.Builder(context, target.id)
            .setShortLabel(target.labelShort)
            .setLongLabel(target.labelLong)
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_notification))
            .setIntent(intent)
            .build()
    }
}
