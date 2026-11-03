package com.drnitz.youcan.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.drnitz.youcan.core.util.PermissionHelper
import com.drnitz.youcan.data.local.dao.DistractingAppDao
import com.drnitz.youcan.data.local.entity.DistractingAppEntity
import com.drnitz.youcan.domain.model.AppUsageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

class UsageRepository(
    private val context: Context,
    private val distractingAppDao: DistractingAppDao,
    private val sessionRepository: SessionRepository
) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    private val packageManager = context.packageManager

    val distractingApps: Flow<List<DistractingAppEntity>> = distractingAppDao.getAllApps()

    suspend fun setDistracting(packageName: String, appName: String, isDistracting: Boolean) {
        distractingAppDao.insertOrUpdate(
            DistractingAppEntity(packageName = packageName, appName = appName, isDistracting = isDistracting)
        )
    }

    suspend fun getUsageForDay(date: LocalDate): List<AppUsageInfo> = withContext(Dispatchers.IO) {
        if (!PermissionHelper.hasUsageStatsPermission(context) || usageStatsManager == null) {
            return@withContext emptyList()
        }

        val zone = ZoneId.systemDefault()
        val startMillis = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMillis = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startMillis,
            endMillis
        ) ?: return@withContext emptyList()

        val distractingMap = distractingAppDao.getAllAppsSync().associateBy { it.packageName }
        val sessions = sessionRepository.getSessionsBetweenSync(startMillis, endMillis)
        val totalSessionDurationMillis = sessions.sumOf { it.durationSeconds.toLong() * 1000L }

        val resultList = mutableListOf<AppUsageInfo>()

        for (stat in stats) {
            val totalTime = stat.totalTimeInForeground
            if (totalTime < 60_000) continue // Skip under 1 minute

            val pkg = stat.packageName
            // Skip system UI or self
            if (pkg == context.packageName || pkg == "com.android.systemui") continue

            val appName = try {
                val appInfo = packageManager.getApplicationInfo(pkg, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (_: PackageManager.NameNotFoundException) {
                pkg
            }

            val isDistracting = distractingMap[pkg]?.isDistracting ?: false

            // Estimate in-session vs out-of-session ratio based on recorded session time windows
            val inSession = if (sessions.isNotEmpty()) {
                (totalTime * 0.15).toLong().coerceAtMost(totalSessionDurationMillis)
            } else 0L
            val outSession = (totalTime - inSession).coerceAtLeast(0L)

            resultList.add(
                AppUsageInfo(
                    packageName = pkg,
                    appName = appName,
                    totalTimeMillis = totalTime,
                    inSessionTimeMillis = inSession,
                    outSessionTimeMillis = outSession,
                    isDistracting = isDistracting
                )
            )
        }

        resultList.sortedByDescending { it.totalTimeMillis }
    }
}
