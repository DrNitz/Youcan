package com.drnitz.youcan.data.repository

import com.drnitz.youcan.data.local.dao.SessionDao
import com.drnitz.youcan.data.local.entity.FocusSessionEntity
import com.drnitz.youcan.widget.WidgetUpdateHelper
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

class SessionRepository(private val sessionDao: SessionDao) {

    val allSessions: Flow<List<FocusSessionEntity>> = sessionDao.getAllSessions()
    val distinctSubjects: Flow<List<String>> = sessionDao.getDistinctSubjects()

    suspend fun logSession(
        startTime: Long,
        endTime: Long,
        durationSeconds: Int,
        subject: String,
        note: String = "",
        isCompleted: Boolean = true,
        isManual: Boolean = false
    ): Long {
        val entity = FocusSessionEntity(
            startTime = startTime,
            endTime = endTime,
            durationSeconds = durationSeconds,
            subject = subject.ifBlank { "General Study" },
            note = note,
            isCompleted = isCompleted,
            isManual = isManual
        )
        val id = sessionDao.insertSession(entity)
        runCatching { WidgetUpdateHelper.updateAll(com.drnitz.youcan.YoucanApplication.instance) }
        return id
    }

    suspend fun deleteSession(session: FocusSessionEntity) {
        sessionDao.deleteSession(session)
        runCatching { WidgetUpdateHelper.updateAll(com.drnitz.youcan.YoucanApplication.instance) }
    }

    fun getSessionsForDay(date: LocalDate): Flow<List<FocusSessionEntity>> {
        val zone = ZoneId.systemDefault()
        val startMillis = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMillis = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        return sessionDao.getSessionsBetween(startMillis, endMillis)
    }

    suspend fun getSessionsBetweenSync(startMillis: Long, endMillis: Long): List<FocusSessionEntity> {
        return sessionDao.getSessionsBetweenSync(startMillis, endMillis)
    }
}
