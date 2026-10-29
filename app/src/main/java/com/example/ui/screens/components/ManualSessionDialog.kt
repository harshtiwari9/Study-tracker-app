package com.example.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Subject
import java.time.LocalDate

@Composable
fun ManualSessionDialog(
    initialSubject: Subject = Subject.PHYSICS,
    onSaveSession: (
        subject: Subject,
        topic: String,
        durationMinutes: Int,
        tag: String,
        date: LocalDate,
        rating: Int,
        notes: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSubject by remember { mutableStateOf(initialSubject) }
    var topic by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableIntStateOf(60) }
    var selectedTag by remember { mutableStateOf("Problem Solving") }
    var selectedDateOffset by remember { mutableIntStateOf(0) } // 0 = Today, 1 = Yesterday, 2 = 2 days ago
    var rating by remember { mutableIntStateOf(4) }
    var notes by remember { mutableStateOf("") }

    val tags = listOf("Theory", "Problem Solving", "Revision", "Mock Test")
    val dateOptions = listOf(
        Pair("Today", 0),
        Pair("Yesterday", 1),
        Pair("2 Days Ago", 2)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("manual_session_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Log Study Session",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_manual_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Subject Picker
                Text(
                    text = "Select Subject",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Subject.entries.forEach { subject ->
                        val isSelected = selectedSubject == subject
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) subject.color else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedSubject = subject }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = subject.shortName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Topic Input
                Text(
                    text = "Topic / Chapter",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    placeholder = { Text("e.g. Electromagnetism, Integration...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_topic_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Duration Stepper & Quick Presets
                Text(
                    text = "Duration",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { durationMinutes = (durationMinutes - 15).coerceAtLeast(15) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease 15 mins")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val h = durationMinutes / 60
                        val m = durationMinutes % 60
                        Text(
                            text = if (h > 0 && m > 0) "${h}h ${m}m" else if (h > 0) "${h} Hours" else "${m} Minutes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = selectedSubject.color
                        )
                    }

                    IconButton(
                        onClick = { durationMinutes = (durationMinutes + 15).coerceAtMost(480) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase 15 mins")
                    }
                }

                // Quick duration pills
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(30, 45, 60, 90, 120).forEach { preset ->
                        val isSel = durationMinutes == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSel) selectedSubject.color.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    1.dp,
                                    if (isSel) selectedSubject.color else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { durationMinutes = preset }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val label = if (preset >= 60) "${preset / 60}h" else "${preset}m"
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) selectedSubject.color else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tag selection
                Text(
                    text = "Study Focus Tag",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tags) { tag ->
                        val isSel = selectedTag == tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSel) selectedSubject.color else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedTag = tag }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
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

                Spacer(modifier = Modifier.height(16.dp))

                // Date Picker row
                Text(
                    text = "Date",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dateOptions.forEach { (label, offset) ->
                        val isSel = selectedDateOffset == offset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSel) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedDateOffset = offset }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Productivity Rating",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row {
                        (1..5).forEach { star ->
                            val isFilled = star <= rating
                            Icon(
                                imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "$star stars",
                                tint = if (isFilled) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable { rating = star }
                                    .padding(2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("Problems solved, key takeaways...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_notes_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Save button
                Button(
                    onClick = {
                        val sessionDate = LocalDate.now().minusDays(selectedDateOffset.toLong())
                        onSaveSession(
                            selectedSubject,
                            topic,
                            durationMinutes,
                            selectedTag,
                            sessionDate,
                            rating,
                            notes
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_manual_session_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = selectedSubject.color
                    )
                ) {
                    Text("Save Study Session", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
