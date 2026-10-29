package com.example.ui.screens.goals

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Subject
import com.example.data.model.SubjectGoal
import com.example.ui.viewmodel.StudyViewModel

@Composable
fun GoalsScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val goals by viewModel.allGoals.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Map current goals
    val goalsMap = Subject.entries.associateWith { subj ->
        goals.firstOrNull { it.subjectName.equals(subj.name, ignoreCase = true) }
            ?: SubjectGoal(
                subjectName = subj.name,
                weeklyTargetMinutes = subj.defaultWeeklyGoalMinutes,
                dailyTargetMinutes = subj.defaultDailyGoalMinutes
            )
    }

    val totalWeeklyMinutes = goalsMap.values.sumOf { it.weeklyTargetMinutes }
    val totalWeeklyHours = totalWeeklyMinutes / 60
    val totalWeeklyRemMins = totalWeeklyMinutes % 60
    val totalDailyAverageMins = totalWeeklyMinutes / 7

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Goal Setting",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Configure weekly & daily subject targets",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { viewModel.resetGoalsToDefault() },
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        CircleShape
                    )
                    .testTag("reset_goals_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset to Defaults",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Total Commitment Summary Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("goals_summary_banner"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL WEEKLY COMMITMENT",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (totalWeeklyRemMins > 0) "${totalWeeklyHours}h ${totalWeeklyRemMins}m / week" else "${totalWeeklyHours} Hours / week",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrackChanges,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val avgHours = totalDailyAverageMins / 60
                val avgMins = totalDailyAverageMins % 60
                val avgStr = if (avgHours > 0) "${avgHours}h ${avgMins}m / day" else "${avgMins}m / day"

                Text(
                    text = "Requires an average of $avgStr distributed across 7 days to complete all targets.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "SUBJECT TARGETS",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Subject Goal Cards
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Subject.entries.forEach { subject ->
                val currentGoal = goalsMap[subject]!!
                SubjectGoalEditCard(
                    subject = subject,
                    weeklyMinutes = currentGoal.weeklyTargetMinutes,
                    dailyMinutes = currentGoal.dailyTargetMinutes,
                    onUpdateWeekly = { newWeekly ->
                        viewModel.updateSubjectGoal(
                            subject,
                            newWeekly.coerceIn(60, 2400),
                            currentGoal.dailyTargetMinutes
                        )
                    },
                    onUpdateDaily = { newDaily ->
                        viewModel.updateSubjectGoal(
                            subject,
                            currentGoal.weeklyTargetMinutes,
                            newDaily.coerceIn(15, 360)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tip Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Subject Balance Recommendation",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "For competitive exams like JEE/NEET, aim for equal 7-9 hours per week in Physics, Chemistry, and Maths, while reserving 5-6 hours of Self Study for revision and mock tests.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SubjectGoalEditCard(
    subject: Subject,
    weeklyMinutes: Int,
    dailyMinutes: Int,
    onUpdateWeekly: (Int) -> Unit,
    onUpdateDaily: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("goal_card_${subject.name.lowercase()}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Subject header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(subject.color)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = subject.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val weeklyH = weeklyMinutes / 60
                val weeklyM = weeklyMinutes % 60
                Text(
                    text = if (weeklyM > 0) "${weeklyH}h ${weeklyM}m / week" else "${weeklyH} hrs / week",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = subject.color
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Weekly Target Steppers
            Text(
                text = "Weekly Target Adjuster",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { onUpdateWeekly(weeklyMinutes - 60) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("-1h")
                }

                FilledTonalButton(
                    onClick = { onUpdateWeekly(weeklyMinutes - 30) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("-30m")
                }

                FilledTonalButton(
                    onClick = { onUpdateWeekly(weeklyMinutes + 30) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("+30m")
                }

                FilledTonalButton(
                    onClick = { onUpdateWeekly(weeklyMinutes + 60) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("+1h")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Daily Target Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Target",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val dh = dailyMinutes / 60
                    val dm = dailyMinutes % 60
                    Text(
                        text = if (dh > 0 && dm > 0) "${dh}h ${dm}m / day" else if (dh > 0) "${dh}h / day" else "${dm}m / day",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onUpdateDaily(dailyMinutes - 15) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease Daily Target")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onUpdateDaily(dailyMinutes + 15) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase Daily Target")
                    }
                }
            }
        }
    }
}
