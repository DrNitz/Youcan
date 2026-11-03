package com.drnitz.youcan.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

data class Quote(val text: String, val author: String)

val FocusQuotes = listOf(
    Quote("Small steps made consistently create profound changes over time.", "James Clear"),
    Quote("Focus is a muscle. Every time you bring your attention back, you strengthen it.", "Cal Newport"),
    Quote("The secret of getting ahead is getting started.", "Mark Twain"),
    Quote("Deep work is the ability to focus without distraction on a cognitively demanding task.", "Cal Newport"),
    Quote("Do what you can, with what you have, where you are.", "Theodore Roosevelt"),
    Quote("Clarity precedes mastery. Quiet your environment to hear your thoughts.", "Robin Sharma"),
    Quote("It does not matter how slowly you go as long as you do not stop.", "Confucius")
)

@Composable
fun MotivationalQuoteCard(
    date: LocalDate = LocalDate.now(),
    modifier: Modifier = Modifier
) {
    val quote = remember(date) {
        val index = (date.toEpochDay() % FocusQuotes.size).toInt().let { if (it < 0) it + FocusQuotes.size else it }
        FocusQuotes[index]
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "\"${quote.text}\"",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = quote.author,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
