package com.example.ui.screens.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyStudyStat
import com.example.data.model.Subject
import java.time.LocalDate

@Composable
fun WeeklyBarChart(
    dailyStats: List<DailyStudyStat>,
    selectedDay: DailyStudyStat?,
    onDaySelected: (DailyStudyStat) -> Unit,
    targetDailyMinutes: Int = 240, // 4 hours reference line
    modifier: Modifier = Modifier
) {
    if (dailyStats.isEmpty()) return

    val maxMinutes = (dailyStats.maxOfOrNull { it.totalMinutes } ?: 180)
        .coerceAtLeast(targetDailyMinutes)
        .coerceAtLeast(180)
    val chartCeilingMinutes = ((maxMinutes + 59) / 60) * 60 // Round up to next full hour

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_bar_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Study Hours",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap any bar to see subject breakdown",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Reference target indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp, 2.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Goal (${targetDailyMinutes / 60}h)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart area
            val animatedProgress by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600),
                label = "chart_bars_anim"
            )

            val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            val targetLineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            val todayDate = LocalDate.now()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(dailyStats) {
                            detectTapGestures { offset ->
                                val count = dailyStats.size
                                val colWidth = size.width / count
                                val tappedIdx = (offset.x / colWidth).toInt().coerceIn(0, count - 1)
                                onDaySelected(dailyStats[tappedIdx])
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val barCount = dailyStats.size
                    val slotWidth = width / barCount
                    val barWidth = slotWidth * 0.55f

                    // Target benchmark line
                    val targetY = height - (height * (targetDailyMinutes.toFloat() / chartCeilingMinutes))
                    if (targetY in 0f..height) {
                        drawLine(
                            color = targetLineColor,
                            start = Offset(0f, targetY),
                            end = Offset(width, targetY),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                    }

                    // Draw stacked bars for each day
                    dailyStats.forEachIndexed { idx, dayStat ->
                        val barCenterX = (idx * slotWidth) + (slotWidth / 2f)
                        val barLeft = barCenterX - (barWidth / 2f)

                        // Empty background pill track
                        drawRoundRect(
                            color = gridColor.copy(alpha = 0.25f),
                            topLeft = Offset(barLeft, 0f),
                            size = Size(barWidth, height),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // If day has study time, draw stacked segments
                        if (dayStat.totalMinutes > 0) {
                            var currentBottomY = height

                            Subject.entries.forEach { subject ->
                                val mins = dayStat.subjectMinutes[subject] ?: 0
                                if (mins > 0) {
                                    val segHeight = (height * (mins.toFloat() / chartCeilingMinutes) * animatedProgress)
                                        .coerceAtLeast(3.dp.toPx())
                                    val segTopY = (currentBottomY - segHeight).coerceAtLeast(0f)

                                    drawRoundRect(
                                        color = subject.color,
                                        topLeft = Offset(barLeft, segTopY),
                                        size = Size(barWidth, currentBottomY - segTopY),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                    currentBottomY = segTopY
                                }
                            }
                        }

                        // Highlight border if selected
                        if (selectedDay?.dateKey == dayStat.dateKey) {
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(barLeft - 2.dp.toPx(), -2.dp.toPx()),
                                size = Size(barWidth + 4.dp.toPx(), height + 4.dp.toPx()),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis day labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dailyStats.forEach { dayStat ->
                    val isToday = dayStat.date == todayDate
                    val isSelected = selectedDay?.dateKey == dayStat.dateKey

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDaySelected(dayStat) }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dayStat.dayLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )

                        if (dayStat.totalMinutes > 0) {
                            val h = dayStat.totalMinutes / 60
                            val m = dayStat.totalMinutes % 60
                            Text(
                                text = if (h > 0) "${h}h" else "${m}m",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "-",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        if (isToday) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }

            // Legend Row for Subjects
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Subject.entries.forEach { subject ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(subject.color)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = subject.shortName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
