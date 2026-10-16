package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BodyMeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM body_measurements ORDER BY timestamp DESC")
    fun getAllMeasurements(): Flow<List<BodyMeasurementEntity>>

    @Query("SELECT * FROM body_measurements ORDER BY timestamp DESC")
    suspend fun getAllMeasurementsList(): List<BodyMeasurementEntity>

    @Query("SELECT * FROM body_measurements ORDER BY timestamp DESC LIMIT 1")
    fun getLatestMeasurement(): Flow<BodyMeasurementEntity?>

    @Query("SELECT * FROM body_measurements WHERE id = :id LIMIT 1")
    suspend fun getMeasurementById(id: Long): BodyMeasurementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: BodyMeasurementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(measurements: List<BodyMeasurementEntity>): List<Long>

    @Update
    suspend fun updateMeasurement(measurement: BodyMeasurementEntity)

    @Delete
    suspend fun deleteMeasurement(measurement: BodyMeasurementEntity)

    @Query("DELETE FROM body_measurements WHERE id = :id")
    suspend fun deleteMeasurementById(id: Long)

    @Query("DELETE FROM body_measurements")
    suspend fun clearAll()
}
