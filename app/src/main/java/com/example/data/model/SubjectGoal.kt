package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subject_goals")
data class SubjectGoal(
    @PrimaryKey
    val subjectName: String,
    val weeklyTargetMinutes: Int,
    val dailyTargetMinutes: Int,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val subject: Subject
        get() = Subject.fromName(subjectName)

    fun formattedWeeklyTarget(): String {
        val hours = weeklyTargetMinutes / 60
        val mins = weeklyTargetMinutes % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}m"
            hours > 0 -> "${hours}h"
            else -> "${mins}m"
        }
    }

    fun formattedDailyTarget(): String {
        val hours = dailyTargetMinutes / 60
        val mins = dailyTargetMinutes % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}m"
            hours > 0 -> "${hours}h"
            else -> "${mins}m"
        }
    }
}
