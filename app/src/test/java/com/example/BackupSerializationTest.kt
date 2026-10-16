package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupManager
import com.example.data.local.AppDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupSerializationTest {

    private lateinit var db: AppDatabase
    private lateinit var backupManager: BackupManager

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        backupManager = BackupManager(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testValidBackupValidation() {
        val validJson = """
            {
                "schemaVersion": 1,
                "exportedAt": 1700000000000,
                "appName": "Offline Fitness Tracker",
                "customExercises": [
                    { "name": "Custom Squat", "muscleGroup": "LEGS", "equipment": "BARBELL", "category": "STRENGTH", "trackingType": "WEIGHT_AND_REPS" }
                ],
                "routines": [
                    { "name": "Leg Routine", "description": "High volume", "exercises": [] }
                ],
                "completedSessions": [
                    { "name": "Leg Day", "startTime": 1700000000000, "durationSeconds": 3600, "exercises": [] }
                ],
                "measurements": [
                    { "timestamp": 1700000000000, "weightKg": 78.5 }
                ]
            }
        """.trimIndent()

        val preview = backupManager.validateAndPreviewBackup(validJson)
        assertTrue(preview.valid)
        assertEquals(1, preview.schemaVersion)
        assertEquals(1, preview.customExercisesCount)
        assertEquals(1, preview.routinesCount)
        assertEquals(1, preview.workoutsCount)
        assertEquals(1, preview.measurementsCount)
    }

    @Test
    fun testMalformedJsonBackupValidation() {
        val malformedJson = "{ schemaVersion: broken json... "
        val preview = backupManager.validateAndPreviewBackup(malformedJson)
        assertFalse(preview.valid)
    }

    @Test
    fun testMissingSchemaVersionValidation() {
        val noSchemaJson = """{ "items": [] }"""
        val preview = backupManager.validateAndPreviewBackup(noSchemaJson)
        assertFalse(preview.valid)
    }
}
