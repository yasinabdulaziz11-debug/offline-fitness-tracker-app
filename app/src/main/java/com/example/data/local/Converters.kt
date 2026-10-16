package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.local.entity.Equipment
import com.example.data.local.entity.ExerciseCategory
import com.example.data.local.entity.MuscleGroup
import com.example.data.local.entity.SessionStatus
import com.example.data.local.entity.SetType
import com.example.data.local.entity.TrackingType

class Converters {
    @TypeConverter
    fun fromMuscleGroup(value: MuscleGroup?): String? = value?.name

    @TypeConverter
    fun toMuscleGroup(value: String?): MuscleGroup? =
        value?.let { runCatching { MuscleGroup.valueOf(it) }.getOrDefault(MuscleGroup.FULL_BODY) }

    @TypeConverter
    fun fromEquipment(value: Equipment?): String? = value?.name

    @TypeConverter
    fun toEquipment(value: String?): Equipment? =
        value?.let { runCatching { Equipment.valueOf(it) }.getOrDefault(Equipment.OTHER) }

    @TypeConverter
    fun fromExerciseCategory(value: ExerciseCategory?): String? = value?.name

    @TypeConverter
    fun toExerciseCategory(value: String?): ExerciseCategory? =
        value?.let { runCatching { ExerciseCategory.valueOf(it) }.getOrDefault(ExerciseCategory.STRENGTH) }

    @TypeConverter
    fun fromTrackingType(value: TrackingType?): String? = value?.name

    @TypeConverter
    fun toTrackingType(value: String?): TrackingType? =
        value?.let { runCatching { TrackingType.valueOf(it) }.getOrDefault(TrackingType.WEIGHT_AND_REPS) }

    @TypeConverter
    fun fromSetType(value: SetType?): String? = value?.name

    @TypeConverter
    fun toSetType(value: String?): SetType? =
        value?.let { runCatching { SetType.valueOf(it) }.getOrDefault(SetType.NORMAL) }

    @TypeConverter
    fun fromSessionStatus(value: SessionStatus?): String? = value?.name

    @TypeConverter
    fun toSessionStatus(value: String?): SessionStatus? =
        value?.let { runCatching { SessionStatus.valueOf(it) }.getOrDefault(SessionStatus.COMPLETED) }
}
