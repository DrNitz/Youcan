package com.drnitz.youcan.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.drnitz.youcan.data.local.entity.StudyReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: StudyReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: StudyReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: StudyReminderEntity)

    @Query("SELECT * FROM study_reminders ORDER BY hour ASC, minute ASC")
    fun getAllReminders(): Flow<List<StudyReminderEntity>>

    @Query("SELECT * FROM study_reminders WHERE isEnabled = 1")
    suspend fun getEnabledRemindersSync(): List<StudyReminderEntity>

    @Query("SELECT * FROM study_reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: Long): StudyReminderEntity?
}
