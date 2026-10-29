package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_sessions",
    indices = [
        Index(value = ["dateKey"]),
        Index(value = ["subjectName"])
    ]
)
data class StudySession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectName: String,
    val topic: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationMinutes: Int,
    val sessionType: String = "Pomodoro", // "Pomodoro", "Stopwatch", "Manual"
    val tag: String = "Problem Solving",  // "Theory", "Problem Solving", "Revision", "Mock Test"
    val productivityRating: Int = 4,      // 1..5
    val notes: String = "",
    val dateKey: String                   // "YYYY-MM-DD"
) {
    val subject: Subject
        get() = Subject.fromName(subjectName)

    fun formattedDuration(): String {
        val hours = durationMinutes / 60
        val mins = durationMinutes % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}m"
            hours > 0 -> "${hours}h"
            else -> "${mins}m"
        }
    }
}
