package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises",
    indices = [
        Index("muscleGroup"),
        Index("category")
    ]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val muscleGroup: MuscleGroup,
    val equipment: Equipment,
    val category: ExerciseCategory,
    val trackingType: TrackingType = TrackingType.WEIGHT_AND_REPS,
    val instructions: String = "",
    val isCustom: Boolean = false,
    val isArchived: Boolean = false
)
