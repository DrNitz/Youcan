package com.drnitz.youcan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_countdowns")
data class ExamCountdownEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val targetEpochMillis: Long,
    val subject: String = "",
    val reminderOffsetsMinutes: String = "1440,60" // e.g. 1 day and 1 hour before
)
