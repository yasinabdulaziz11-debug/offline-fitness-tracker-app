package com.example.data.repository

import com.example.core.Calculators
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BodyMeasurementEntity
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.MuscleGroup
import com.example.data.local.entity.RoutineExerciseEntity
import com.example.data.local.entity.SessionExerciseEntity
import com.example.data.local.entity.SessionStatus
import com.example.data.local.entity.SetType
import com.example.data.local.entity.TrackingType
import com.example.data.local.entity.WorkoutRoutineEntity
import com.example.data.local.entity.WorkoutSessionEntity
import com.example.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class WorkoutRepository(private val database: AppDatabase) {

    private val exerciseDao = database.exerciseDao()
    private val routineDao = database.routineDao()
    private val sessionDao = database.workoutSessionDao()
    private val measurementDao = database.measurementDao()

    // --- Active Workout Flow ---
    val activeSessionFlow: Flow<WorkoutSessionEntity?> = sessionDao.getActiveSessionFlow()

    fun getActiveSessionDetailFlow(): Flow<WorkoutSessionDetail?> = flow {
        sessionDao.getActiveSessionFlow().collect { activeSession ->
            if (activeSession == null) {
                emit(null)
            } else {
                emit(getSessionDetail(activeSession.id))
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getActiveSession(): WorkoutSessionEntity? = withContext(Dispatchers.IO) {
        sessionDao.getActiveSession()
    }

    suspend fun startWorkout(name: String, routineId: Long? = null): Long = withContext(Dispatchers.IO) {
        val existing = sessionDao.getActiveSession()
        if (existing != null) {
            return@withContext existing.id
        }
        val session = WorkoutSessionEntity(
            name = name.ifBlank { "Quick Workout" },
            routineId = routineId,
            startTime = System.currentTimeMillis(),
            status = SessionStatus.ACTIVE
        )
        sessionDao.insertSession(session)
    }

    suspend fun startWorkoutFromRoutine(routineId: Long): Long = withContext(Dispatchers.IO) {
        val routine = routineDao.getRoutineById(routineId) ?: return@withContext 0L
        val routineExercises = routineDao.getRoutineExercises(routineId)

        val sessionId = startWorkout(routine.name, routineId)

        routineExercises.sortedBy { it.orderIndex }.forEachIndexed { index, item ->
            val exercise = exerciseDao.getExerciseById(item.exerciseId) ?: return@forEachIndexed
            val sessionExerciseId = sessionDao.insertSessionExercise(
                SessionExerciseEntity(
                    sessionId = sessionId,
                    exerciseId = exercise.id,
                    orderIndex = index
                )
            )

            // Add default sets based on routine configuration
            val sets = (1..item.defaultSets).map { setNum ->
                WorkoutSetEntity(
                    sessionExerciseId = sessionExerciseId,
                    setNumber = setNum,
                    setType = SetType.NORMAL,
                    weightKg = if (exercise.trackingType == TrackingType.WEIGHT_AND_REPS) item.defaultWeightKg else null,
                    reps = if (exercise.trackingType != TrackingType.DURATION_ONLY) item.defaultReps else null,
                    durationSeconds = if (exercise.trackingType == TrackingType.DURATION_ONLY || exercise.trackingType == TrackingType.DURATION_AND_DISTANCE) item.defaultDurationSeconds else null
                )
            }
            sessionDao.insertSets(sets)
        }

        sessionId
    }

    suspend fun addExerciseToActiveSession(exerciseId: Long): Long = withContext(Dispatchers.IO) {
        val active = sessionDao.getActiveSession() ?: return@withContext -1L
        val existingExercises = sessionDao.getSessionExercises(active.id)
        val nextOrder = existingExercises.size
        val exercise = exerciseDao.getExerciseById(exerciseId) ?: return@withContext -1L

        val sessionExerciseId = sessionDao.insertSessionExercise(
            SessionExerciseEntity(
                sessionId = active.id,
                exerciseId = exerciseId,
                orderIndex = nextOrder
            )
        )

        // Add 1 initial default set
        sessionDao.insertSet(
            WorkoutSetEntity(
                sessionExerciseId = sessionExerciseId,
                setNumber = 1,
                setType = SetType.NORMAL,
                weightKg = if (exercise.trackingType == TrackingType.WEIGHT_AND_REPS) 20.0 else null,
                reps = if (exercise.trackingType != TrackingType.DURATION_ONLY) 10 else null,
                durationSeconds = if (exercise.trackingType == TrackingType.DURATION_ONLY) 60 else null
            )
        )

        sessionExerciseId
    }

    suspend fun removeExerciseFromSession(sessionExerciseId: Long) = withContext(Dispatchers.IO) {
        sessionDao.deleteSessionExerciseById(sessionExerciseId)
    }

    suspend fun addSetToExercise(sessionExerciseId: Long) = withContext(Dispatchers.IO) {
        val existingSets = sessionDao.getSetsForExercise(sessionExerciseId)
        val lastSet = existingSets.lastOrNull()
        val nextNumber = (lastSet?.setNumber ?: 0) + 1

        val newSet = WorkoutSetEntity(
            sessionExerciseId = sessionExerciseId,
            setNumber = nextNumber,
            setType = lastSet?.setType ?: SetType.NORMAL,
            weightKg = lastSet?.weightKg ?: 0.0,
            reps = lastSet?.reps ?: 10,
            durationSeconds = lastSet?.durationSeconds,
            distanceMeters = lastSet?.distanceMeters,
            isCompleted = false
        )
        sessionDao.insertSet(newSet)
    }

    suspend fun updateSet(set: WorkoutSetEntity) = withContext(Dispatchers.IO) {
        sessionDao.updateSet(set)
    }

    suspend fun removeSet(setId: Long) = withContext(Dispatchers.IO) {
        sessionDao.deleteSetById(setId)
    }

    suspend fun cancelActiveWorkout(sessionId: Long) = withContext(Dispatchers.IO) {
        sessionDao.deleteSessionById(sessionId)
    }

    suspend fun finishWorkout(sessionId: Long, finalDurationSeconds: Long, notes: String): WorkoutSummaryData = withContext(Dispatchers.IO) {
        val session = sessionDao.getSessionById(sessionId) ?: throw IllegalStateException("Session not found")

        // Guard against duplicate finish
        if (session.status == SessionStatus.COMPLETED) {
            return@withContext calculateSummary(session)
        }

        // Before completing, query previous PRs
        val previousPRs = getAllPersonalRecords()

        val updated = session.copy(
            endTime = System.currentTimeMillis(),
            durationSeconds = finalDurationSeconds,
            notes = notes,
            status = SessionStatus.COMPLETED
        )
        sessionDao.updateSession(updated)

        // Now calculate new PRs achieved in this workout
        val currentPRs = getAllPersonalRecords()
        val newPRs = currentPRs.filter { curr ->
            val prev = previousPRs.find { it.exerciseId == curr.exerciseId }
            prev == null || curr.maxWeightKg > prev.maxWeightKg
        }

        calculateSummary(updated, newPRs)
    }

    private suspend fun calculateSummary(session: WorkoutSessionEntity, newPRs: List<PersonalRecordItem> = emptyList()): WorkoutSummaryData {
        val detail = getSessionDetail(session.id)
        var totalSets = 0
        var completedSets = 0
        var totalReps = 0
        var totalVolumeKg = 0.0
        var completedExercises = 0

        detail?.exercises?.forEach { exDetail ->
            val completedInExercise = exDetail.sets.filter { it.isCompleted }
            if (completedInExercise.isNotEmpty()) {
                completedExercises++
            }
            totalSets += exDetail.sets.size
            completedSets += completedInExercise.size

            completedInExercise.forEach { set ->
                val reps = set.reps ?: 0
                val weight = set.weightKg ?: 0.0
                totalReps += reps
                if (weight > 0 && reps > 0) {
                    totalVolumeKg += (weight * reps)
                }
            }
        }

        return WorkoutSummaryData(
            sessionId = session.id,
            workoutName = session.name,
            durationSeconds = session.durationSeconds,
            completedExercisesCount = completedExercises,
            completedSetsCount = completedSets,
            totalReps = totalReps,
            totalVolumeKg = totalVolumeKg,
            newRecordsAchieved = newPRs
        )
    }

    // --- Session Detail queries ---
    suspend fun getSessionDetail(sessionId: Long): WorkoutSessionDetail? = withContext(Dispatchers.IO) {
        val session = sessionDao.getSessionById(sessionId) ?: return@withContext null
        val sessionExercises = sessionDao.getSessionExercises(sessionId)

        val exerciseDetails = sessionExercises.mapNotNull { se ->
            val exercise = exerciseDao.getExerciseById(se.exerciseId) ?: return@mapNotNull null
            val sets = sessionDao.getSetsForExercise(se.id)
            SessionExerciseDetail(
                sessionExercise = se,
                exercise = exercise,
                sets = sets
            )
        }

        WorkoutSessionDetail(
            session = session,
            exercises = exerciseDetails
        )
    }

    fun getCompletedSessionsFlow(): Flow<List<WorkoutSessionEntity>> = sessionDao.getCompletedSessions()

    suspend fun updateSession(sessionId: Long, name: String, notes: String) = withContext(Dispatchers.IO) {
        val session = sessionDao.getSessionById(sessionId) ?: return@withContext
        sessionDao.updateSession(session.copy(name = name, notes = notes))
    }

    suspend fun deleteSession(sessionId: Long) = withContext(Dispatchers.IO) {
        sessionDao.deleteSessionById(sessionId)
    }

    // --- Exercise Library ---
    fun getAllExercisesFlow(): Flow<List<ExerciseEntity>> = exerciseDao.getActiveExercises()

    suspend fun getAllExercisesList(): List<ExerciseEntity> = withContext(Dispatchers.IO) {
        exerciseDao.getActiveExercises().firstOrNull() ?: emptyList()
    }

    suspend fun getExerciseById(id: Long): ExerciseEntity? = withContext(Dispatchers.IO) {
        exerciseDao.getExerciseById(id)
    }

    suspend fun insertCustomExercise(exercise: ExerciseEntity): Long = withContext(Dispatchers.IO) {
        exerciseDao.insertExercise(exercise.copy(isCustom = true))
    }

    suspend fun updateExercise(exercise: ExerciseEntity) = withContext(Dispatchers.IO) {
        exerciseDao.updateExercise(exercise)
    }

    suspend fun archiveExercise(id: Long) = withContext(Dispatchers.IO) {
        exerciseDao.setArchived(id, true)
    }

    // --- Routines ---
    fun getAllRoutinesFlow(): Flow<List<WorkoutRoutineEntity>> = routineDao.getAllRoutines()

    suspend fun getRoutineDetail(routineId: Long): RoutineDetail? = withContext(Dispatchers.IO) {
        val routine = routineDao.getRoutineById(routineId) ?: return@withContext null
        val items = routineDao.getRoutineExercises(routineId)
        val details = items.mapNotNull { item ->
            val exercise = exerciseDao.getExerciseById(item.exerciseId) ?: return@mapNotNull null
            RoutineExerciseItemDetail(routineExercise = item, exercise = exercise)
        }
        RoutineDetail(routine = routine, items = details)
    }

    suspend fun saveRoutine(
        routine: WorkoutRoutineEntity,
        exercises: List<RoutineExerciseEntity>
    ): Long = withContext(Dispatchers.IO) {
        val routineId = if (routine.id == 0L) {
            routineDao.insertRoutine(routine)
        } else {
            routineDao.updateRoutine(routine)
            routine.id
        }
        val itemsWithId = exercises.mapIndexed { idx, item ->
            item.copy(routineId = routineId, orderIndex = idx)
        }
        routineDao.replaceRoutineExercises(routineId, itemsWithId)
        routineId
    }

    suspend fun duplicateRoutine(routineId: Long): Long = withContext(Dispatchers.IO) {
        val detail = getRoutineDetail(routineId) ?: return@withContext -1L
        val copyRoutine = WorkoutRoutineEntity(
            name = "${detail.routine.name} (Copy)",
            description = detail.routine.description,
            isTemplate = false
        )
        val newRoutineId = routineDao.insertRoutine(copyRoutine)
        val copyExercises = detail.items.map { item ->
            item.routineExercise.copy(id = 0, routineId = newRoutineId)
        }
        routineDao.insertRoutineExercises(copyExercises)
        newRoutineId
    }

    suspend fun deleteRoutine(routineId: Long) = withContext(Dispatchers.IO) {
        val routine = routineDao.getRoutineById(routineId) ?: return@withContext
        routineDao.deleteRoutine(routine)
    }

    // --- Personal Records ---
    suspend fun getAllPersonalRecords(): List<PersonalRecordItem> = withContext(Dispatchers.IO) {
        val allCompletedSets = sessionDao.getAllCompletedSets()
        val allExercises = exerciseDao.getAllExercises().firstOrNull() ?: emptyList()
        val exerciseMap = allExercises.associateBy { it.id }
        val sessionExercises = sessionDao.getAllSessionExercises().associateBy { it.id }

        val prMap = mutableMapOf<Long, PersonalRecordItem>()

        for (set in allCompletedSets) {
            val se = sessionExercises[set.sessionExerciseId] ?: continue
            val exercise = exerciseMap[se.exerciseId] ?: continue
            val weight = set.weightKg ?: 0.0
            val reps = set.reps ?: 0
            if (weight <= 0.0 || reps <= 0) continue

            val currentPR = prMap[exercise.id]
            if (currentPR == null || weight > currentPR.maxWeightKg || (weight == currentPR.maxWeightKg && reps > currentPR.repsAtMaxWeight)) {
                prMap[exercise.id] = PersonalRecordItem(
                    exerciseId = exercise.id,
                    exerciseName = exercise.name,
                    muscleGroup = exercise.muscleGroup,
                    maxWeightKg = weight,
                    repsAtMaxWeight = reps,
                    estimated1RMKg = Calculators.estimate1RMEpley(weight, reps),
                    achievedDate = set.completedAt ?: System.currentTimeMillis()
                )
            }
        }

        prMap.values.sortedByDescending { it.maxWeightKg }
    }

    // --- Body Measurements ---
    fun getAllMeasurementsFlow(): Flow<List<BodyMeasurementEntity>> = measurementDao.getAllMeasurements()
    fun getLatestMeasurementFlow(): Flow<BodyMeasurementEntity?> = measurementDao.getLatestMeasurement()

    suspend fun addMeasurement(measurement: BodyMeasurementEntity): Long = withContext(Dispatchers.IO) {
        measurementDao.insertMeasurement(measurement)
    }

    suspend fun deleteMeasurement(id: Long) = withContext(Dispatchers.IO) {
        measurementDao.deleteMeasurementById(id)
    }
}
