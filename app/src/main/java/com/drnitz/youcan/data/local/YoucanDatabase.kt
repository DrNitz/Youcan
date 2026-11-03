package com.drnitz.youcan.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.drnitz.youcan.data.local.dao.CountdownDao
import com.drnitz.youcan.data.local.dao.DailyPlanDao
import com.drnitz.youcan.data.local.dao.DistractingAppDao
import com.drnitz.youcan.data.local.dao.ReminderDao
import com.drnitz.youcan.data.local.dao.SessionDao
import com.drnitz.youcan.data.local.dao.StreakFreezeDao
import com.drnitz.youcan.data.local.entity.DailyPlanEntity
import com.drnitz.youcan.data.local.entity.DistractingAppEntity
import com.drnitz.youcan.data.local.entity.ExamCountdownEntity
import com.drnitz.youcan.data.local.entity.FocusSessionEntity
import com.drnitz.youcan.data.local.entity.StudyReminderEntity
import com.drnitz.youcan.data.local.entity.StreakFreezeEntity

@Database(
    entities = [
        FocusSessionEntity::class,
        ExamCountdownEntity::class,
        StudyReminderEntity::class,
        DailyPlanEntity::class,
        DistractingAppEntity::class,
        StreakFreezeEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class YoucanDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun countdownDao(): CountdownDao
    abstract fun reminderDao(): ReminderDao
    abstract fun dailyPlanDao(): DailyPlanDao
    abstract fun distractingAppDao(): DistractingAppDao
    abstract fun streakFreezeDao(): StreakFreezeDao

    companion object {
        @Volatile
        private var INSTANCE: YoucanDatabase? = null

        fun getInstance(context: Context): YoucanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    YoucanDatabase::class.java,
                    "youcan_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
