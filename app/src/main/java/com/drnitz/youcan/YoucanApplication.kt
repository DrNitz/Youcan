package com.drnitz.youcan

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.drnitz.youcan.data.local.YoucanDatabase
import com.drnitz.youcan.data.preferences.AppPreferences
import com.drnitz.youcan.core.sound.SoundEffectManager

class YoucanApplication : Application() {

    lateinit var database: YoucanDatabase
        private set

    lateinit var preferences: AppPreferences
        private set

    lateinit var soundEffectManager: SoundEffectManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = YoucanDatabase.getInstance(this)
        preferences = AppPreferences(this)
        soundEffectManager = SoundEffectManager(this)
        com.drnitz.youcan.service.timer.TimerStateManager.init(this)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Focus Timer Channel
            val timerChannel = NotificationChannel(
                CHANNEL_TIMER_ID,
                getString(R.string.channel_timer_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_timer_desc)
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }

            // Phase End Alert Channel
            val alertChannel = NotificationChannel(
                CHANNEL_ALERT_ID,
                "Timer Phase Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when a focus or break interval completes"
                enableVibration(true)
            }

            // Study Reminders Channel
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                getString(R.string.channel_reminders_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_reminders_desc)
                enableVibration(true)
            }

            // Overlay Service Channel
            val overlayChannel = NotificationChannel(
                CHANNEL_OVERLAY_ID,
                getString(R.string.channel_overlay_name),
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = getString(R.string.channel_overlay_desc)
                setShowBadge(false)
            }

            notificationManager.createNotificationChannels(
                listOf(timerChannel, alertChannel, reminderChannel, overlayChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_TIMER_ID = "youcan_timer_channel"
        const val CHANNEL_ALERT_ID = "youcan_alert_channel"
        const val CHANNEL_REMINDERS_ID = "youcan_reminders_channel"
        const val CHANNEL_OVERLAY_ID = "youcan_overlay_channel"

        lateinit var instance: YoucanApplication
            private set
    }
}
