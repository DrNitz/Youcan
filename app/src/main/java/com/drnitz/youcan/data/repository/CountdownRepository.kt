package com.drnitz.youcan.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.drnitz.youcan.data.local.dao.CountdownDao
import com.drnitz.youcan.data.local.entity.ExamCountdownEntity
import com.drnitz.youcan.domain.model.CountdownItem
import com.drnitz.youcan.receiver.ExactAlarmReceiver
import com.drnitz.youcan.widget.WidgetUpdateHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CountdownRepository(
    private val context: Context,
    private val countdownDao: CountdownDao
) {

    constructor(countdownDao: CountdownDao) : this(
        com.drnitz.youcan.YoucanApplication.instance,
        countdownDao
    )

    private val alarmManager by lazy { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }

    val allCountdowns: Flow<List<CountdownItem>> = countdownDao.getAllCountdowns().map { list ->
        list.map { it.toDomain() }
    }

    fun observeNearestCountdown(): Flow<CountdownItem?> {
        return countdownDao.getNearestUpcomingCountdown(System.currentTimeMillis()).map { it?.toDomain() }
    }

    suspend fun getNearestCountdownSync(): CountdownItem? {
        return countdownDao.getNearestUpcomingCountdownSync(System.currentTimeMillis())?.toDomain()
    }

    suspend fun addCountdown(title: String, targetEpochMillis: Long, subject: String, offsets: List<Int>): Long {
        val entity = ExamCountdownEntity(
            title = title,
            targetEpochMillis = targetEpochMillis,
            subject = subject,
            reminderOffsetsMinutes = offsets.joinToString(",")
        )
        val id = countdownDao.insertCountdown(entity)
        val savedItem = entity.copy(id = id).toDomain()
        scheduleCountdownAlarms(savedItem)
        runCatching { WidgetUpdateHelper.updateAll(com.drnitz.youcan.YoucanApplication.instance) }
        return id
    }

    suspend fun updateCountdown(item: CountdownItem) {
        countdownDao.updateCountdown(item.toEntity())
        scheduleCountdownAlarms(item)
        runCatching { WidgetUpdateHelper.updateAll(com.drnitz.youcan.YoucanApplication.instance) }
    }

    suspend fun deleteCountdown(item: CountdownItem) {
        cancelCountdownAlarms(item)
        countdownDao.deleteCountdown(item.toEntity())
        runCatching { WidgetUpdateHelper.updateAll(com.drnitz.youcan.YoucanApplication.instance) }
    }

    suspend fun rescheduleAllCountdowns() {
        val upcoming = countdownDao.getAllUpcomingCountdownsSync(System.currentTimeMillis())
        for (entity in upcoming) {
            scheduleCountdownAlarms(entity.toDomain())
        }
    }

    fun scheduleCountdownAlarms(item: CountdownItem) {
        val now = System.currentTimeMillis()
        for (offset in item.reminderOffsetsMinutes) {
            val triggerMillis = item.targetEpochMillis - (offset * 60 * 1000L)
            if (triggerMillis > now) {
                val intent = Intent(context, ExactAlarmReceiver::class.java).apply {
                    putExtra("reminder_id", (item.id * 1000L) + (offset % 1000))
                    putExtra("reminder_title", "${item.title} (${item.subject}) is in ${formatOffset(offset)}")
                    putExtra("is_countdown", true)
                }
                val requestCode = ((item.id % 100000) * 1000 + (offset % 1000)).toInt()
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
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
                } catch (_: SecurityException) {
                    // Degrade gracefully if exact alarm permission was revoked by user
                }
            }
        }
    }

    fun cancelCountdownAlarms(item: CountdownItem) {
        for (offset in item.reminderOffsetsMinutes) {
            val intent = Intent(context, ExactAlarmReceiver::class.java)
            val requestCode = ((item.id % 100000) * 1000 + (offset % 1000)).toInt()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        }
    }

    private fun formatOffset(offsetMinutes: Int): String {
        return when {
            offsetMinutes >= 1440 -> "${offsetMinutes / 1440} day(s)"
            offsetMinutes >= 60 -> "${offsetMinutes / 60} hour(s)"
            else -> "$offsetMinutes minutes"
        }
    }

    private fun ExamCountdownEntity.toDomain(): CountdownItem {
        val offsets = reminderOffsetsMinutes.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .ifEmpty { listOf(1440, 60) }
        return CountdownItem(
            id = id,
            title = title,
            targetEpochMillis = targetEpochMillis,
            subject = subject,
            reminderOffsetsMinutes = offsets
        )
    }

    private fun CountdownItem.toEntity(): ExamCountdownEntity {
        return ExamCountdownEntity(
            id = id,
            title = title,
            targetEpochMillis = targetEpochMillis,
            subject = subject,
            reminderOffsetsMinutes = reminderOffsetsMinutes.joinToString(",")
        )
    }
}
