package com.drnitz.youcan.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.drnitz.youcan.data.local.dao.DailyPlanDao
import com.drnitz.youcan.data.local.entity.DailyPlanEntity
import com.drnitz.youcan.domain.model.TaskItem
import com.drnitz.youcan.receiver.ExactAlarmReceiver
import com.drnitz.youcan.widget.WidgetUpdateHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class PlanRepository(
    private val context: Context,
    private val dailyPlanDao: DailyPlanDao
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun getPlansForDay(date: LocalDate): Flow<List<TaskItem>> {
        return dailyPlanDao.getPlansForDay(date.toEpochDay()).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getAllUpcomingTasks(fromDate: LocalDate = LocalDate.now()): Flow<List<TaskItem>> {
        return dailyPlanDao.getAllUpcomingTasks(fromDate.toEpochDay()).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun addTask(
        date: LocalDate,
        task: String,
        targetMinutes: Int = 0,
        hasReminder: Boolean = false,
        reminderHour: Int? = null,
        reminderMinute: Int? = null
    ): Long {
        val entity = DailyPlanEntity(
            dateEpochDay = date.toEpochDay(),
            task = task,
            isCompleted = false,
            targetMinutes = targetMinutes,
            hasReminder = hasReminder,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute
        )
        val id = dailyPlanDao.insertPlan(entity)
        val saved = entity.copy(id = id).toDomain()
        if (hasReminder) {
            scheduleAlarm(saved)
        }
        WidgetUpdateHelper.updateAll(context)
        return id
    }

    suspend fun addPlan(date: LocalDate, task: String, targetMinutes: Int = 0): Long {
        return addTask(date, task, targetMinutes)
    }

    suspend fun updateTask(item: TaskItem) {
        dailyPlanDao.updatePlan(item.toEntity())
        if (item.hasReminder && !item.isCompleted) {
            scheduleAlarm(item)
        } else {
            cancelAlarm(item)
        }
        WidgetUpdateHelper.updateAll(context)
    }

    suspend fun togglePlanCompletion(item: TaskItem) {
        val updated = item.copy(isCompleted = !item.isCompleted)
        dailyPlanDao.updatePlan(updated.toEntity())
        if (updated.isCompleted) {
            cancelAlarm(item)
        } else if (updated.hasReminder) {
            scheduleAlarm(updated)
        }
        WidgetUpdateHelper.updateAll(context)
    }

    suspend fun deleteTask(item: TaskItem) {
        cancelAlarm(item)
        dailyPlanDao.deletePlan(item.toEntity())
        WidgetUpdateHelper.updateAll(context)
    }

    suspend fun deletePlan(item: TaskItem) {
        deleteTask(item)
    }

    suspend fun copyPlansToDate(fromDate: LocalDate, toDate: LocalDate) {
        val sourcePlans = dailyPlanDao.getPlansForDaySync(fromDate.toEpochDay())
        val newEntities = sourcePlans.map {
            DailyPlanEntity(
                dateEpochDay = toDate.toEpochDay(),
                task = it.task,
                isCompleted = false,
                targetMinutes = it.targetMinutes,
                hasReminder = it.hasReminder,
                reminderHour = it.reminderHour,
                reminderMinute = it.reminderMinute
            )
        }
        if (newEntities.isNotEmpty()) {
            dailyPlanDao.insertPlans(newEntities)
            WidgetUpdateHelper.updateAll(context)
        }
    }

    suspend fun rescheduleAllReminders() {
        val tasks = dailyPlanDao.getAllTasksWithReminderSync()
        for (entity in tasks) {
            scheduleAlarm(entity.toDomain())
        }
    }

    fun scheduleAlarm(item: TaskItem) {
        if (!item.hasReminder || item.reminderHour == null || item.reminderMinute == null) return
        val ldt = LocalDateTime.of(item.date, LocalTime.of(item.reminderHour, item.reminderMinute))
        val triggerMillis = ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (triggerMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, ExactAlarmReceiver::class.java).apply {
            putExtra("reminder_id", item.id)
            putExtra("reminder_title", item.task)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (item.id + 100000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {}
    }

    fun cancelAlarm(item: TaskItem) {
        val intent = Intent(context, ExactAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (item.id + 100000).toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun DailyPlanEntity.toDomain(): TaskItem {
        return TaskItem(
            id = id,
            date = LocalDate.ofEpochDay(dateEpochDay),
            task = task,
            isCompleted = isCompleted,
            targetMinutes = targetMinutes,
            hasReminder = hasReminder,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute
        )
    }

    private fun TaskItem.toEntity(): DailyPlanEntity {
        return DailyPlanEntity(
            id = id,
            dateEpochDay = date.toEpochDay(),
            task = task,
            isCompleted = isCompleted,
            targetMinutes = targetMinutes,
            hasReminder = hasReminder,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute
        )
    }
}

typealias TaskRepository = PlanRepository
