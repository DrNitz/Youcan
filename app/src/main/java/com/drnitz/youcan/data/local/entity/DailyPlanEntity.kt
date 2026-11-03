package com.drnitz.youcan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_plans")
data class DailyPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val dateEpochDay: Long, // LocalDate.toEpochDay()
    val task: String,
    val isCompleted: Boolean = false,
    val targetMinutes: Int = 0,
    val hasReminder: Boolean = false,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null
)
