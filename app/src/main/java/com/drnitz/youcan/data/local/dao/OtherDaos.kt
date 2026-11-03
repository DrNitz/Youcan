package com.drnitz.youcan.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.drnitz.youcan.data.local.entity.DailyPlanEntity
import com.drnitz.youcan.data.local.entity.DistractingAppEntity
import com.drnitz.youcan.data.local.entity.StreakFreezeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: DailyPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<DailyPlanEntity>)

    @Update
    suspend fun updatePlan(plan: DailyPlanEntity)

    @Delete
    suspend fun deletePlan(plan: DailyPlanEntity)

    @Query("SELECT * FROM daily_plans WHERE dateEpochDay = :epochDay ORDER BY id ASC")
    fun getPlansForDay(epochDay: Long): Flow<List<DailyPlanEntity>>

    @Query("SELECT * FROM daily_plans WHERE dateEpochDay = :epochDay ORDER BY id ASC")
    suspend fun getPlansForDaySync(epochDay: Long): List<DailyPlanEntity>

    @Query("SELECT * FROM daily_plans WHERE dateEpochDay >= :fromEpochDay ORDER BY dateEpochDay ASC, id ASC")
    fun getAllUpcomingTasks(fromEpochDay: Long): Flow<List<DailyPlanEntity>>

    @Query("SELECT * FROM daily_plans WHERE hasReminder = 1 AND isCompleted = 0")
    suspend fun getAllTasksWithReminderSync(): List<DailyPlanEntity>

    @Query("SELECT * FROM daily_plans WHERE id = :id LIMIT 1")
    suspend fun getPlanById(id: Long): DailyPlanEntity?
}

@Dao
interface DistractingAppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(app: DistractingAppEntity)

    @Delete
    suspend fun delete(app: DistractingAppEntity)

    @Query("SELECT * FROM distracting_apps ORDER BY appName ASC")
    fun getAllApps(): Flow<List<DistractingAppEntity>>

    @Query("SELECT * FROM distracting_apps")
    suspend fun getAllAppsSync(): List<DistractingAppEntity>

    @Query("SELECT isDistracting FROM distracting_apps WHERE packageName = :pkg LIMIT 1")
    suspend fun isDistracting(pkg: String): Boolean?
}

@Dao
interface StreakFreezeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFreeze(freeze: StreakFreezeEntity)

    @Query("SELECT * FROM streak_freezes ORDER BY dateEpochDay DESC")
    fun getAllFreezes(): Flow<List<StreakFreezeEntity>>

    @Query("SELECT * FROM streak_freezes")
    suspend fun getAllFreezesSync(): List<StreakFreezeEntity>

    @Query("SELECT COUNT(*) FROM streak_freezes WHERE dateEpochDay = :epochDay")
    suspend fun isFrozen(epochDay: Long): Int
}
