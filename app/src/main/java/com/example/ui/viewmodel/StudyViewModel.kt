package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.DailyStudyStat
import com.example.data.model.StudySession
import com.example.data.model.Subject
import com.example.data.model.SubjectGoal
import com.example.data.model.WeeklyAnalytics
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class TimerMode {
    POMODORO,
    STOPWATCH
}

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = StudyRepository(db.studySessionDao(), db.subjectGoalDao())
        viewModelScope.launch {
            repository.initializeDefaultsIfEmpty()
        }
    }

    // --- Active Timer State ---
    private val _selectedSubject = MutableStateFlow(Subject.PHYSICS)
    val selectedSubject: StateFlow<Subject> = _selectedSubject.asStateFlow()

    private val _topicText = MutableStateFlow("")
    val topicText: StateFlow<String> = _topicText.asStateFlow()

    private val _selectedTag = MutableStateFlow("Theory")
    val selectedTag: StateFlow<String> = _selectedTag.asStateFlow()

    private val _timerMode = MutableStateFlow(TimerMode.POMODORO)
    val timerMode: StateFlow<TimerMode> = _timerMode.asStateFlow()

    private val _pomodoroPresetMinutes = MutableStateFlow(25)
    val pomodoroPresetMinutes: StateFlow<Int> = _pomodoroPresetMinutes.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _isTimerPaused = MutableStateFlow(false)
    val isTimerPaused: StateFlow<Boolean> = _isTimerPaused.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(25 * 60L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()

    private val _showSaveDialog = MutableStateFlow(false)
    val showSaveDialog: StateFlow<Boolean> = _showSaveDialog.asStateFlow()

    private val _sessionCompletedDurationMinutes = MutableStateFlow(0)
    val sessionCompletedDurationMinutes: StateFlow<Int> = _sessionCompletedDurationMinutes.asStateFlow()

    private var timerJob: Job? = null
    private var sessionStartTimeMillis: Long = 0L

    fun setSubject(subject: Subject) {
        if (!_isTimerRunning.value) {
            _selectedSubject.value = subject
        }
    }

    fun setTopic(topic: String) {
        _topicText.value = topic
    }

    fun setTag(tag: String) {
        _selectedTag.value = tag
    }

    fun setTimerMode(mode: TimerMode) {
        if (!_isTimerRunning.value) {
            _timerMode.value = mode
            resetTimerState()
        }
    }

    fun setPomodoroPreset(minutes: Int) {
        if (!_isTimerRunning.value) {
            _pomodoroPresetMinutes.value = minutes
            _remainingSeconds.value = minutes * 60L
            _elapsedSeconds.value = 0L
        }
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        _isTimerPaused.value = false
        if (sessionStartTimeMillis == 0L) {
            sessionStartTimeMillis = System.currentTimeMillis()
        }

        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value && !_isTimerPaused.value) {
                delay(1000L)
                if (_timerMode.value == TimerMode.POMODORO) {
                    if (_remainingSeconds.value > 0) {
                        _remainingSeconds.value -= 1
                        _elapsedSeconds.value += 1
                    } else {
                        // Pomodoro finished!
                        pauseTimer()
                        onTimerFinished()
                        break
                    }
                } else {
                    _elapsedSeconds.value += 1
                }
            }
        }
    }

    fun pauseTimer() {
        _isTimerPaused.value = true
        timerJob?.cancel()
    }

    fun resumeTimer() {
        if (_isTimerRunning.value && _isTimerPaused.value) {
            _isTimerPaused.value = false
            startTimer()
        }
    }

    fun stopAndPromptSave() {
        pauseTimer()
        val totalSecs = _elapsedSeconds.value
        val durationMins = (totalSecs / 60).toInt().coerceAtLeast(1)
        _sessionCompletedDurationMinutes.value = durationMins
        _showSaveDialog.value = true
    }

    private fun onTimerFinished() {
        val durationMins = (_elapsedSeconds.value / 60).toInt().coerceAtLeast(1)
        _sessionCompletedDurationMinutes.value = durationMins
        _showSaveDialog.value = true
    }

    fun dismissSaveDialog() {
        _showSaveDialog.value = false
        resetTimerState()
    }

    fun saveCompletedSession(notes: String, rating: Int) {
        val durationMins = _sessionCompletedDurationMinutes.value
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val now = System.currentTimeMillis()
        val start = if (sessionStartTimeMillis > 0) sessionStartTimeMillis else (now - (durationMins * 60_000L))

        viewModelScope.launch {
            val session = StudySession(
                subjectName = _selectedSubject.value.name,
                topic = _topicText.value.ifBlank { "${_selectedSubject.value.displayName} Practice" },
                startTimeMillis = start,
                endTimeMillis = now,
                durationMinutes = durationMins,
                sessionType = if (_timerMode.value == TimerMode.POMODORO) "Pomodoro" else "Stopwatch",
                tag = _selectedTag.value,
                productivityRating = rating,
                notes = notes,
                dateKey = todayStr
            )
            repository.insertSession(session)
            _showSaveDialog.value = false
            resetTimerState()
        }
    }

    fun resetTimerState() {
        timerJob?.cancel()
        _isTimerRunning.value = false
        _isTimerPaused.value = false
        _elapsedSeconds.value = 0L
        _remainingSeconds.value = _pomodoroPresetMinutes.value * 60L
        sessionStartTimeMillis = 0L
    }

    // --- Weekly Analytics State ---
    private val _weekOffset = MutableStateFlow(0L)
    val weekOffset: StateFlow<Long> = _weekOffset.asStateFlow()

    val weeklyAnalytics: StateFlow<WeeklyAnalytics> = _weekOffset
        .flatMapLatest { offset -> repository.observeWeeklyAnalytics(offset) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeeklyAnalytics(
                startOfWeek = LocalDate.now(),
                endOfWeek = LocalDate.now(),
                totalMinutes = 0,
                targetMinutes = 0,
                previousWeekTotalMinutes = 0,
                currentStreakDays = 0,
                dailyStats = emptyList(),
                subjectStats = emptyList(),
                tagDistribution = emptyMap(),
                totalSessionsCount = 0
            )
        )

    private val _selectedDayStat = MutableStateFlow<DailyStudyStat?>(null)
    val selectedDayStat: StateFlow<DailyStudyStat?> = _selectedDayStat.asStateFlow()

    fun selectDayStat(stat: DailyStudyStat?) {
        _selectedDayStat.value = stat
    }

    fun nextWeek() {
        if (_weekOffset.value < 0L) {
            _weekOffset.value += 1L
            _selectedDayStat.value = null
        }
    }

    fun previousWeek() {
        _weekOffset.value -= 1L
        _selectedDayStat.value = null
    }

    fun currentWeek() {
        _weekOffset.value = 0L
        _selectedDayStat.value = null
    }

    // --- Goals State ---
    val allGoals: StateFlow<List<SubjectGoal>> = repository.allGoals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSubjectGoal(subject: Subject, weeklyMinutes: Int, dailyMinutes: Int) {
        viewModelScope.launch {
            repository.updateGoal(subject, weeklyMinutes, dailyMinutes)
        }
    }

    fun resetGoalsToDefault() {
        viewModelScope.launch {
            repository.resetGoalsToDefaults()
        }
    }

    // --- History & Manual Session Log State ---
    val allSessions: StateFlow<List<StudySession>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _historyFilterSubject = MutableStateFlow<Subject?>(null)
    val historyFilterSubject: StateFlow<Subject?> = _historyFilterSubject.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    val filteredSessions: StateFlow<List<StudySession>> = combine(
        allSessions,
        _historyFilterSubject,
        _historySearchQuery
    ) { sessions, subjectFilter, query ->
        sessions.filter { session ->
            val matchesSubject = subjectFilter == null || session.subject == subjectFilter
            val matchesQuery = query.isBlank() ||
                    session.topic.contains(query, ignoreCase = true) ||
                    session.notes.contains(query, ignoreCase = true) ||
                    session.tag.contains(query, ignoreCase = true)
            matchesSubject && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setHistoryFilter(subject: Subject?) {
        _historyFilterSubject.value = subject
    }

    fun setHistorySearch(query: String) {
        _historySearchQuery.value = query
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            repository.deleteSession(id)
        }
    }

    // Manual Log Session Dialog
    private val _showManualLogDialog = MutableStateFlow(false)
    val showManualLogDialog: StateFlow<Boolean> = _showManualLogDialog.asStateFlow()

    fun openManualLogDialog() {
        _showManualLogDialog.value = true
    }

    fun closeManualLogDialog() {
        _showManualLogDialog.value = false
    }

    fun logManualSession(
        subject: Subject,
        topic: String,
        durationMinutes: Int,
        tag: String,
        date: LocalDate,
        rating: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val now = System.currentTimeMillis()
            val session = StudySession(
                subjectName = subject.name,
                topic = topic.ifBlank { "${subject.displayName} Study" },
                startTimeMillis = now - (durationMinutes * 60_000L),
                endTimeMillis = now,
                durationMinutes = durationMinutes,
                sessionType = "Manual",
                tag = tag,
                productivityRating = rating,
                notes = notes,
                dateKey = dateStr
            )
            repository.insertSession(session)
            _showManualLogDialog.value = false
        }
    }
}
