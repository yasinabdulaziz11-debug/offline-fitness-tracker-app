package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupManager
import com.example.data.local.AppDatabase
import com.example.data.local.entity.Equipment
import com.example.data.local.entity.ExerciseCategory
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.MuscleGroup
import com.example.data.local.entity.SetType
import com.example.data.local.entity.TrackingType
import com.example.data.local.entity.WorkoutSetEntity
import com.example.data.repository.WorkoutRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: WorkoutRepository
    private lateinit var backupManager: BackupManager

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = WorkoutRepository(db)
        backupManager = BackupManager(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Offline Fitness", appName)
    }

    @Test
    fun `create exercise and workout lifecycle`() = runBlocking {
        // 1. Insert exercise
        val exId = repository.insertCustomExercise(
            ExerciseEntity(
                name = "Bench Press",
                muscleGroup = MuscleGroup.CHEST,
                equipment = Equipment.BARBELL,
                category = ExerciseCategory.STRENGTH,
                trackingType = TrackingType.WEIGHT_AND_REPS
            )
        )
        assertTrue(exId > 0)

        // 2. Start workout
        val sessionId = repository.startWorkout("Push Day")
        assertTrue(sessionId > 0)

        // 3. Add exercise to session
        val sessionExId = repository.addExerciseToActiveSession(exId)
        assertTrue(sessionExId > 0)

        // 4. Update set
        val sets = db.workoutSessionDao().getSetsForExercise(sessionExId)
        assertEquals(1, sets.size)

        val set = sets.first()
        repository.updateSet(
            set.copy(
                weightKg = 100.0,
                reps = 10,
                isCompleted = true,
                completedAt = System.currentTimeMillis()
            )
        )

        // 5. Finish workout
        val summary = repository.finishWorkout(sessionId, 2400, "Great workout")
        assertEquals("Push Day", summary.workoutName)
        assertEquals(1, summary.completedSetsCount)
        assertEquals(10, summary.totalReps)
        assertEquals(1000.0, summary.totalVolumeKg, 0.01)

        // 6. Verify Personal Record
        val prs = repository.getAllPersonalRecords()
        assertEquals(1, prs.size)
        assertEquals("Bench Press", prs.first().exerciseName)
        assertEquals(100.0, prs.first().maxWeightKg, 0.01)
        assertEquals(10, prs.first().repsAtMaxWeight)
    }

    @Test
    fun `export and restore backup round-trip`() = runBlocking {
        // Add custom exercise
        val exId = repository.insertCustomExercise(
            ExerciseEntity(
                name = "Incline Dumbbell Press",
                muscleGroup = MuscleGroup.CHEST,
                equipment = Equipment.DUMBBELL,
                category = ExerciseCategory.HYPERTROPHY
            )
        )
        val sessionId = repository.startWorkout("Chest Session")
        val seId = repository.addExerciseToActiveSession(exId)
        val sets = db.workoutSessionDao().getSetsForExercise(seId)
        repository.updateSet(sets.first().copy(weightKg = 30.0, reps = 12, isCompleted = true))
        repository.finishWorkout(sessionId, 1800, "Notes")

        // Generate JSON
        val jsonBackup = backupManager.generateJsonBackup()
        assertTrue(jsonBackup.contains("Incline Dumbbell Press"))
        assertTrue(jsonBackup.contains("Chest Session"))

        // Validate
        val preview = backupManager.validateAndPreviewBackup(jsonBackup)
        assertTrue(preview.valid)
        assertEquals(1, preview.customExercisesCount)
        assertEquals(1, preview.workoutsCount)

        // Wipe and restore
        val restoreResult = backupManager.restoreBackup(jsonBackup, clearExisting = true)
        assertTrue(restoreResult.isSuccess)

        val completed = db.workoutSessionDao().getAllCompletedSets()
        assertTrue(completed.isNotEmpty())
    }
}
