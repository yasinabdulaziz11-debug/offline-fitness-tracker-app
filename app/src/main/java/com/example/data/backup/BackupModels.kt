package com.example.data.backup

import com.example.data.local.entity.BodyMeasurementEntity
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.WorkoutRoutineEntity
import com.example.data.local.entity.WorkoutSessionEntity
import com.example.data.local.entity.WorkoutSetEntity

data class BackupSessionItem(
    val session: WorkoutSessionEntity,
    val exerciseNames: List<String>,
    val sets: List<WorkoutSetEntity>
)

data class BackupRoutineItem(
    val routine: WorkoutRoutineEntity,
    val exercises: List<BackupRoutineExerciseItem>
)

data class BackupRoutineExerciseItem(
    val exerciseName: String,
    val orderIndex: Int,
    val defaultSets: Int,
    val defaultReps: Int,
    val defaultWeightKg: Double,
    val defaultDurationSeconds: Int
)

data class BackupPayload(
    val schemaVersion: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0",
    val customExercises: List<ExerciseEntity> = emptyList(),
    val routines: List<BackupRoutineItem> = emptyList(),
    val completedSessions: List<BackupSessionItem> = emptyList(),
    val measurements: List<BodyMeasurementEntity> = emptyList()
)

data class BackupPreview(
    val valid: Boolean,
    val schemaVersion: Int,
    val exportedAt: Long,
    val customExercisesCount: Int,
    val routinesCount: Int,
    val workoutsCount: Int,
    val measurementsCount: Int,
    val errorMessage: String? = null
)
