package com.drnitz.youcan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "distracting_apps")
data class DistractingAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isDistracting: Boolean = true
)

@Entity(tableName = "streak_freezes")
data class StreakFreezeEntity(
    @PrimaryKey
    val dateEpochDay: Long,
    val usedAt: Long = System.currentTimeMillis()
)
