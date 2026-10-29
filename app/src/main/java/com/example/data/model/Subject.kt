package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ChemistryEmerald
import com.example.ui.theme.ChemistryEmeraldGlow
import com.example.ui.theme.ChemistryEmeraldLight
import com.example.ui.theme.MathsAmber
import com.example.ui.theme.MathsAmberGlow
import com.example.ui.theme.MathsAmberLight
import com.example.ui.theme.PhysicsBlue
import com.example.ui.theme.PhysicsBlueGlow
import com.example.ui.theme.PhysicsBlueLight
import com.example.ui.theme.SelfStudyViolet
import com.example.ui.theme.SelfStudyVioletGlow
import com.example.ui.theme.SelfStudyVioletLight

enum class Subject(
    val id: String,
    val displayName: String,
    val shortName: String,
    val color: Color,
    val lightBgColor: Color,
    val glowColor: Color,
    val defaultWeeklyGoalMinutes: Int = 420, // 7 hours
    val defaultDailyGoalMinutes: Int = 60    // 1 hour
) {
    PHYSICS(
        id = "PHYSICS",
        displayName = "Physics",
        shortName = "PHY",
        color = PhysicsBlue,
        lightBgColor = PhysicsBlueLight,
        glowColor = PhysicsBlueGlow,
        defaultWeeklyGoalMinutes = 480, // 8 hours
        defaultDailyGoalMinutes = 70
    ),
    CHEMISTRY(
        id = "CHEMISTRY",
        displayName = "Chemistry",
        shortName = "CHEM",
        color = ChemistryEmerald,
        lightBgColor = ChemistryEmeraldLight,
        glowColor = ChemistryEmeraldGlow,
        defaultWeeklyGoalMinutes = 420, // 7 hours
        defaultDailyGoalMinutes = 60
    ),
    MATHEMATICS(
        id = "MATHEMATICS",
        displayName = "Maths",
        shortName = "MATH",
        color = MathsAmber,
        lightBgColor = MathsAmberLight,
        glowColor = MathsAmberGlow,
        defaultWeeklyGoalMinutes = 540, // 9 hours
        defaultDailyGoalMinutes = 80
    ),
    SELF_STUDY(
        id = "SELF_STUDY",
        displayName = "Self Study",
        shortName = "SELF",
        color = SelfStudyViolet,
        lightBgColor = SelfStudyVioletLight,
        glowColor = SelfStudyVioletGlow,
        defaultWeeklyGoalMinutes = 360, // 6 hours
        defaultDailyGoalMinutes = 50
    );

    companion object {
        fun fromName(name: String): Subject {
            return entries.firstOrNull {
                it.name.equals(name, ignoreCase = true) ||
                it.displayName.equals(name, ignoreCase = true) ||
                it.shortName.equals(name, ignoreCase = true)
            } ?: SELF_STUDY
        }
    }
}
