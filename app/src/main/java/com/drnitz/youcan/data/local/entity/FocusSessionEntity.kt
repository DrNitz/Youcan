package com.drnitz.youcan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Int,
    val subject: String,
    val note: String = "",
    val isCompleted: Boolean = true,
    val isManual: Boolean = false
)
