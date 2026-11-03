package com.drnitz.youcan.core.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class SoundEffectManager(private val context: Context) {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playPhaseEndAlert(soundEnabled: Boolean = true, vibrationEnabled: Boolean = true) {
        if (vibrationEnabled) {
            triggerVibration()
        }
        if (soundEnabled) {
            playTone()
        }
    }

    private fun triggerVibration() {
        try {
            vibrator?.let { v ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = longArrayOf(0, 200, 100, 250, 100, 400)
                    val amplitudes = intArrayOf(0, 180, 0, 220, 0, 255)
                    v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(longArrayOf(0, 200, 100, 250, 100, 400), -1)
                }
            }
        } catch (_: Exception) {
            // Graceful degrade if vibrator permission or hardware is unavailable
        }
    }

    private fun playTone() {
        try {
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, notificationUri)
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ringtone.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                }
                ringtone.play()
            } else {
                // Fallback to ToneGenerator
                val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 80)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
            }
        } catch (_: Exception) {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 80)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
            } catch (_: Exception) {
                // Audio unavailable
            }
        }
    }
}
