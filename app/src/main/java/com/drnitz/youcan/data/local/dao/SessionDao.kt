package com.drnitz.youcan.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.drnitz.youcan.data.local.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Update
    suspend fun updateSession(session: FocusSessionEntity)

    @Delete
    suspend fun deleteSession(session: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE startTime >= :startMillis AND endTime <= :endMillis ORDER BY startTime DESC")
    fun getSessionsBetween(startMillis: Long, endMillis: Long): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE startTime >= :startMillis AND endTime <= :endMillis")
    suspend fun getSessionsBetweenSync(startMillis: Long, endMillis: Long): List<FocusSessionEntity>

    @Query("SELECT DISTINCT subject FROM focus_sessions WHERE subject != '' ORDER BY subject ASC")
    fun getDistinctSubjects(): Flow<List<String>>

    @Query("SELECT SUM(durationSeconds) FROM focus_sessions WHERE startTime >= :startMillis AND endTime <= :endMillis")
    fun getTotalStudySeconds(startMillis: Long, endMillis: Long): Flow<Long?>

    @Query("SELECT SUM(durationSeconds) FROM focus_sessions WHERE startTime >= :startMillis AND endTime <= :endMillis")
    suspend fun getTotalStudySecondsSync(startMillis: Long, endMillis: Long): Long?

    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    suspend fun getAllSessionsSync(): List<FocusSessionEntity>
}
