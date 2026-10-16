package com.example.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.Calculators
import com.example.core.TimeUtils
import com.example.data.local.entity.BodyMeasurementEntity
import com.example.data.local.entity.LengthUnit
import com.example.data.local.entity.WeightUnit
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.PersonalRecordItem
import com.example.data.repository.WorkoutRepository
import com.example.ui.components.ChartPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ProgressUiState(
    val personalRecords: List<PersonalRecordItem> = emptyList(),
    val volumeChartPoints: List<ChartPoint> = emptyList(),
    val latestMeasurement: BodyMeasurementEntity? = null,
    val measurements: List<BodyMeasurementEntity> = emptyList(),
    val totalWorkoutsCount: Int = 0,
    val totalVolumeLiftedKg: Double = 0.0,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val lengthUnit: LengthUnit = LengthUnit.CM,
    val isLoading: Boolean = true
)

class ProgressViewModel(
    private val workoutRepository: WorkoutRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        loadProgress()
    }

    private fun loadProgress() {
        viewModelScope.launch {
            combine(
                workoutRepository.getCompletedSessionsFlow(),
                workoutRepository.getAllMeasurementsFlow(),
                preferencesRepository.userPreferencesFlow
            ) { sessions, measurements, prefs ->
                val prs = workoutRepository.getAllPersonalRecords()

                // Calculate volume per session for the chart
                val chartPoints = sessions.take(10).reversed().mapNotNull { session ->
                    val detail = workoutRepository.getSessionDetail(session.id)
                    val sessionVolume = detail?.exercises?.sumOf { ex ->
                        ex.sets.filter { it.isCompleted }.sumOf { st ->
                            val w = st.weightKg ?: 0.0
                            val r = st.reps ?: 0
                            if (w > 0 && r > 0) w * r else 0.0
                        }
                    } ?: 0.0
                    if (sessionVolume > 0) {
                        ChartPoint(
                            label = TimeUtils.formatDateShort(session.startTime),
                            value = sessionVolume.toFloat(),
                            timestamp = session.startTime
                        )
                    } else null
                }

                val totalVolume = chartPoints.sumOf { it.value.toDouble() }

                ProgressUiState(
                    personalRecords = prs,
                    volumeChartPoints = chartPoints,
                    latestMeasurement = measurements.firstOrNull(),
                    measurements = measurements,
                    totalWorkoutsCount = sessions.size,
                    totalVolumeLiftedKg = totalVolume,
                    weightUnit = prefs.weightUnit,
                    lengthUnit = prefs.lengthUnit,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun addMeasurement(
        weightKg: Double?,
        waistCm: Double?,
        chestCm: Double?,
        hipsCm: Double?,
        armsCm: Double?,
        thighsCm: Double?,
        notes: String
    ) {
        viewModelScope.launch {
            workoutRepository.addMeasurement(
                BodyMeasurementEntity(
                    timestamp = System.currentTimeMillis(),
                    weightKg = weightKg,
                    waistCm = waistCm,
                    chestCm = chestCm,
                    hipsCm = hipsCm,
                    armsCm = armsCm,
                    thighsCm = thighsCm,
                    notes = notes
                )
            )
        }
    }

    fun deleteMeasurement(id: Long) {
        viewModelScope.launch {
            workoutRepository.deleteMeasurement(id)
        }
    }

    class Factory(
        private val workoutRepository: WorkoutRepository,
        private val preferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProgressViewModel(workoutRepository, preferencesRepository) as T
        }
    }
}
