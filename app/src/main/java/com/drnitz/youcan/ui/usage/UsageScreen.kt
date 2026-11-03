package com.drnitz.youcan.ui.usage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.drnitz.youcan.core.util.PermissionHelper
import com.drnitz.youcan.domain.model.AppUsageInfo

@Composable
fun UsageScreen(
    usageList: List<AppUsageInfo>,
    hasPermission: Boolean,
    onToggleDistracting: (AppUsageInfo, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val totalMinutes = usageList.sumOf { it.totalTimeMillis } / (1000 * 60)
    val distractingMinutes = usageList.filter { it.isDistracting }.sumOf { it.totalTimeMillis } / (1000 * 60)
    val inSessionMinutes = usageList.sumOf { it.inSessionTimeMillis } / (1000 * 60)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Phone & App Usage",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Understand your digital habits and mark distracting apps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (!hasPermission) {
            item {
                UsagePermissionBanner(
                    onGrantClick = { PermissionHelper.openUsageAccessSettings(context) }
                )
            }
        } else {
            item {
                UsageSummaryCard(
                    totalMinutes = totalMinutes,
                    distractingMinutes = distractingMinutes,
                    inSessionMinutes = inSessionMinutes
                )
            }

            item {
                Text(
                    text = "Apps Used Today",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (usageList.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No app usage recorded yet for today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(usageList, key = { it.packageName }) { info ->
                    AppUsageRow(
                        info = info,
                        onToggleDistracting = { isDistracting ->
                            onToggleDistracting(info, isDistracting)
                        }
                    )
                }
            }
        }
    }
}
