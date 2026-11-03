package com.drnitz.youcan.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.drnitz.youcan.data.local.dao.ReminderDao
import com.drnitz.youcan.data.local.entity.StudyReminderEntity
import com.drnitz.youcan.domain.model.StudyReminderItem
import com.drnitz.youcan.receiver.ExactAlarmReceiver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class ReminderRepository(
    private val context: Context,
    private val reminderDao: ReminderDao
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val allReminders: Flow<List<StudyReminderItem>> = reminderDao.getAllReminders().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun addReminder(
        title: String,
        hour: Int,
        minute: Int,
        daysOfWeek: List<Int>,
        oneOffDate: LocalDate? = null
    ): Long {
        val entity = StudyReminderEntity(
            title = title,
            hour = hour,
            minute = minute,
            daysOfWeek = daysOfWeek.joinToString(","),
            oneOffDateEpochDay = oneOffDate?.toEpochDay(),
            isEnabled = true
        )
        val id = reminderDao.insertReminder(entity)
        val savedItem = entity.copy(id = id).toDomain()
        scheduleAlarm(savedItem)
        return id
    }

    suspend fun updateReminder(item: StudyReminderItem) {
        reminderDao.updateReminder(item.toEntity())
        if (item.isEnabled) {
            scheduleAlarm(item)
        } else {
            cancelAlarm(item)
        }
    }

    suspend fun deleteReminder(item: StudyReminderItem) {
        cancelAlarm(item)
        reminderDao.deleteReminder(item.toEntity())
    }

    suspend fun rescheduleAllEnabledReminders() {
        val enabled = reminderDao.getEnabledRemindersSync()
        for (entity in enabled) {
            scheduleAlarm(entity.toDomain())
        }
    }

    fun scheduleAlarm(item: StudyReminderItem) {
        if (!item.isEnabled) return

        val nextTriggerMillis = calculateNextTriggerMillis(item) ?: return
        val intent = Intent(context, ExactAlarmReceiver::class.java).apply {
            putExtra("reminder_id", item.id)
            putExtra("reminder_title", item.title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // Degrade gracefully if exact alarm permission was revoked by user
        }
    }

    fun cancelAlarm(item: StudyReminderItem) {
        val intent = Intent(context, ExactAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun calculateNextTriggerMillis(item: StudyReminderItem): Long? {
        val now = LocalDateTime.now()
        val zone = ZoneId.systemDefault()

        if (item.oneOffDate != null) {
            val triggerTime = item.oneOffDate.atTime(item.hour, item.minute)
            if (triggerTime.isAfter(now)) {
                return triggerTime.atZone(zone).toInstant().toEpochMilli()
            }
            return null
        }

        if (item.daysOfWeek.isEmpty()) {
            // One-off for today or tomorrow
            var triggerTime = now.toLocalDate().atTime(item.hour, item.minute)
            if (!triggerTime.isAfter(now)) {
                triggerTime = triggerTime.plusDays(1)
            }
            return triggerTime.atZone(zone).toInstant().toEpochMilli()
        }

        // Recurring weekly on specific days
        val targetDays = item.daysOfWeek.map { DayOfWeek.of(it) }.toSet()
        var checkDate = now.toLocalDate()
        for (i in 0..7) {
            if (targetDays.contains(checkDate.dayOfWeek)) {
                val candidateTime = checkDate.atTime(item.hour, item.minute)
                if (candidateTime.isAfter(now)) {
                    return candidateTime.atZone(zone).toInstant().toEpochMilli()
                }
            }
            checkDate = checkDate.plusDays(1)
        }
        return null
    }

    private fun StudyReminderEntity.toDomain(): StudyReminderItem {
        val days = daysOfWeek.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
        val oneOff = oneOffDateEpochDay?.let { LocalDate.ofEpochDay(it) }
        return StudyReminderItem(
            id = id,
            title = title,
            hour = hour,
            minute = minute,
            daysOfWeek = days,
            oneOffDate = oneOff,
            isEnabled = isEnabled
        )
    }

    private fun StudyReminderItem.toEntity(): StudyReminderEntity {
        return StudyReminderEntity(
            id = id,
            title = title,
            hour = hour,
            minute = minute,
            daysOfWeek = daysOfWeek.joinToString(","),
            oneOffDateEpochDay = oneOffDate?.toEpochDay(),
            isEnabled = isEnabled
        )
    }
}
