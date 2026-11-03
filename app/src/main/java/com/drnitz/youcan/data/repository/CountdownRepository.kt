package com.drnitz.youcan.data.repository

import com.drnitz.youcan.data.local.dao.CountdownDao
import com.drnitz.youcan.data.local.entity.ExamCountdownEntity
import com.drnitz.youcan.domain.model.CountdownItem
import com.drnitz.youcan.widget.WidgetUpdateHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CountdownRepository(private val countdownDao: CountdownDao) {

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
        runCatching { WidgetUpdateHelper.updateAll(com.drnitz.youcan.YoucanApplication.instance) }
        return id
    }

    suspend fun updateCountdown(item: CountdownItem) {
        countdownDao.updateCountdown(item.toEntity())
        runCatching { WidgetUpdateHelper.updateAll(com.drnitz.youcan.YoucanApplication.instance) }
    }

    suspend fun deleteCountdown(item: CountdownItem) {
        countdownDao.deleteCountdown(item.toEntity())
        runCatching { WidgetUpdateHelper.updateAll(com.drnitz.youcan.YoucanApplication.instance) }
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
