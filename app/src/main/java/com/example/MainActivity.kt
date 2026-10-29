package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.analytics.AnalyticsScreen
import com.example.ui.screens.components.ManualSessionDialog
import com.example.ui.screens.goals.GoalsScreen
import com.example.ui.screens.history.HistoryScreen
import com.example.ui.screens.timer.TimerScreen
import com.example.ui.theme.StudyPulseTheme
import com.example.ui.viewmodel.StudyViewModel

enum class StudyNavDestination(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    TIMER("Timer", Icons.Default.HourglassTop, "nav_timer"),
    ANALYTICS("Analytics", Icons.Default.BarChart, "nav_analytics"),
    GOALS("Goals", Icons.Default.TrackChanges, "nav_goals"),
    HISTORY("History", Icons.AutoMirrored.Filled.List, "nav_history")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyPulseTheme {
                StudyPulseApp()
            }
        }
    }
}

@Composable
fun StudyPulseApp(
    viewModel: StudyViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(StudyNavDestination.TIMER) }
    val showManualLog by viewModel.showManualLogDialog.collectAsStateWithLifecycle()
    val activeSubject by viewModel.selectedSubject.collectAsStateWithLifecycle()

    // BackHandler: if on any secondary screen, back button brings user back to Timer screen
    if (currentScreen != StudyNavDestination.TIMER) {
        BackHandler {
            currentScreen = StudyNavDestination.TIMER
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                StudyNavDestination.entries.forEach { destination ->
                    val isSelected = currentScreen == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title
                            )
                        },
                        label = { Text(destination.title) },
                        modifier = Modifier.testTag(destination.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                StudyNavDestination.TIMER -> {
                    TimerScreen(
                        viewModel = viewModel,
                        onOpenManualLog = { viewModel.openManualLogDialog() }
                    )
                }
                StudyNavDestination.ANALYTICS -> {
                    AnalyticsScreen(
                        viewModel = viewModel
                    )
                }
                StudyNavDestination.GOALS -> {
                    GoalsScreen(
                        viewModel = viewModel
                    )
                }
                StudyNavDestination.HISTORY -> {
                    HistoryScreen(
                        viewModel = viewModel,
                        onOpenManualLog = { viewModel.openManualLogDialog() }
                    )
                }
            }
        }
    }

    // Modal dialog for manual log entry
    if (showManualLog) {
        ManualSessionDialog(
            initialSubject = activeSubject,
            onSaveSession = { subject, topic, durationMinutes, tag, date, rating, notes ->
                viewModel.logManualSession(
                    subject = subject,
                    topic = topic,
                    durationMinutes = durationMinutes,
                    tag = tag,
                    date = date,
                    rating = rating,
                    notes = notes
                )
            },
            onDismiss = {
                viewModel.closeManualLogDialog()
            }
        )
    }
}
