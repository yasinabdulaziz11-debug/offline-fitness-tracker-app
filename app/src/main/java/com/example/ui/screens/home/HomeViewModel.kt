package com.example.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.Calculators
import com.example.data.local.entity.WeightUnit
import com.example.data.local.entity.WorkoutRoutineEntity
import com.example.data.local.entity.WorkoutSessionEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.PersonalRecordItem
import com.example.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val activeSession: WorkoutSessionEntity? = null,
    val totalWorkoutsThisWeek: Int = 0,
    val totalDurationSecondsThisWeek: Long = 0,
    val currentDayStreak: Int = 0,
    val recentWorkouts: List<WorkoutSessionEntity> = emptyList(),
    val recentPRs: List<PersonalRecordItem> = emptyList(),
    val templates: List<WorkoutRoutineEntity> = emptyList(),
    val todayCompletedWorkout: WorkoutSessionEntity? = null,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val workoutRepository: WorkoutRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                workoutRepository.activeSessionFlow,
                workoutRepository.getCompletedSessionsFlow(),
                workoutRepository.getAllRoutinesFlow(),
                preferencesRepository.userPreferencesFlow
            ) { activeSession, completedSessions, routines, preferences ->
                val now = System.currentTimeMillis()
                val timestamps = completedSessions.map { it.startTime }

                val weekCount = Calculators.countWorkoutsThisWeek(timestamps, now)
                val dayStreak = Calculators.calculateDayStreak(timestamps, now)

                // Calculate week duration
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = now
                    firstDayOfWeek = Calendar.MONDAY
                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfWeek = calendar.timeInMillis
                val weekDuration = completedSessions
                    .filter { it.startTime >= startOfWeek }
                    .sumOf { it.durationSeconds }

                // Check today's workout
                calendar.timeInMillis = now
                val todayYear = calendar.get(Calendar.YEAR)
                val todayDay = calendar.get(Calendar.DAY_OF_YEAR)

                val todayWorkout = completedSessions.firstOrNull { s ->
                    calendar.timeInMillis = s.startTime
                    calendar.get(Calendar.YEAR) == todayYear && calendar.get(Calendar.DAY_OF_YEAR) == todayDay
                }

                val templates = routines.filter { it.isTemplate }
                val recent = completedSessions.take(3)
                val prs = workoutRepository.getAllPersonalRecords().take(4)

                HomeUiState(
                    activeSession = activeSession,
                    totalWorkoutsThisWeek = weekCount,
                    totalDurationSecondsThisWeek = weekDuration,
                    currentDayStreak = dayStreak,
                    recentWorkouts = recent,
                    recentPRs = prs,
                    templates = templates,
                    todayCompletedWorkout = todayWorkout,
                    weightUnit = preferences.weightUnit,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun startQuickWorkout(onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val id = workoutRepository.startWorkout("Quick Workout")
            onStarted(id)
        }
    }

    fun startFromRoutine(routineId: Long, onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val id = workoutRepository.startWorkoutFromRoutine(routineId)
            onStarted(id)
        }
    }

    class Factory(
        private val workoutRepository: WorkoutRepository,
        private val preferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(workoutRepository, preferencesRepository) as T
        }
    }
}
