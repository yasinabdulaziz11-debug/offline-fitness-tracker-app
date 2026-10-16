package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.RoutineExerciseEntity
import com.example.data.local.entity.WorkoutRoutineEntity
import kotlinx.coroutines.flow.Flow

data class RoutineWithExercises(
    val routine: WorkoutRoutineEntity,
    val items: List<RoutineExerciseItem>
)

data class RoutineExerciseItem(
    val routineExercise: RoutineExerciseEntity,
    val exercise: ExerciseEntity
)

@Dao
interface RoutineDao {
    @Query("SELECT * FROM workout_routines ORDER BY isTemplate DESC, createdAt DESC")
    fun getAllRoutines(): Flow<List<WorkoutRoutineEntity>>

    @Query("SELECT * FROM workout_routines WHERE id = :id LIMIT 1")
    suspend fun getRoutineById(id: Long): WorkoutRoutineEntity?

    @Query("SELECT * FROM workout_routines WHERE id = :id LIMIT 1")
    fun getRoutineByIdFlow(id: Long): Flow<WorkoutRoutineEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: WorkoutRoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: WorkoutRoutineEntity)

    @Delete
    suspend fun deleteRoutine(routine: WorkoutRoutineEntity)

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    fun getRoutineExercisesFlow(routineId: Long): Flow<List<RoutineExerciseEntity>>

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    suspend fun getRoutineExercises(routineId: Long): List<RoutineExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercises(items: List<RoutineExerciseEntity>)

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteRoutineExercises(routineId: Long)

    @Transaction
    suspend fun replaceRoutineExercises(routineId: Long, items: List<RoutineExerciseEntity>) {
        deleteRoutineExercises(routineId)
        insertRoutineExercises(items)
    }

    @Query("SELECT COUNT(*) FROM workout_routines")
    suspend fun getCount(): Int
}
