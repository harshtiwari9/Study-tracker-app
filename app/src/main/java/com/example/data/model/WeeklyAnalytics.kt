package com.example.data.model

import java.time.LocalDate

data class SubjectWeeklyStat(
    val subject: Subject,
    val minutesStudied: Int,
    val targetMinutes: Int,
    val sessionsCount: Int
) {
    val progressFraction: Float
        get() = if (targetMinutes > 0) (minutesStudied.toFloat() / targetMinutes).coerceIn(0f, 1f) else 0f

    val progressPercentage: Int
        get() = if (targetMinutes > 0) ((minutesStudied.toFloat() / targetMinutes) * 100).toInt() else 0

    val remainingMinutes: Int
        get() = (targetMinutes - minutesStudied).coerceAtLeast(0)

    val isGoalMet: Boolean
        get() = minutesStudied >= targetMinutes

    fun formattedStudied(): String {
        val h = minutesStudied / 60
        val m = minutesStudied % 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0 -> "${h}h"
            else -> "${m}m"
        }
    }

    fun formattedTarget(): String {
        val h = targetMinutes / 60
        val m = targetMinutes % 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0 -> "${h}h"
            else -> "${m}m"
        }
    }
}

data class DailyStudyStat(
    val date: LocalDate,
    val dateKey: String,
    val dayLabel: String, // "Mon", "Tue", etc.
    val totalMinutes: Int,
    val subjectMinutes: Map<Subject, Int>
) {
    fun formattedTotal(): String {
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0 -> "${h}h"
            else -> "${m}m"
        }
    }
}

data class WeeklyAnalytics(
    val startOfWeek: LocalDate,
    val endOfWeek: LocalDate,
    val totalMinutes: Int,
    val targetMinutes: Int,
    val previousWeekTotalMinutes: Int,
    val currentStreakDays: Int,
    val dailyStats: List<DailyStudyStat>,
    val subjectStats: List<SubjectWeeklyStat>,
    val tagDistribution: Map<String, Int>,
    val totalSessionsCount: Int
) {
    val progressFraction: Float
        get() = if (targetMinutes > 0) (totalMinutes.toFloat() / targetMinutes).coerceIn(0f, 1f) else 0f

    val progressPercentage: Int
        get() = if (targetMinutes > 0) ((totalMinutes.toFloat() / targetMinutes) * 100).toInt() else 0

    val dailyAverageMinutes: Int
        get() = totalMinutes / 7

    val weekComparisonPercentage: Int
        get() = if (previousWeekTotalMinutes > 0) {
            (((totalMinutes - previousWeekTotalMinutes).toFloat() / previousWeekTotalMinutes) * 100).toInt()
        } else if (totalMinutes > 0) {
            100
        } else 0

    fun formattedTotal(): String {
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0 -> "${h}h"
            else -> "${m}m"
        }
    }

    fun formattedTarget(): String {
        val h = targetMinutes / 60
        val m = targetMinutes % 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0 -> "${h}h"
            else -> "${m}m"
        }
    }

    fun formattedDailyAverage(): String {
        val h = dailyAverageMinutes / 60
        val m = dailyAverageMinutes % 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m/day"
            h > 0 -> "${h}h/day"
            else -> "${m}m/day"
        }
    }
}
