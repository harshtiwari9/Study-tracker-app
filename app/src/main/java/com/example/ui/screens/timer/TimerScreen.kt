package com.example.ui.screens.timer

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Subject
import com.example.ui.screens.components.SaveSessionDialog
import com.example.ui.viewmodel.StudyViewModel
import com.example.ui.viewmodel.TimerMode

@Composable
fun TimerScreen(
    viewModel: StudyViewModel,
    onOpenManualLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedSubject by viewModel.selectedSubject.collectAsStateWithLifecycle()
    val topicText by viewModel.topicText.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()
    val timerMode by viewModel.timerMode.collectAsStateWithLifecycle()
    val pomodoroPreset by viewModel.pomodoroPresetMinutes.collectAsStateWithLifecycle()
    val isRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val isPaused by viewModel.isTimerPaused.collectAsStateWithLifecycle()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()
    val showSaveDialog by viewModel.showSaveDialog.collectAsStateWithLifecycle()
    val completedDurationMins by viewModel.sessionCompletedDurationMinutes.collectAsStateWithLifecycle()

    val availableTags = listOf("Theory", "Problem Solving", "Revision", "Mock Test")
    val pomodoroPresets = listOf(25, 45, 50, 60, 90)

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Bar Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Study Session",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Track your subject hours & focus",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick manual log button
            FilledTonalButton(
                onClick = onOpenManualLog,
                modifier = Modifier.testTag("open_manual_log_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log Manually", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timer Mode Toggle (Pomodoro vs Stopwatch)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    RoundedCornerShape(14.dp)
                )
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (timerMode == TimerMode.POMODORO) MaterialTheme.colorScheme.surface else Color.Transparent
                    )
                    .clickable(enabled = !isRunning) { viewModel.setTimerMode(TimerMode.POMODORO) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HourglassBottom,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (timerMode == TimerMode.POMODORO) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pomodoro Timer",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (timerMode == TimerMode.POMODORO) FontWeight.Bold else FontWeight.Medium,
                        color = if (timerMode == TimerMode.POMODORO) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (timerMode == TimerMode.STOPWATCH) MaterialTheme.colorScheme.surface else Color.Transparent
                    )
                    .clickable(enabled = !isRunning) { viewModel.setTimerMode(TimerMode.STOPWATCH) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (timerMode == TimerMode.STOPWATCH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Open Stopwatch",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (timerMode == TimerMode.STOPWATCH) FontWeight.Bold else FontWeight.Medium,
                        color = if (timerMode == TimerMode.STOPWATCH) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subject Selector Cards (Physics, Chemistry, Maths, Self Study)
        Text(
            text = "SELECT SUBJECT",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Subject.entries.forEach { subject ->
                val isSelected = selectedSubject == subject
                val icon: ImageVector = when (subject) {
                    Subject.PHYSICS -> Icons.Default.Science
                    Subject.CHEMISTRY -> Icons.Default.Science
                    Subject.MATHEMATICS -> Icons.Default.Calculate
                    Subject.SELF_STUDY -> Icons.Default.MenuBook
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("subject_card_${subject.name.lowercase()}")
                        .clickable(enabled = !isRunning) { viewModel.setSubject(subject) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) subject.color else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 4.dp else 1.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color.White.copy(alpha = 0.25f)
                                    else subject.color.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else subject.color,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = subject.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Pomodoro Preset selector if in Pomodoro Mode
        if (timerMode == TimerMode.POMODORO && !isRunning) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TARGET DURATION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pomodoroPresets.forEach { preset ->
                    val isSel = pomodoroPreset == preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSel) selectedSubject.color.copy(alpha = 0.18f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                1.5.dp,
                                if (isSel) selectedSubject.color else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.setPomodoroPreset(preset) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${preset}m",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) selectedSubject.color else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Circular Timer Display
        val targetTotalSecs = if (timerMode == TimerMode.POMODORO) pomodoroPreset * 60f else 3600f
        val progressFraction = if (timerMode == TimerMode.POMODORO) {
            (elapsedSeconds.toFloat() / targetTotalSecs).coerceIn(0f, 1f)
        } else {
            ((elapsedSeconds % 3600).toFloat() / 3600f).coerceIn(0f, 1f)
        }

        val animatedProgress by animateFloatAsState(
            targetValue = progressFraction,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            label = "timer_ring_anim"
        )

        val displaySeconds = if (timerMode == TimerMode.POMODORO) remainingSeconds else elapsedSeconds
        val hours = displaySeconds / 3600
        val mins = (displaySeconds % 3600) / 60
        val secs = displaySeconds % 60
        val timeDisplayString = if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, mins, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }

        Box(
            modifier = Modifier
                .size(240.dp)
                .testTag("circular_timer_box"),
            contentAlignment = Alignment.Center
        ) {
            val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            val ringColor = selectedSubject.color
            val glowColor = selectedSubject.glowColor

            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f
                val centerOffset = Offset(size.width / 2f, size.height / 2f)

                // Background Ring Track
                drawCircle(
                    color = trackColor,
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = strokeWidth)
                )

                // Foreground Animated Progress Arc
                val sweepAngle = animatedProgress * 360f
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(ringColor, glowColor, ringColor),
                        center = centerOffset
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Inner content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Active Subject Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(selectedSubject.color.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = selectedSubject.displayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = selectedSubject.color
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Digits
                Text(
                    text = timeDisplayString,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 42.sp,
                        letterSpacing = 1.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when {
                        !isRunning -> "READY TO FOCUS"
                        isPaused -> "SESSION PAUSED"
                        timerMode == TimerMode.POMODORO -> "FOCUSING..."
                        else -> "RECORDING TIME"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isRunning && !isPaused) selectedSubject.color else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Timer Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            IconButton(
                onClick = { viewModel.resetTimerState() },
                enabled = isRunning || elapsedSeconds > 0,
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        CircleShape
                    )
                    .testTag("reset_timer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Timer",
                    tint = if (isRunning || elapsedSeconds > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Main Play / Pause Button
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .shadow(8.dp, CircleShape, ambientColor = selectedSubject.color, spotColor = selectedSubject.color)
                    .clip(CircleShape)
                    .background(selectedSubject.color)
                    .clickable {
                        if (!isRunning) {
                            viewModel.startTimer()
                        } else if (isPaused) {
                            viewModel.resumeTimer()
                        } else {
                            viewModel.pauseTimer()
                        }
                    }
                    .testTag("play_pause_timer_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRunning && !isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning && !isPaused) "Pause" else "Start",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Stop & Save Button
            IconButton(
                onClick = { viewModel.stopAndPromptSave() },
                enabled = isRunning || elapsedSeconds > 0,
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        if (isRunning || elapsedSeconds > 0) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        CircleShape
                    )
                    .testTag("stop_save_timer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop and Save",
                    tint = if (isRunning || elapsedSeconds > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Topic and Tag Configuration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Topic / Target Chapter",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = topicText,
                    onValueChange = { viewModel.setTopic(it) },
                    placeholder = {
                        Text(
                            when (selectedSubject) {
                                Subject.PHYSICS -> "e.g. Thermodynamics, Optics, Rotational Motion"
                                Subject.CHEMISTRY -> "e.g. Organic Mechanisms, Coordination Compounds"
                                Subject.MATHEMATICS -> "e.g. Definite Integrals, Vectors & 3D"
                                Subject.SELF_STUDY -> "e.g. Revision Flashcards, Daily Mock Test"
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("timer_topic_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = selectedSubject.color,
                        focusedLabelColor = selectedSubject.color
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Focus Activity",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableTags) { tag ->
                        val isSel = selectedTag == tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSel) selectedSubject.color else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { viewModel.setTag(tag) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Save Session Modal Dialog
    if (showSaveDialog) {
        SaveSessionDialog(
            subject = selectedSubject,
            topic = topicText.ifBlank { "${selectedSubject.displayName} Study Session" },
            durationMinutes = completedDurationMins,
            tag = selectedTag,
            onSave = { notes, rating ->
                viewModel.saveCompletedSession(notes, rating)
            },
            onDismiss = {
                viewModel.dismissSaveDialog()
            }
        )
    }
}
