package com.example.data.repository

import com.example.data.local.StudySessionDao
import com.example.data.local.SubjectGoalDao
import com.example.data.model.DailyStudyStat
import com.example.data.model.StudySession
import com.example.data.model.Subject
import com.example.data.model.SubjectGoal
import com.example.data.model.SubjectWeeklyStat
import com.example.data.model.WeeklyAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class StudyRepository(
    private val sessionDao: StudySessionDao,
    private val goalDao: SubjectGoalDao
) {
    val allSessions: Flow<List<StudySession>> = sessionDao.getAllSessions()
    val allGoals: Flow<List<SubjectGoal>> = goalDao.getAllGoals()

    suspend fun insertSession(session: StudySession): Long = withContext(Dispatchers.IO) {
        sessionDao.insertSession(session)
    }

    suspend fun deleteSession(id: Long) = withContext(Dispatchers.IO) {
        sessionDao.deleteSessionById(id)
    }

    suspend fun updateGoal(subject: Subject, weeklyMinutes: Int, dailyMinutes: Int) = withContext(Dispatchers.IO) {
        goalDao.insertOrUpdateGoal(
            SubjectGoal(
                subjectName = subject.name,
                weeklyTargetMinutes = weeklyMinutes,
                dailyTargetMinutes = dailyMinutes,
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    suspend fun resetGoalsToDefaults() = withContext(Dispatchers.IO) {
        val defaultGoals = Subject.entries.map { subject ->
            SubjectGoal(
                subjectName = subject.name,
                weeklyTargetMinutes = subject.defaultWeeklyGoalMinutes,
                dailyTargetMinutes = subject.defaultDailyGoalMinutes
            )
        }
        goalDao.insertOrUpdateGoals(defaultGoals)
    }

    suspend fun initializeDefaultsIfEmpty() = withContext(Dispatchers.IO) {
        val goalCount = goalDao.getGoalCount()
        if (goalCount == 0) {
            resetGoalsToDefaults()
        }

        val sessionCount = sessionDao.getSessionCount()
        if (sessionCount == 0) {
            seedSampleSessions()
        }
    }

    private suspend fun seedSampleSessions() {
        val today = LocalDate.now()
        val dtf = DateTimeFormatter.ISO_LOCAL_DATE
        val nowMillis = System.currentTimeMillis()
        val oneHour = 3600_000L

        // Generate engaging historical study sessions for the current week
        val sampleSessions = listOf(
            StudySession(
                subjectName = Subject.PHYSICS.name,
                topic = "Thermodynamics - Carnot Engine",
                startTimeMillis = nowMillis - (86400000L * 3) - (3 * oneHour),
                endTimeMillis = nowMillis - (86400000L * 3) - (2 * oneHour),
                durationMinutes = 60,
                sessionType = "Pomodoro",
                tag = "Theory",
                productivityRating = 5,
                notes = "Understood efficiency limits and PV diagrams clearly.",
                dateKey = today.minusDays(3).format(dtf)
            ),
            StudySession(
                subjectName = Subject.CHEMISTRY.name,
                topic = "Organic Chemistry - Aldehydes & Ketones",
                startTimeMillis = nowMillis - (86400000L * 3) - oneHour,
                endTimeMillis = nowMillis - (86400000L * 3),
                durationMinutes = 45,
                sessionType = "Pomodoro",
                tag = "Problem Solving",
                productivityRating = 4,
                notes = "Solved 20 mechanism synthesis questions.",
                dateKey = today.minusDays(3).format(dtf)
            ),
            StudySession(
                subjectName = Subject.MATHEMATICS.name,
                topic = "Integral Calculus - Integration by Parts",
                startTimeMillis = nowMillis - (86400000L * 2) - (4 * oneHour),
                endTimeMillis = nowMillis - (86400000L * 2) - (2 * oneHour),
                durationMinutes = 90,
                sessionType = "Stopwatch",
                tag = "Problem Solving",
                productivityRating = 5,
                notes = "Completed exercise 7.4 from textbook.",
                dateKey = today.minusDays(2).format(dtf)
            ),
            StudySession(
                subjectName = Subject.SELF_STUDY.name,
                topic = "Formula Flashcards & Error Book",
                startTimeMillis = nowMillis - (86400000L * 2) - oneHour,
                endTimeMillis = nowMillis - (86400000L * 2),
                durationMinutes = 40,
                sessionType = "Pomodoro",
                tag = "Revision",
                productivityRating = 4,
                notes = "Quick review of all physics vectors & chemistry reagents.",
                dateKey = today.minusDays(2).format(dtf)
            ),
            StudySession(
                subjectName = Subject.PHYSICS.name,
                topic = "Electromagnetism - Ampere's Circuital Law",
                startTimeMillis = nowMillis - 86400000L - (3 * oneHour),
                endTimeMillis = nowMillis - 86400000L - (2 * oneHour),
                durationMinutes = 75,
                sessionType = "Pomodoro",
                tag = "Theory",
                productivityRating = 5,
                notes = "Derived solenoid and toroid magnetic fields.",
                dateKey = today.minusDays(1).format(dtf)
            ),
            StudySession(
                subjectName = Subject.MATHEMATICS.name,
                topic = "Matrices and Determinants",
                startTimeMillis = nowMillis - 86400000L - oneHour,
                endTimeMillis = nowMillis - 86400000L,
                durationMinutes = 60,
                sessionType = "Problem Solving",
                tag = "Mock Test",
                productivityRating = 4,
                notes = "Timed test on invertible matrices.",
                dateKey = today.minusDays(1).format(dtf)
            ),
            StudySession(
                subjectName = Subject.CHEMISTRY.name,
                topic = "Coordination Compounds - Crystal Field Theory",
                startTimeMillis = nowMillis - (2 * oneHour),
                endTimeMillis = nowMillis - oneHour,
                durationMinutes = 50,
                sessionType = "Pomodoro",
                tag = "Theory",
                productivityRating = 5,
                notes = "Octahedral vs tetrahedral splitting diagrams.",
                dateKey = today.format(dtf)
            )
        )
        sessionDao.insertSessions(sampleSessions)
    }

    fun observeWeeklyAnalytics(weekOffset: Long = 0L): Flow<WeeklyAnalytics> {
        val today = LocalDate.now().plusWeeks(weekOffset)
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val dtf = DateTimeFormatter.ISO_LOCAL_DATE

        val prevStartOfWeek = startOfWeek.minusWeeks(1)
        val prevEndOfWeek = endOfWeek.minusWeeks(1)

        return combine(allSessions, allGoals) { sessions, goals ->
            val goalsMap = Subject.entries.associateWith { subject ->
                goals.firstOrNull { it.subjectName.equals(subject.name, ignoreCase = true) }
                    ?.weeklyTargetMinutes ?: subject.defaultWeeklyGoalMinutes
            }

            // Filter for current selected week
            val weekStartDateKey = startOfWeek.format(dtf)
            val weekEndDateKey = endOfWeek.format(dtf)
            val weekSessions = sessions.filter { it.dateKey in weekStartDateKey..weekEndDateKey }

            // Previous week total
            val prevStartKey = prevStartOfWeek.format(dtf)
            val prevEndKey = prevEndOfWeek.format(dtf)
            val prevWeekTotal = sessions
                .filter { it.dateKey in prevStartKey..prevEndKey }
                .sumOf { it.durationMinutes }

            // Total studied this week
            val totalMinutes = weekSessions.sumOf { it.durationMinutes }
            val totalTargetMinutes = goalsMap.values.sum()

            // Subject breakdown
            val subjectStats = Subject.entries.map { subject ->
                val subjectSessions = weekSessions.filter { it.subject == subject }
                val studied = subjectSessions.sumOf { it.durationMinutes }
                val target = goalsMap[subject] ?: subject.defaultWeeklyGoalMinutes
                SubjectWeeklyStat(
                    subject = subject,
                    minutesStudied = studied,
                    targetMinutes = target,
                    sessionsCount = subjectSessions.size
                )
            }

            // Daily breakdown (Mon to Sun)
            val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val dailyStats = (0..6).map { dayIdx ->
                val date = startOfWeek.plusDays(dayIdx.toLong())
                val dateKey = date.format(dtf)
                val daySessions = weekSessions.filter { it.dateKey == dateKey }
                val dayTotal = daySessions.sumOf { it.durationMinutes }
                val subjectMins = Subject.entries.associateWith { subj ->
                    daySessions.filter { it.subject == subj }.sumOf { it.durationMinutes }
                }
                DailyStudyStat(
                    date = date,
                    dateKey = dateKey,
                    dayLabel = dayNames[dayIdx],
                    totalMinutes = dayTotal,
                    subjectMinutes = subjectMins
                )
            }

            // Tag distribution
            val tagMap = weekSessions.groupBy { it.tag }
                .mapValues { entry -> entry.value.sumOf { it.durationMinutes } }

            // Calculate active streak
            val currentStreak = calculateStreak(sessions)

            WeeklyAnalytics(
                startOfWeek = startOfWeek,
                endOfWeek = endOfWeek,
                totalMinutes = totalMinutes,
                targetMinutes = totalTargetMinutes,
                previousWeekTotalMinutes = prevWeekTotal,
                currentStreakDays = currentStreak,
                dailyStats = dailyStats,
                subjectStats = subjectStats,
                tagDistribution = tagMap,
                totalSessionsCount = weekSessions.size
            )
        }
    }

    private fun calculateStreak(allSessions: List<StudySession>): Int {
        if (allSessions.isEmpty()) return 0
        val dtf = DateTimeFormatter.ISO_LOCAL_DATE
        val sessionDates = allSessions.map { it.dateKey }.toSet()
        val today = LocalDate.now()

        var streak = 0
        var checkDate = today

        // If not studied today yet, check if studied yesterday to keep streak alive
        if (!sessionDates.contains(today.format(dtf))) {
            checkDate = today.minusDays(1)
        }

        while (sessionDates.contains(checkDate.format(dtf))) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        return streak
    }
}
