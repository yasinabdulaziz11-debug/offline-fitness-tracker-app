package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ExerciseDao
import com.example.data.local.dao.MeasurementDao
import com.example.data.local.dao.RoutineDao
import com.example.data.local.dao.WorkoutSessionDao
import com.example.data.local.entity.BodyMeasurementEntity
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.RoutineExerciseEntity
import com.example.data.local.entity.SessionExerciseEntity
import com.example.data.local.entity.WorkoutRoutineEntity
import com.example.data.local.entity.WorkoutSessionEntity
import com.example.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutRoutineEntity::class,
        RoutineExerciseEntity::class,
        WorkoutSessionEntity::class,
        SessionExerciseEntity::class,
        WorkoutSetEntity::class,
        BodyMeasurementEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineDao(): RoutineDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun measurementDao(): MeasurementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sessions_startTime` ON `workout_sessions` (`startTime`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sessions_routineId` ON `workout_sessions` (`routineId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sets_completedAt` ON `workout_sets` (`completedAt`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_body_measurements_timestamp` ON `body_measurements` (`timestamp`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_routines_createdAt` ON `workout_routines` (`createdAt`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercises_muscleGroup` ON `exercises` (`muscleGroup`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercises_category` ON `exercises` (`category`)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "offline_fitness.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            database.exerciseDao().insertAll(InitialData.defaultExercises)
            for (routine in InitialData.defaultRoutines) {
                database.routineDao().insertRoutine(routine)
            }
            database.routineDao().insertRoutineExercises(InitialData.defaultRoutineExercises)
        }
    }
}
