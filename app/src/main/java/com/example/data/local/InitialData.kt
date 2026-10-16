package com.example.data.local

import com.example.data.local.entity.Equipment
import com.example.data.local.entity.ExerciseCategory
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.MuscleGroup
import com.example.data.local.entity.RoutineExerciseEntity
import com.example.data.local.entity.TrackingType
import com.example.data.local.entity.WorkoutRoutineEntity

object InitialData {
    val defaultExercises = listOf(
        // Chest
        ExerciseEntity(
            id = 1,
            name = "Barbell Bench Press",
            muscleGroup = MuscleGroup.CHEST,
            equipment = Equipment.BARBELL,
            category = ExerciseCategory.STRENGTH,
            trackingType = TrackingType.WEIGHT_AND_REPS,
            instructions = "Lie on a flat bench. Retract shoulder blades, grip bar slightly wider than shoulder width. Lower to mid-chest with control, press up until arms lock out safely."
        ),
        ExerciseEntity(
            id = 2,
            name = "Incline Dumbbell Press",
            muscleGroup = MuscleGroup.CHEST,
            equipment = Equipment.DUMBBELL,
            category = ExerciseCategory.HYPERTROPHY,
            trackingType = TrackingType.WEIGHT_AND_REPS,
            instructions = "Set bench to 30-45 degrees. Press dumbbells upward together while maintaining stable footing and retracted scapulae."
        ),
        ExerciseEntity(
            id = 3,
            name = "Push-Ups",
            muscleGroup = MuscleGroup.CHEST,
            equipment = Equipment.BODYWEIGHT,
            category = ExerciseCategory.ENDURANCE,
            trackingType = TrackingType.REPS_ONLY,
            instructions = "Place hands shoulder-width apart. Maintain a rigid plank torso from heels to head. Lower chest to floor and push up smoothly."
        ),
        ExerciseEntity(
            id = 4,
            name = "Cable Chest Fly",
            muscleGroup = MuscleGroup.CHEST,
            equipment = Equipment.CABLE,
            category = ExerciseCategory.HYPERTROPHY,
            trackingType = TrackingType.WEIGHT_AND_REPS,
            instructions = "Set pulleys at chest level. Step forward slightly with a slight elbow bend. Bring hands together in an arc motion focusing on chest squeeze."
        ),

        // Back
        ExerciseEntity(
            id = 5,
            name = "Conventional Deadlift",
            muscleGroup = MuscleGroup.BACK,
            equipment = Equipment.BARBELL,
            category = ExerciseCategory.STRENGTH,
            trackingType = TrackingType.WEIGHT_AND_REPS,
            instructions = "Stand mid-foot under barbell. Hinge hips back, grip bar outside shins. Brace core, engage lats, and drive through heels to full standing lockout."
        ),
        ExerciseEntity(
            id = 6,
            name = "Pull-Ups",
            muscleGroup = MuscleGroup.BACK,
            equipment = Equipment.BODYWEIGHT,
            category = ExerciseCategory.STRENGTH,
            trackingType = TrackingType.REPS_ONLY,
            instructions = "Grip bar overhand slightly wider than shoulders. Depress shoulders and pull chest towards bar until chin clears the bar."
        ),
        ExerciseEntity(
            id = 7,
            name = "Barbell Bent-Over Row",
            muscleGroup = MuscleGroup.BACK,
            equipment = Equipment.BARBELL,
            category = ExerciseCategory.STRENGTH,
            trackingType = TrackingType.WEIGHT_AND_REPS,
            instructions = "Hinge forward at roughly 45 degrees with flat spine. Pull bar towards lower abdomen, pinching shoulder blades at the top."
        ),
        ExerciseEntity(
            id = 8,
            name = "Lat Pulldown",
            muscleGroup = MuscleGroup.BACK,
            equipment = Equipment.CABLE,
            category = ExerciseCategory.HYPERTROPHY,
            trackingType = TrackingType.WEIGHT_AND_REPS,
            instructions = "Sit upright with knees secured under pads. Pull bar down towards upper chest while keeping elbows pointing down."
        ),

        // Shoulders
        ExerciseEntity(
            id = 9,
            name = "Overhead Barbell Press",
            muscleGroup = MuscleGroup.SHOULDERS,
            equipment = Equipment.BARBELL,
            category = ExerciseCategory.STRENGTH,
            trackingType = TrackingType.WEIGHT_AND_REPS,
            instructions = "Rest bar on anterior shoulders. Squeeze glutes, brace abs, and press vertically directly over crown of head."
        ),
        ExerciseEntity(
            id = 10,
            name = "Dumbbell Lateral Raise",
            muscleGroup = MuscleGroup.SHOULDERS,
            equipment = Equipment.DUMBBELL,
            category = ExerciseCategory.HYPERTROPHY,
            trackingType = TrackingType.WEIGHT_AND_REPS,
            instructions = "Stand upright with dumbbells at sides. Raise arms to shoulder height leading with elbows with a slight forward angle."
        ),
        ExerciseEntity(
            id = 11,
            name = "Face Pull",
            muscleGroup = MuscleGroup.SHOULDERS,
            equipment = Equipment.CABLE,
            category = ExerciseCategory.HYPERTROPHY,
            trackingType = TrackingType.WEIGHT_AND_REPS,
            instructions = "Set cable rope at eye height. Pull rope towards bridge of nose while rotating thumbs backwards to train rear delts and rotator cuffs."
        ),

        // Arms
        ExerciseEntity(
            id = 12,
            name = "Barbell Bicep Curl",
            muscleGroup = MuscleGroup.ARMS,
            equipment = Equipment.BARBELL,
            category = ExerciseCategory.HYPERTROPHY,
            trackingType = TrackingType.WEIGHT_AND_REPS,
            instructions = "Hold bar underhand with elbows pinned to sides. Curl upwards without swinging torso, contracting biceps at apex."
        ),
        ExerciseEntity(
            id = 13,
            name = "Tricep Rope Pushdown",
            muscleGroup = MuscleGroup.ARMS,
            equipment = Equipment.CABLE,
            category = ExerciseCategory.HYPERTROPHY,
            trackingType = TrackingType.WEIGHT_AND_REPS,
            instructions = "Stand tall, hold rope with neutral grip. Extend elbows downwards and spread rope ends at lockout."
        ),
        ExerciseEntity(
            id = 14,
            name = "Dips",
            muscleGroup = MuscleGroup.ARMS,
            equipment = Equipment.BODYWEIGHT,
            category = ExerciseCategory.STRENGTH,
            trackingType = TrackingType.REPS_ONLY,
            instructions = "Suspend body on parallel bars. Lower until upper arms are parallel to floor, then press firmly back to top."
        ),

        // Legs
        ExerciseEntity(
            id = 15,
            name = "Barbell Back Squat",
            muscleGroup = MuscleGroup.LEGS,
            equipment = Equipment.BARBELL,
            category = ExerciseCategory.STRENGTH,
            trackingType = TrackingType.WEIGHT_AND_REPS,
            instructions = "Place bar securely across upper traps. Descend under control until hip crease is below knee level, maintaining neutral lumbar curve."
        ),
        ExerciseEntity(
            id = 16,
            name = "Romanian Deadlift",
            muscleGroup = MuscleGroup.LEGS,
            equipment = Equipment.BARBELL,
            category = ExerciseCategory.STRENGTH,
            trackingType = TrackingType.WEIGHT_AND_REPS,
            instructions = "With soft knees, push hips backward while tracing bar close down thighs until hamstrings stretch, then drive hips forward."
        ),
        ExerciseEntity(
            id = 17,
            name = "Leg Press",
            muscleGroup = MuscleGroup.LEGS,
            equipment = Equipment.MACHINE,
            category = ExerciseCategory.HYPERTROPHY,
            trackingType = TrackingType.WEIGHT_AND_REPS,
            instructions = "Seat firmly against back pad. Place feet shoulder-width on platform. Lower sled under control without rounding lower back."
        ),
        ExerciseEntity(
            id = 18,
            name = "Walking Lunges",
            muscleGroup = MuscleGroup.LEGS,
            equipment = Equipment.DUMBBELL,
            category = ExerciseCategory.HYPERTROPHY,
            trackingType = TrackingType.WEIGHT_AND_REPS,
            instructions = "Step forward into a deep lunge with back knee gently kissing the ground. Drive through front heel to step into next repetition."
        ),

        // Glutes
        ExerciseEntity(
            id = 19,
            name = "Barbell Hip Thrust",
            muscleGroup = MuscleGroup.GLUTES,
            equipment = Equipment.BARBELL,
            category = ExerciseCategory.STRENGTH,
            trackingType = TrackingType.WEIGHT_AND_REPS,
            instructions = "Position upper back against padded bench with bar padded across hips. Drive through heels to full hip extension, pausing at top."
        ),

        // Core
        ExerciseEntity(
            id = 20,
            name = "Plank",
            muscleGroup = MuscleGroup.CORE,
            equipment = Equipment.BODYWEIGHT,
            category = ExerciseCategory.ENDURANCE,
            trackingType = TrackingType.DURATION_ONLY,
            instructions = "Hold forearms and toes on ground with rigid neutral spine. Squeeze glutes and abdominal wall continuously."
        ),
        ExerciseEntity(
            id = 21,
            name = "Hanging Leg Raise",
            muscleGroup = MuscleGroup.CORE,
            equipment = Equipment.BODYWEIGHT,
            category = ExerciseCategory.STRENGTH,
            trackingType = TrackingType.REPS_ONLY,
            instructions = "Hang from pull-up bar. Without swinging, curl pelvis upward bringing legs to at least 90 degrees."
        ),

        // Cardio
        ExerciseEntity(
            id = 22,
            name = "Running (Treadmill or Outdoor)",
            muscleGroup = MuscleGroup.CARDIO,
            equipment = Equipment.OTHER,
            category = ExerciseCategory.CARDIO,
            trackingType = TrackingType.DURATION_AND_DISTANCE,
            instructions = "Maintain upright posture, relaxed shoulders, and steady midfoot strike cadence."
        ),
        ExerciseEntity(
            id = 23,
            name = "Rowing Machine",
            muscleGroup = MuscleGroup.CARDIO,
            equipment = Equipment.MACHINE,
            category = ExerciseCategory.CARDIO,
            trackingType = TrackingType.DURATION_AND_DISTANCE,
            instructions = "Drive with legs first, swing torso back, and finish pull with arms to sternum. Reverse sequence smoothly on recovery."
        )
    )

    val defaultRoutines = listOf(
        WorkoutRoutineEntity(
            id = 1,
            name = "Full Body Foundation",
            description = "Balanced 5-exercise strength routine targeting all major muscle groups.",
            isTemplate = true
        ),
        WorkoutRoutineEntity(
            id = 2,
            name = "Upper Body Hypertrophy",
            description = "Comprehensive upper body session focusing on chest, back, shoulders, and arms.",
            isTemplate = true
        ),
        WorkoutRoutineEntity(
            id = 3,
            name = "Lower Body & Core",
            description = "Solid quad, hamstring, glute, and abdominal session.",
            isTemplate = true
        )
    )

    val defaultRoutineExercises = listOf(
        // Routine 1: Full Body Foundation
        RoutineExerciseEntity(id = 1, routineId = 1, exerciseId = 15, orderIndex = 0, defaultSets = 3, defaultReps = 8, defaultWeightKg = 60.0), // Squat
        RoutineExerciseEntity(id = 2, routineId = 1, exerciseId = 1, orderIndex = 1, defaultSets = 3, defaultReps = 8, defaultWeightKg = 50.0), // Bench
        RoutineExerciseEntity(id = 3, routineId = 1, exerciseId = 7, orderIndex = 2, defaultSets = 3, defaultReps = 10, defaultWeightKg = 40.0), // Row
        RoutineExerciseEntity(id = 4, routineId = 1, exerciseId = 9, orderIndex = 3, defaultSets = 3, defaultReps = 8, defaultWeightKg = 30.0), // OHP
        RoutineExerciseEntity(id = 5, routineId = 1, exerciseId = 20, orderIndex = 4, defaultSets = 3, defaultDurationSeconds = 60), // Plank

        // Routine 2: Upper Body
        RoutineExerciseEntity(id = 6, routineId = 2, exerciseId = 2, orderIndex = 0, defaultSets = 3, defaultReps = 10, defaultWeightKg = 22.0), // Incline DB
        RoutineExerciseEntity(id = 7, routineId = 2, exerciseId = 6, orderIndex = 1, defaultSets = 3, defaultReps = 8), // Pull-ups
        RoutineExerciseEntity(id = 8, routineId = 2, exerciseId = 10, orderIndex = 2, defaultSets = 3, defaultReps = 12, defaultWeightKg = 10.0), // Lateral raise
        RoutineExerciseEntity(id = 9, routineId = 2, exerciseId = 12, orderIndex = 3, defaultSets = 3, defaultReps = 10, defaultWeightKg = 25.0), // Bicep curl
        RoutineExerciseEntity(id = 10, routineId = 2, exerciseId = 13, orderIndex = 4, defaultSets = 3, defaultReps = 12, defaultWeightKg = 20.0), // Tricep rope

        // Routine 3: Lower Body & Core
        RoutineExerciseEntity(id = 11, routineId = 3, exerciseId = 15, orderIndex = 0, defaultSets = 4, defaultReps = 8, defaultWeightKg = 70.0), // Squat
        RoutineExerciseEntity(id = 12, routineId = 3, exerciseId = 16, orderIndex = 1, defaultSets = 3, defaultReps = 10, defaultWeightKg = 50.0), // RDL
        RoutineExerciseEntity(id = 13, routineId = 3, exerciseId = 19, orderIndex = 2, defaultSets = 3, defaultReps = 10, defaultWeightKg = 60.0), // Hip thrust
        RoutineExerciseEntity(id = 14, routineId = 3, exerciseId = 21, orderIndex = 3, defaultSets = 3, defaultReps = 12) // Leg raise
    )
}
