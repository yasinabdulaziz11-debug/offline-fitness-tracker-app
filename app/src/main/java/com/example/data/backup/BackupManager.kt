package com.example.data.backup

import com.example.core.TimeUtils
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BodyMeasurementEntity
import com.example.data.local.entity.Equipment
import com.example.data.local.entity.ExerciseCategory
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class BackupManager(private val database: AppDatabase) {

    private val exerciseDao = database.exerciseDao()
    private val routineDao = database.routineDao()
    private val sessionDao = database.workoutSessionDao()
    private val measurementDao = database.measurementDao()

    suspend fun generateJsonBackup(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("schemaVersion", 1)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("appVersion", "1.0")
        root.put("appName", "Offline Fitness Tracker")

        // 1. Custom exercises
        val allExercises = exerciseDao.getAllExercises().firstOrNull() ?: emptyList()
        val customExercises = allExercises.filter { it.isCustom }
        val exArray = JSONArray()
        for (ex in customExercises) {
            val exObj = JSONObject().apply {
                put("name", ex.name)
                put("muscleGroup", ex.muscleGroup.name)
                put("equipment", ex.equipment.name)
                put("category", ex.category.name)
                put("trackingType", ex.trackingType.name)
                put("instructions", ex.instructions)
            }
            exArray.put(exObj)
        }
        root.put("customExercises", exArray)

        // 2. User routines
        val allRoutines = routineDao.getAllRoutines().firstOrNull() ?: emptyList()
        val userRoutines = allRoutines.filter { !it.isTemplate }
        val routinesArray = JSONArray()
        for (r in userRoutines) {
            val rObj = JSONObject()
            rObj.put("name", r.name)
            rObj.put("description", r.description)
            rObj.put("createdAt", r.createdAt)

            val rItems = routineDao.getRoutineExercises(r.id)
            val itemsArray = JSONArray()
            for (item in rItems) {
                val ex = exerciseDao.getExerciseById(item.exerciseId)
                if (ex != null) {
                    val itemObj = JSONObject().apply {
                        put("exerciseName", ex.name)
                        put("orderIndex", item.orderIndex)
                        put("defaultSets", item.defaultSets)
                        put("defaultReps", item.defaultReps)
                        put("defaultWeightKg", item.defaultWeightKg)
                        put("defaultDurationSeconds", item.defaultDurationSeconds)
                    }
                    itemsArray.put(itemObj)
                }
            }
            rObj.put("exercises", itemsArray)
            routinesArray.put(rObj)
        }
        root.put("routines", routinesArray)

        // 3. Completed workout sessions
        val completedSessions = sessionDao.getCompletedSessions().firstOrNull() ?: emptyList()
        val sessionsArray = JSONArray()
        for (s in completedSessions) {
            val sObj = JSONObject().apply {
                put("name", s.name)
                put("startTime", s.startTime)
                put("endTime", s.endTime ?: s.startTime)
                put("durationSeconds", s.durationSeconds)
                put("notes", s.notes)
            }

            val sessionExercises = sessionDao.getSessionExercises(s.id)
            val seArray = JSONArray()
            for (se in sessionExercises) {
                val ex = exerciseDao.getExerciseById(se.exerciseId)
                val seObj = JSONObject().apply {
                    put("exerciseName", ex?.name ?: "Unknown")
                    put("orderIndex", se.orderIndex)
                    put("notes", se.notes)
                }
                val sets = sessionDao.getSetsForExercise(se.id)
                val setsArray = JSONArray()
                for (st in sets) {
                    val stObj = JSONObject().apply {
                        put("setNumber", st.setNumber)
                        put("setType", st.setType.name)
                        put("weightKg", st.weightKg ?: 0.0)
                        put("reps", st.reps ?: 0)
                        put("distanceMeters", st.distanceMeters ?: 0.0)
                        put("durationSeconds", st.durationSeconds ?: 0)
                        put("isCompleted", st.isCompleted)
                        put("completedAt", st.completedAt ?: 0L)
                    }
                    setsArray.put(stObj)
                }
                seObj.put("sets", setsArray)
                seArray.put(seObj)
            }
            sObj.put("exercises", seArray)
            sessionsArray.put(sObj)
        }
        root.put("completedSessions", sessionsArray)

        // 4. Body measurements
        val measurements = measurementDao.getAllMeasurementsList()
        val mArray = JSONArray()
        for (m in measurements) {
            val mObj = JSONObject().apply {
                put("timestamp", m.timestamp)
                put("weightKg", m.weightKg ?: JSONObject.NULL)
                put("waistCm", m.waistCm ?: JSONObject.NULL)
                put("chestCm", m.chestCm ?: JSONObject.NULL)
                put("hipsCm", m.hipsCm ?: JSONObject.NULL)
                put("armsCm", m.armsCm ?: JSONObject.NULL)
                put("thighsCm", m.thighsCm ?: JSONObject.NULL)
                put("notes", m.notes)
            }
            mArray.put(mObj)
        }
        root.put("measurements", mArray)

        root.toString(2)
    }

    suspend fun generateWorkoutsCsv(): String = withContext(Dispatchers.IO) {
        val completedSessions = sessionDao.getCompletedSessions().firstOrNull() ?: emptyList()
        val allExercises = exerciseDao.getAllExercises().firstOrNull() ?: emptyList()
        val exerciseMap = allExercises.associateBy { it.id }

        val sb = StringBuilder()
        sb.append("Workout Name,Date,Duration (Seconds),Exercise Name,Set Number,Set Type,Weight (kg),Reps,Distance (m),Duration (s),Completed\n")

        for (session in completedSessions) {
            val dateStr = TimeUtils.formatDate(session.startTime)
            val sessionExercises = sessionDao.getSessionExercises(session.id)
            for (se in sessionExercises) {
                val exName = exerciseMap[se.exerciseId]?.name ?: "Unknown"
                val sets = sessionDao.getSetsForExercise(se.id)
                for (st in sets) {
                    sb.append("\"${session.name.replace("\"", "\"\"")}\",")
                    sb.append("\"$dateStr\",")
                    sb.append("${session.durationSeconds},")
                    sb.append("\"${exName.replace("\"", "\"\"")}\",")
                    sb.append("${st.setNumber},")
                    sb.append("${st.setType.name},")
                    sb.append("${st.weightKg ?: ""},")
                    sb.append("${st.reps ?: ""},")
                    sb.append("${st.distanceMeters ?: ""},")
                    sb.append("${st.durationSeconds ?: ""},")
                    sb.append("${st.isCompleted}\n")
                }
            }
        }
        sb.toString()
    }

    suspend fun generateMeasurementsCsv(): String = withContext(Dispatchers.IO) {
        val measurements = measurementDao.getAllMeasurementsList()
        val sb = StringBuilder()
        sb.append("Date,Weight (kg),Waist (cm),Chest (cm),Hips (cm),Arms (cm),Thighs (cm),Notes\n")
        for (m in measurements) {
            sb.append("\"${TimeUtils.formatDate(m.timestamp)}\",")
            sb.append("${m.weightKg ?: ""},")
            sb.append("${m.waistCm ?: ""},")
            sb.append("${m.chestCm ?: ""},")
            sb.append("${m.hipsCm ?: ""},")
            sb.append("${m.armsCm ?: ""},")
            sb.append("${m.thighsCm ?: ""},")
            sb.append("\"${m.notes.replace("\"", "\"\"")}\"\n")
        }
        sb.toString()
    }

    fun validateAndPreviewBackup(jsonString: String): BackupPreview {
        return try {
            val root = JSONObject(jsonString)
            val version = root.optInt("schemaVersion", 0)
            if (version < 1) {
                return BackupPreview(false, 0, 0, 0, 0, 0, 0, "Invalid schema version or unsupported backup format")
            }
            val exportedAt = root.optLong("exportedAt", 0)
            val customExercisesCount = root.optJSONArray("customExercises")?.length() ?: 0
            val routinesCount = root.optJSONArray("routines")?.length() ?: 0
            val workoutsCount = root.optJSONArray("completedSessions")?.length() ?: 0
            val measurementsCount = root.optJSONArray("measurements")?.length() ?: 0

            BackupPreview(
                valid = true,
                schemaVersion = version,
                exportedAt = exportedAt,
                customExercisesCount = customExercisesCount,
                routinesCount = routinesCount,
                workoutsCount = workoutsCount,
                measurementsCount = measurementsCount
            )
        } catch (e: Exception) {
            BackupPreview(
                valid = false,
                schemaVersion = 0,
                exportedAt = 0,
                customExercisesCount = 0,
                routinesCount = 0,
                workoutsCount = 0,
                measurementsCount = 0,
                errorMessage = e.localizedMessage ?: "Corrupted JSON format"
            )
        }
    }

    suspend fun restoreBackup(jsonString: String, clearExisting: Boolean): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val preview = validateAndPreviewBackup(jsonString)
            if (!preview.valid) {
                return@withContext Result.failure(IllegalArgumentException(preview.errorMessage ?: "Invalid backup file"))
            }

            val root = JSONObject(jsonString)

            if (clearExisting) {
                clearAllUserData()
            }

            var totalRestored = 0

            // 1. Restore custom exercises
            val exArray = root.optJSONArray("customExercises")
            if (exArray != null) {
                for (i in 0 until exArray.length()) {
                    val obj = exArray.getJSONObject(i)
                    val name = obj.getString("name")
                    val muscle = runCatching { MuscleGroup.valueOf(obj.getString("muscleGroup")) }.getOrDefault(MuscleGroup.FULL_BODY)
                    val equip = runCatching { Equipment.valueOf(obj.getString("equipment")) }.getOrDefault(Equipment.OTHER)
                    val cat = runCatching { ExerciseCategory.valueOf(obj.getString("category")) }.getOrDefault(ExerciseCategory.STRENGTH)
                    val track = runCatching { TrackingType.valueOf(obj.getString("trackingType")) }.getOrDefault(TrackingType.WEIGHT_AND_REPS)
                    val instr = obj.optString("instructions", "")

                    exerciseDao.insertExercise(
                        ExerciseEntity(
                            name = name,
                            muscleGroup = muscle,
                            equipment = equip,
                            category = cat,
                            trackingType = track,
                            instructions = instr,
                            isCustom = true
                        )
                    )
                    totalRestored++
                }
            }

            // Refresh available exercises mapping
            val allExercises = exerciseDao.getAllExercises().firstOrNull() ?: emptyList()
            val exerciseByName = allExercises.associateBy { it.name.lowercase(Locale.ROOT) }

            // 2. Restore routines
            val rArray = root.optJSONArray("routines")
            if (rArray != null) {
                for (i in 0 until rArray.length()) {
                    val rObj = rArray.getJSONObject(i)
                    val routineName = rObj.getString("name")
                    val desc = rObj.optString("description", "")
                    val createdAt = rObj.optLong("createdAt", System.currentTimeMillis())

                    val routineId = routineDao.insertRoutine(
                        WorkoutRoutineEntity(
                            name = routineName,
                            description = desc,
                            createdAt = createdAt,
                            isTemplate = false
                        )
                    )

                    val itemsArray = rObj.optJSONArray("exercises")
                    if (itemsArray != null) {
                        val routineItems = mutableListOf<RoutineExerciseEntity>()
                        for (j in 0 until itemsArray.length()) {
                            val itemObj = itemsArray.getJSONObject(j)
                            val exName = itemObj.getString("exerciseName")
                            val matchedExercise = exerciseByName[exName.lowercase(Locale.ROOT)]
                            if (matchedExercise != null) {
                                routineItems.add(
                                    RoutineExerciseEntity(
                                        routineId = routineId,
                                        exerciseId = matchedExercise.id,
                                        orderIndex = itemObj.optInt("orderIndex", j),
                                        defaultSets = itemObj.optInt("defaultSets", 3),
                                        defaultReps = itemObj.optInt("defaultReps", 10),
                                        defaultWeightKg = itemObj.optDouble("defaultWeightKg", 0.0),
                                        defaultDurationSeconds = itemObj.optInt("defaultDurationSeconds", 0)
                                    )
                                )
                            }
                        }
                        routineDao.insertRoutineExercises(routineItems)
                    }
                    totalRestored++
                }
            }

            // 3. Restore completed sessions
            val sArray = root.optJSONArray("completedSessions")
            if (sArray != null) {
                for (i in 0 until sArray.length()) {
                    val sObj = sArray.getJSONObject(i)
                    val sName = sObj.getString("name")
                    val startTime = sObj.getLong("startTime")
                    val endTime = sObj.optLong("endTime", startTime)
                    val durationSeconds = sObj.optLong("durationSeconds", 0)
                    val notes = sObj.optString("notes", "")

                    val sessionId = sessionDao.insertSession(
                        WorkoutSessionEntity(
                            name = sName,
                            startTime = startTime,
                            endTime = endTime,
                            durationSeconds = durationSeconds,
                            notes = notes,
                            status = SessionStatus.COMPLETED
                        )
                    )

                    val seArray = sObj.optJSONArray("exercises")
                    if (seArray != null) {
                        for (j in 0 until seArray.length()) {
                            val seObj = seArray.getJSONObject(j)
                            val exName = seObj.getString("exerciseName")
                            var exercise = exerciseByName[exName.lowercase(Locale.ROOT)]
                            if (exercise == null) {
                                // Create custom exercise on the fly if not found
                                val newId = exerciseDao.insertExercise(
                                    ExerciseEntity(
                                        name = exName,
                                        muscleGroup = MuscleGroup.FULL_BODY,
                                        equipment = Equipment.OTHER,
                                        category = ExerciseCategory.STRENGTH,
                                        isCustom = true
                                    )
                                )
                                exercise = exerciseDao.getExerciseById(newId)
                            }
                            if (exercise != null) {
                                val sessionExerciseId = sessionDao.insertSessionExercise(
                                    SessionExerciseEntity(
                                        sessionId = sessionId,
                                        exerciseId = exercise.id,
                                        orderIndex = seObj.optInt("orderIndex", j),
                                        notes = seObj.optString("notes", "")
                                    )
                                )
                                val setsArray = seObj.optJSONArray("sets")
                                if (setsArray != null) {
                                    val sets = mutableListOf<WorkoutSetEntity>()
                                    for (k in 0 until setsArray.length()) {
                                        val stObj = setsArray.getJSONObject(k)
                                        val setType = runCatching { SetType.valueOf(stObj.getString("setType")) }.getOrDefault(SetType.NORMAL)
                                        val weight = if (stObj.has("weightKg") && !stObj.isNull("weightKg")) stObj.getDouble("weightKg") else null
                                        val reps = if (stObj.has("reps") && !stObj.isNull("reps")) stObj.getInt("reps") else null
                                        val dist = if (stObj.has("distanceMeters") && !stObj.isNull("distanceMeters")) stObj.getDouble("distanceMeters") else null
                                        val dur = if (stObj.has("durationSeconds") && !stObj.isNull("durationSeconds")) stObj.getInt("durationSeconds") else null

                                        sets.add(
                                            WorkoutSetEntity(
                                                sessionExerciseId = sessionExerciseId,
                                                setNumber = stObj.optInt("setNumber", k + 1),
                                                setType = setType,
                                                weightKg = weight,
                                                reps = reps,
                                                distanceMeters = dist,
                                                durationSeconds = dur,
                                                isCompleted = stObj.optBoolean("isCompleted", true),
                                                completedAt = stObj.optLong("completedAt", startTime)
                                            )
                                        )
                                    }
                                    sessionDao.insertSets(sets)
                                }
                            }
                        }
                    }
                    totalRestored++
                }
            }

            // 4. Restore measurements
            val mArray = root.optJSONArray("measurements")
            if (mArray != null) {
                val list = mutableListOf<BodyMeasurementEntity>()
                for (i in 0 until mArray.length()) {
                    val mObj = mArray.getJSONObject(i)
                    list.add(
                        BodyMeasurementEntity(
                            timestamp = mObj.getLong("timestamp"),
                            weightKg = if (mObj.has("weightKg") && !mObj.isNull("weightKg")) mObj.getDouble("weightKg") else null,
                            waistCm = if (mObj.has("waistCm") && !mObj.isNull("waistCm")) mObj.getDouble("waistCm") else null,
                            chestCm = if (mObj.has("chestCm") && !mObj.isNull("chestCm")) mObj.getDouble("chestCm") else null,
                            hipsCm = if (mObj.has("hipsCm") && !mObj.isNull("hipsCm")) mObj.getDouble("hipsCm") else null,
                            armsCm = if (mObj.has("armsCm") && !mObj.isNull("armsCm")) mObj.getDouble("armsCm") else null,
                            thighsCm = if (mObj.has("thighsCm") && !mObj.isNull("thighsCm")) mObj.getDouble("thighsCm") else null,
                            notes = mObj.optString("notes", "")
                        )
                    )
                }
                measurementDao.insertAll(list)
                totalRestored += list.size
            }

            Result.success(totalRestored)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllUserData() = withContext(Dispatchers.IO) {
        val completedSessions = sessionDao.getAllSessions().firstOrNull() ?: emptyList()
        for (s in completedSessions) {
            sessionDao.deleteSessionById(s.id)
        }
        val userRoutines = routineDao.getAllRoutines().firstOrNull() ?: emptyList()
        for (r in userRoutines) {
            if (!r.isTemplate) {
                routineDao.deleteRoutine(r)
            }
        }
        measurementDao.clearAll()
    }
}
