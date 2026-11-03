package com.drnitz.youcan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_reminders")
data class StudyReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val hour: Int,
    val minute: Int,
    val daysOfWeek: String = "", // Comma-separated Java DayOfWeek numbers (1=Mon..7=Sun), empty if one-off
    val oneOffDateEpochDay: Long? = null,
    val isEnabled: Boolean = true
)
