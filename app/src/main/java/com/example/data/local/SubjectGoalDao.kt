package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SubjectGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectGoalDao {
    @Query("SELECT * FROM subject_goals")
    fun getAllGoals(): Flow<List<SubjectGoal>>

    @Query("SELECT * FROM subject_goals WHERE subjectName = :subjectName LIMIT 1")
    suspend fun getGoalForSubject(subjectName: String): SubjectGoal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGoal(goal: SubjectGoal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGoals(goals: List<SubjectGoal>)

    @Query("SELECT COUNT(*) FROM subject_goals")
    suspend fun getGoalCount(): Int
}
