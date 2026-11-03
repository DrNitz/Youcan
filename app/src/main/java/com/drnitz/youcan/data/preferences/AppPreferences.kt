package com.drnitz.youcan.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "youcan_prefs")

enum class BackgroundType(val id: String, val displayName: String) {
    AURORA("aurora", "Northern Aurora"),
    GENTLE_WAVES("waves", "Gentle Waves"),
    SOFT_RAIN("rain", "Soft Rain"),
    DRIFTING_STARS("stars", "Drifting Stars"),
    AMOLED("amoled", "True Black AMOLED"),
    SOLID_SAGE("solid_sage", "Calm Sage"),
    CUSTOM("custom", "Custom Photo")
}

data class UserPreferences(
    val focusDurationMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val cyclesBeforeLongBreak: Int = 4,
    val dailyStudyGoalMinutes: Int = 120,
    val backgroundType: BackgroundType = BackgroundType.AURORA,
    val customWallpaperUri: String = "",
    val wallpaperBlurRadius: Float = 10f,
    val wallpaperDimAlpha: Float = 0.4f,
    val soundAlertEnabled: Boolean = true,
    val vibrationAlertEnabled: Boolean = true,
    val floatingDockEnabled: Boolean = false,
    val islandPillEnabled: Boolean = false,
    val amoledModeEnabled: Boolean = false,
    val reducedMotionOverride: Boolean = false
)

class AppPreferences(private val context: Context) {

    private object Keys {
        val FOCUS_MINUTES = intPreferencesKey("focus_minutes")
        val SHORT_BREAK_MINUTES = intPreferencesKey("short_break_minutes")
        val LONG_BREAK_MINUTES = intPreferencesKey("long_break_minutes")
        val CYCLES = intPreferencesKey("cycles")
        val DAILY_GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")
        val BACKGROUND_TYPE = stringPreferencesKey("background_type")
        val CUSTOM_WALLPAPER_URI = stringPreferencesKey("custom_wallpaper_uri")
        val WALLPAPER_BLUR = floatPreferencesKey("wallpaper_blur")
        val WALLPAPER_DIM = floatPreferencesKey("wallpaper_dim")
        val SOUND_ALERT = booleanPreferencesKey("sound_alert")
        val VIBRATION_ALERT = booleanPreferencesKey("vibration_alert")
        val FLOATING_DOCK = booleanPreferencesKey("floating_dock")
        val ISLAND_PILL = booleanPreferencesKey("island_pill")
        val AMOLED_MODE = booleanPreferencesKey("amoled_mode")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val bgTypeStr = prefs[Keys.BACKGROUND_TYPE] ?: BackgroundType.AURORA.id
        val bgType = BackgroundType.entries.find { it.id == bgTypeStr } ?: BackgroundType.AURORA
        UserPreferences(
            focusDurationMinutes = prefs[Keys.FOCUS_MINUTES] ?: 25,
            shortBreakMinutes = prefs[Keys.SHORT_BREAK_MINUTES] ?: 5,
            longBreakMinutes = prefs[Keys.LONG_BREAK_MINUTES] ?: 15,
            cyclesBeforeLongBreak = prefs[Keys.CYCLES] ?: 4,
            dailyStudyGoalMinutes = prefs[Keys.DAILY_GOAL_MINUTES] ?: 120,
            backgroundType = bgType,
            customWallpaperUri = prefs[Keys.CUSTOM_WALLPAPER_URI] ?: "",
            wallpaperBlurRadius = prefs[Keys.WALLPAPER_BLUR] ?: 10f,
            wallpaperDimAlpha = prefs[Keys.WALLPAPER_DIM] ?: 0.4f,
            soundAlertEnabled = prefs[Keys.SOUND_ALERT] ?: true,
            vibrationAlertEnabled = prefs[Keys.VIBRATION_ALERT] ?: true,
            floatingDockEnabled = prefs[Keys.FLOATING_DOCK] ?: false,
            islandPillEnabled = prefs[Keys.ISLAND_PILL] ?: false,
            amoledModeEnabled = prefs[Keys.AMOLED_MODE] ?: false,
            reducedMotionOverride = prefs[Keys.REDUCED_MOTION] ?: false
        )
    }

    suspend fun updateTimerDurations(focus: Int, shortBreak: Int, longBreak: Int, cycles: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FOCUS_MINUTES] = focus
            prefs[Keys.SHORT_BREAK_MINUTES] = shortBreak
            prefs[Keys.LONG_BREAK_MINUTES] = longBreak
            prefs[Keys.CYCLES] = cycles
        }
        runCatching { com.drnitz.youcan.widget.WidgetUpdateHelper.updateAll(context) }
    }

    suspend fun updateDailyGoal(goalMinutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DAILY_GOAL_MINUTES] = goalMinutes
        }
        runCatching { com.drnitz.youcan.widget.WidgetUpdateHelper.updateAll(context) }
    }

    suspend fun updateBackground(
        type: BackgroundType,
        customUri: String = "",
        blur: Float = 10f,
        dim: Float = 0.4f
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BACKGROUND_TYPE] = type.id
            if (customUri.isNotEmpty()) prefs[Keys.CUSTOM_WALLPAPER_URI] = customUri
            prefs[Keys.WALLPAPER_BLUR] = blur
            prefs[Keys.WALLPAPER_DIM] = dim
        }
    }

    suspend fun updateAlerts(sound: Boolean, vibration: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SOUND_ALERT] = sound
            prefs[Keys.VIBRATION_ALERT] = vibration
        }
    }

    suspend fun setFloatingDockEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FLOATING_DOCK] = enabled
        }
    }

    suspend fun setIslandPillEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ISLAND_PILL] = enabled
        }
    }

    suspend fun setAmoledMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AMOLED_MODE] = enabled
        }
        runCatching { com.drnitz.youcan.widget.WidgetUpdateHelper.updateAll(context) }
    }

    suspend fun setReducedMotion(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.REDUCED_MOTION] = enabled
        }
    }
}
