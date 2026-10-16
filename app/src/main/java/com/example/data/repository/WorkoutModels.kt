package com.example.data.repository

import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.MuscleGroup
import com.example.data.local.entity.RoutineExerciseEntity
import com.example.data.local.entity.SessionExerciseEntity
import com.example.data.local.entity.WorkoutRoutineEntity
import com.example.data.local.entity.WorkoutSessionEntity
import com.example.data.local.entity.WorkoutSetEntity

data class SessionExerciseDetail(
    val sessionExercise: SessionExerciseEntity,
    val exercise: ExerciseEntity,
    val sets: List<WorkoutSetEntity>
)

data class WorkoutSessionDetail(
    val session: WorkoutSessionEntity,
    val exercises: List<SessionExerciseDetail>
)

data class RoutineDetail(
    val routine: WorkoutRoutineEntity,
    val items: List<RoutineExerciseItemDetail>
)

data class RoutineExerciseItemDetail(
    val routineExercise: RoutineExerciseEntity,
    val exercise: ExerciseEntity
)

data class PersonalRecordItem(
    val exerciseId: Long,
    val exerciseName: String,
    val muscleGroup: MuscleGroup,
    val maxWeightKg: Double,
    val repsAtMaxWeight: Int,
    val estimated1RMKg: Double,
    val achievedDate: Long
)

data class WorkoutSummaryData(
    val sessionId: Long,
    val workoutName: String,
    val durationSeconds: Long,
    val completedExercisesCount: Int,
    val completedSetsCount: Int,
    val totalReps: Int,
    val totalVolumeKg: Double,
    val newRecordsAchieved: List<PersonalRecordItem>
)
