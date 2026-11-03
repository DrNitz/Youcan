package com.drnitz.youcan.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.drnitz.youcan.data.local.entity.ExamCountdownEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CountdownDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountdown(countdown: ExamCountdownEntity): Long

    @Update
    suspend fun updateCountdown(countdown: ExamCountdownEntity)

    @Delete
    suspend fun deleteCountdown(countdown: ExamCountdownEntity)

    @Query("SELECT * FROM exam_countdowns ORDER BY targetEpochMillis ASC")
    fun getAllCountdowns(): Flow<List<ExamCountdownEntity>>

    @Query("SELECT * FROM exam_countdowns WHERE targetEpochMillis >= :nowMillis ORDER BY targetEpochMillis ASC LIMIT 1")
    fun getNearestUpcomingCountdown(nowMillis: Long): Flow<ExamCountdownEntity?>

    @Query("SELECT * FROM exam_countdowns WHERE targetEpochMillis >= :nowMillis ORDER BY targetEpochMillis ASC LIMIT 1")
    suspend fun getNearestUpcomingCountdownSync(nowMillis: Long): ExamCountdownEntity?

    @Query("SELECT * FROM exam_countdowns WHERE targetEpochMillis >= :nowMillis ORDER BY targetEpochMillis ASC")
    suspend fun getAllUpcomingCountdownsSync(nowMillis: Long): List<ExamCountdownEntity>

    @Query("SELECT * FROM exam_countdowns WHERE id = :id LIMIT 1")
    suspend fun getCountdownById(id: Long): ExamCountdownEntity?
}
