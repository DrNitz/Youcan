package com.drnitz.youcan.ui.timer

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SubjectTagPicker(
    currentSubject: String,
    onSubjectSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val commonSubjects = listOf("General Study", "Math", "Computer Science", "Physics", "Languages", "Writing")

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (sub in commonSubjects) {
            val isSelected = sub.equals(currentSubject, ignoreCase = true)
            FilterChip(
                selected = isSelected,
                onClick = { onSubjectSelected(sub) },
                label = { Text(sub) },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = Color.White.copy(alpha = 0.12f),
                    labelColor = Color.White.copy(alpha = 0.85f)
                )
            )
        }
    }
}
