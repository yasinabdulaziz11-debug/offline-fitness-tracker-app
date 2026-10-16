package com.example.data.local.entity

enum class MuscleGroup(val displayName: String) {
    CHEST("Chest"),
    BACK("Back"),
    SHOULDERS("Shoulders"),
    ARMS("Arms"),
    LEGS("Legs"),
    GLUTES("Glutes"),
    CORE("Core"),
    CARDIO("Cardio"),
    FULL_BODY("Full Body")
}

enum class Equipment(val displayName: String) {
    BARBELL("Barbell"),
    DUMBBELL("Dumbbell"),
    MACHINE("Machine"),
    CABLE("Cable"),
    BODYWEIGHT("Bodyweight"),
    BAND("Resistance Band"),
    KETTLEBELL("Kettlebell"),
    OTHER("Other")
}

enum class ExerciseCategory(val displayName: String) {
    STRENGTH("Strength"),
    HYPERTROPHY("Hypertrophy"),
    ENDURANCE("Endurance"),
    MOBILITY("Mobility"),
    CARDIO("Cardio")
}

enum class TrackingType {
    WEIGHT_AND_REPS,
    REPS_ONLY,
    DURATION_AND_DISTANCE,
    DURATION_ONLY
}

enum class SetType(val shortName: String, val displayName: String) {
    NORMAL("N", "Normal"),
    WARMUP("W", "Warm-up"),
    DROP("D", "Drop Set"),
    FAILURE("F", "To Failure"),
    TIMED("T", "Timed")
}

enum class SessionStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}

enum class WeightUnit(val symbol: String) {
    KG("kg"),
    LBS("lbs")
}

enum class LengthUnit(val symbol: String) {
    CM("cm"),
    INCH("in")
}

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}
