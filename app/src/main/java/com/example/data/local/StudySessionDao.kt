package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.StudySession
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY startTimeMillis DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE dateKey BETWEEN :startDateKey AND :endDateKey ORDER BY startTimeMillis DESC")
    fun getSessionsBetweenDates(startDateKey: String, endDateKey: String): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE subjectName = :subjectName ORDER BY startTimeMillis DESC")
    fun getSessionsBySubject(subjectName: String): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE dateKey = :dateKey ORDER BY startTimeMillis DESC")
    fun getSessionsForDate(dateKey: String): Flow<List<StudySession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<StudySession>)

    @Delete
    suspend fun deleteSession(session: StudySession)

    @Query("DELETE FROM study_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("SELECT COUNT(*) FROM study_sessions")
    suspend fun getSessionCount(): Int
}
