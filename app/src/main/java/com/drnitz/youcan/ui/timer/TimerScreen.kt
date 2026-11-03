package com.drnitz.youcan.ui.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drnitz.youcan.core.backgrounds.TimerBackgroundView
import com.drnitz.youcan.core.util.WidgetPinningHelper
import com.drnitz.youcan.data.preferences.UserPreferences
import com.drnitz.youcan.domain.model.Quote
import com.drnitz.youcan.domain.model.QuoteProvider
import com.drnitz.youcan.domain.model.TimerStatus
import com.drnitz.youcan.service.timer.FocusTimerService
import com.drnitz.youcan.service.timer.TimerStateManager
import com.drnitz.youcan.widget.TimerWidgetReceiver
import java.time.LocalDate

@Composable
fun TimerScreen(
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snapshot by TimerStateManager.snapshot.collectAsState()
    var showNoteDialog by remember { mutableStateOf(false) }

    val initialQuoteIndex = remember {
        val day = LocalDate.now().toEpochDay()
        val size = QuoteProvider.quotes.size
        ((day % size).toInt() + size) % size
    }
    var currentQuoteIndex by remember { mutableIntStateOf(initialQuoteIndex) }
    val currentQuote = QuoteProvider.getQuoteByIndex(currentQuoteIndex)

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    LaunchedEffect(snapshot.status) {
        if (snapshot.status == TimerStatus.RUNNING && !FocusTimerService.isRunning()) {
            FocusTimerService.resumeTimer(context)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        TimerBackgroundView(
            backgroundType = userPreferences.backgroundType,
            customWallpaperUri = userPreferences.customWallpaperUri,
            wallpaperBlurRadius = userPreferences.wallpaperBlurRadius,
            wallpaperDimAlpha = userPreferences.wallpaperDimAlpha,
            reducedMotionOverride = userPreferences.reducedMotionOverride
        )

        if (isLandscape) {
            // Adaptive horizontal 2-pane layout for wide screens and landscape
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = 48.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    TimerRing(
                        snapshot = snapshot,
                        size = if (configuration.screenWidthDp >= 800) 350.dp else 280.dp,
                        strokeWidth = if (configuration.screenWidthDp >= 800) 14.dp else 10.dp
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TimerQuoteBanner(
                        quote = currentQuote,
                        onNextQuote = {
                            currentQuoteIndex = (currentQuoteIndex + 1) % QuoteProvider.quotes.size
                        }
                    )
                    SubjectTagPicker(
                        currentSubject = snapshot.subject,
                        onSubjectSelected = { TimerStateManager.setSubject(it) }
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NoteChip(
                            note = snapshot.note,
                            onClick = { showNoteDialog = true }
                        )
                        PinWidgetChip(
                            onClick = {
                                WidgetPinningHelper.pinWidget(context, TimerWidgetReceiver::class.java, "Timer Widget")
                            }
                        )
                    }
                    TimerControls(
                        status = snapshot.status,
                        onStart = { FocusTimerService.startTimer(context, snapshot.subject, snapshot.note) },
                        onPause = { FocusTimerService.pauseTimer(context) },
                        onResume = { FocusTimerService.resumeTimer(context) },
                        onStop = { FocusTimerService.stopTimer(context) },
                        onSkip = { FocusTimerService.skipPhase(context) }
                    )
                }
            }
        } else {
            // Portrait layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top header: Quote banner, Subject chips & quick note
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimerQuoteBanner(
                        quote = currentQuote,
                        onNextQuote = {
                            currentQuoteIndex = (currentQuoteIndex + 1) % QuoteProvider.quotes.size
                        }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    SubjectTagPicker(
                        currentSubject = snapshot.subject,
                        onSubjectSelected = { TimerStateManager.setSubject(it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NoteChip(
                            note = snapshot.note,
                            onClick = { showNoteDialog = true }
                        )
                        PinWidgetChip(
                            onClick = {
                                WidgetPinningHelper.pinWidget(context, TimerWidgetReceiver::class.java, "Timer Widget")
                            }
                        )
                    }
                }

                // Hero Center: Circular countdown ring
                TimerRing(
                    snapshot = snapshot,
                    size = 280.dp
                )

                // Bottom: Primary action controls
                TimerControls(
                    status = snapshot.status,
                    onStart = { FocusTimerService.startTimer(context, snapshot.subject, snapshot.note) },
                    onPause = { FocusTimerService.pauseTimer(context) },
                    onResume = { FocusTimerService.resumeTimer(context) },
                    onStop = { FocusTimerService.stopTimer(context) },
                    onSkip = { FocusTimerService.skipPhase(context) },
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }

        if (showNoteDialog) {
            SessionNoteDialog(
                initialNote = snapshot.note,
                onDismiss = { showNoteDialog = false },
                onSave = {
                    TimerStateManager.setNote(it)
                    showNoteDialog = false
                }
            )
        }
    }
}

@Composable
private fun TimerQuoteBanner(
    quote: Quote,
    onNextQuote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .widthIn(max = 480.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.28f))
            .clickable(onClick = onNextQuote)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "“${quote.text}”",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.5.sp,
                    lineHeight = 16.sp
                ),
                color = Color.White.copy(alpha = 0.92f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = quote.author,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = Color.White.copy(alpha = 0.72f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NoteChip(
    note: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = if (note.isBlank()) "Add session note" else note,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.EditNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = Color.White.copy(alpha = 0.12f),
            labelColor = Color.White.copy(alpha = 0.85f)
        ),
        modifier = modifier
    )
}

@Composable
private fun PinWidgetChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = "Pin Widget",
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Widgets,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = Color.White.copy(alpha = 0.12f),
            labelColor = Color.White.copy(alpha = 0.9f)
        ),
        modifier = modifier
    )
}
