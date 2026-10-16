package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.SessionExerciseEntity
import com.example.data.local.entity.SessionStatus
import com.example.data.local.entity.WorkoutSessionEntity
import com.example.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    @Query("SELECT * FROM workout_sessions WHERE status = 'ACTIVE' ORDER BY startTime DESC LIMIT 1")
    fun getActiveSessionFlow(): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sessions WHERE status = 'ACTIVE' ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSession(): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE status = 'COMPLETED' ORDER BY startTime DESC")
    fun getCompletedSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE id = :id LIMIT 1")
    fun getSessionByIdFlow(id: Long): Flow<WorkoutSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Delete
    suspend fun deleteSession(session: WorkoutSessionEntity)

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    // Session Exercises
    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    fun getSessionExercisesFlow(sessionId: Long): Flow<List<SessionExerciseEntity>>

    @Query("SELECT * FROM session_exercises WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    suspend fun getSessionExercises(sessionId: Long): List<SessionExerciseEntity>

    @Query("SELECT * FROM session_exercises")
    suspend fun getAllSessionExercises(): List<SessionExerciseEntity>

    @Query("SELECT * FROM session_exercises")
    fun getAllSessionExercisesFlow(): Flow<List<SessionExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionExercise(exercise: SessionExerciseEntity): Long

    @Update
    suspend fun updateSessionExercise(exercise: SessionExerciseEntity)

    @Query("DELETE FROM session_exercises WHERE id = :id")
    suspend fun deleteSessionExerciseById(id: Long)

    // Workout Sets
    @Query("SELECT * FROM workout_sets WHERE sessionExerciseId = :sessionExerciseId ORDER BY setNumber ASC")
    fun getSetsForExerciseFlow(sessionExerciseId: Long): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM workout_sets WHERE sessionExerciseId = :sessionExerciseId ORDER BY setNumber ASC")
    suspend fun getSetsForExercise(sessionExerciseId: Long): List<WorkoutSetEntity>

    @Query("SELECT * FROM workout_sets WHERE sessionExerciseId IN (:sessionExerciseIds) ORDER BY setNumber ASC")
    suspend fun getSetsForExercises(sessionExerciseIds: List<Long>): List<WorkoutSetEntity>

    @Query("SELECT * FROM workout_sets")
    suspend fun getAllSets(): List<WorkoutSetEntity>

    @Query("SELECT * FROM workout_sets")
    fun getAllSetsFlow(): Flow<List<WorkoutSetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: WorkoutSetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<WorkoutSetEntity>): List<Long>

    @Update
    suspend fun updateSet(set: WorkoutSetEntity)

    @Query("DELETE FROM workout_sets WHERE id = :id")
    suspend fun deleteSetById(id: Long)

    // PR queries
    @Query("""
        SELECT ws.* FROM workout_sets ws
        INNER JOIN session_exercises se ON ws.sessionExerciseId = se.id
        INNER JOIN workout_sessions s ON se.sessionId = s.id
        WHERE se.exerciseId = :exerciseId AND ws.isCompleted = 1 AND s.status = 'COMPLETED'
        ORDER BY ws.weightKg DESC, ws.reps DESC
    """)
    suspend fun getCompletedSetsForExercise(exerciseId: Long): List<WorkoutSetEntity>

    @Query("""
        SELECT ws.* FROM workout_sets ws
        INNER JOIN session_exercises se ON ws.sessionExerciseId = se.id
        INNER JOIN workout_sessions s ON se.sessionId = s.id
        WHERE ws.isCompleted = 1 AND s.status = 'COMPLETED'
    """)
    suspend fun getAllCompletedSets(): List<WorkoutSetEntity>

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE status = 'COMPLETED'")
    suspend fun getCompletedCount(): Int
}
