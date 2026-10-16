package com.example.ui.screens.activeworkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.RestTimerManager
import com.example.core.UnitConverter
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.SetType
import com.example.data.local.entity.WeightUnit
import com.example.data.local.entity.WorkoutSetEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.WorkoutRepository
import com.example.data.repository.WorkoutSessionDetail
import com.example.data.repository.WorkoutSummaryData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ActiveWorkoutUiState(
    val sessionDetail: WorkoutSessionDetail? = null,
    val elapsedSeconds: Long = 0,
    val isPaused: Boolean = false,
    val availableExercises: List<ExerciseEntity> = emptyList(),
    val isExercisePickerOpen: Boolean = false,
    val isCancelConfirmationOpen: Boolean = false,
    val isFinishSummaryOpen: Boolean = false,
    val workoutSummary: WorkoutSummaryData? = null,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val defaultRestSeconds: Int = 90,
    val workoutNotes: String = "",
    val workoutTitle: String = ""
)

class ActiveWorkoutViewModel(
    private val workoutRepository: WorkoutRepository,
    private val preferencesRepository: UserPreferencesRepository,
    val restTimerManager: RestTimerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveWorkoutUiState())
    val uiState: StateFlow<ActiveWorkoutUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var baseStartTime: Long = 0L
    private var accumulatedPausedSeconds: Long = 0L
    private var pauseStartTime: Long = 0L

    init {
        loadSession()
        loadAvailableExercises()
    }

    private fun loadSession() {
        viewModelScope.launch {
            combine(
                workoutRepository.getActiveSessionDetailFlow(),
                preferencesRepository.userPreferencesFlow
            ) { detail, preferences ->
                Pair(detail, preferences)
            }.collect { (detail, preferences) ->
                if (detail != null) {
                    if (baseStartTime == 0L) {
                        baseStartTime = detail.session.startTime
                        startTimerLoop()
                    }
                    _uiState.value = _uiState.value.copy(
                        sessionDetail = detail,
                        weightUnit = preferences.weightUnit,
                        defaultRestSeconds = preferences.defaultRestSeconds,
                        workoutNotes = detail.session.notes,
                        workoutTitle = detail.session.name
                    )
                } else {
                    _uiState.value = _uiState.value.copy(sessionDetail = null)
                }
            }
        }
    }

    private fun loadAvailableExercises() {
        viewModelScope.launch {
            workoutRepository.getAllExercisesFlow().collect { list ->
                _uiState.value = _uiState.value.copy(availableExercises = list)
            }
        }
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                if (!_uiState.value.isPaused && baseStartTime > 0L) {
                    val now = System.currentTimeMillis()
                    val totalElapsed = ((now - baseStartTime) / 1000) - accumulatedPausedSeconds
                    _uiState.value = _uiState.value.copy(
                        elapsedSeconds = totalElapsed.coerceAtLeast(0)
                    )
                }
                delay(1000)
            }
        }
    }

    fun togglePause() {
        val currentlyPaused = _uiState.value.isPaused
        if (!currentlyPaused) {
            pauseStartTime = System.currentTimeMillis()
            _uiState.value = _uiState.value.copy(isPaused = true)
        } else {
            if (pauseStartTime > 0L) {
                val pausedDuration = (System.currentTimeMillis() - pauseStartTime) / 1000
                accumulatedPausedSeconds += pausedDuration
                pauseStartTime = 0L
            }
            _uiState.value = _uiState.value.copy(isPaused = false)
        }
    }

    fun updateWorkoutTitle(newTitle: String) {
        val detail = _uiState.value.sessionDetail ?: return
        _uiState.value = _uiState.value.copy(workoutTitle = newTitle)
        viewModelScope.launch {
            workoutRepository.updateSession(detail.session.id, newTitle, _uiState.value.workoutNotes)
        }
    }

    fun updateWorkoutNotes(notes: String) {
        val detail = _uiState.value.sessionDetail ?: return
        _uiState.value = _uiState.value.copy(workoutNotes = notes)
        viewModelScope.launch {
            workoutRepository.updateSession(detail.session.id, _uiState.value.workoutTitle, notes)
        }
    }

    fun openExercisePicker() {
        _uiState.value = _uiState.value.copy(isExercisePickerOpen = true)
    }

    fun closeExercisePicker() {
        _uiState.value = _uiState.value.copy(isExercisePickerOpen = false)
    }

    fun addExercise(exerciseId: Long) {
        closeExercisePicker()
        viewModelScope.launch {
            workoutRepository.addExerciseToActiveSession(exerciseId)
        }
    }

    fun removeExercise(sessionExerciseId: Long) {
        viewModelScope.launch {
            workoutRepository.removeExerciseFromSession(sessionExerciseId)
        }
    }

    fun addSet(sessionExerciseId: Long) {
        viewModelScope.launch {
            workoutRepository.addSetToExercise(sessionExerciseId)
        }
    }

    fun removeSet(setId: Long) {
        viewModelScope.launch {
            workoutRepository.removeSet(setId)
        }
    }

    fun updateSet(set: WorkoutSetEntity) {
        viewModelScope.launch {
            workoutRepository.updateSet(set)
        }
    }

    fun toggleSetCompleted(set: WorkoutSetEntity) {
        val newStatus = !set.isCompleted
        val updated = set.copy(
            isCompleted = newStatus,
            completedAt = if (newStatus) System.currentTimeMillis() else null
        )
        updateSet(updated)

        // Automatically start rest timer when set is checked completed!
        if (newStatus) {
            restTimerManager.startTimer(_uiState.value.defaultRestSeconds)
        }
    }

    fun changeSetType(set: WorkoutSetEntity, newType: SetType) {
        updateSet(set.copy(setType = newType))
    }

    fun updateSetValues(
        set: WorkoutSetEntity,
        weightDisplayInput: String,
        repsInput: String,
        durationInput: String,
        distanceInput: String
    ) {
        val weightVal = weightDisplayInput.toDoubleOrNull()
        val repsVal = repsInput.toIntOrNull()
        val durationVal = durationInput.toIntOrNull()
        val distVal = distanceInput.toDoubleOrNull()

        val weightKg = if (weightVal != null && weightVal >= 0.0) {
            UnitConverter.toStoredWeightKg(weightVal, _uiState.value.weightUnit)
        } else set.weightKg

        val updated = set.copy(
            weightKg = weightKg,
            reps = if (repsVal != null && repsVal >= 0) repsVal else set.reps,
            durationSeconds = if (durationVal != null && durationVal >= 0) durationVal else set.durationSeconds,
            distanceMeters = if (distVal != null && distVal >= 0.0) distVal else set.distanceMeters
        )
        updateSet(updated)
    }

    fun requestCancelWorkout() {
        _uiState.value = _uiState.value.copy(isCancelConfirmationOpen = true)
    }

    fun dismissCancelConfirmation() {
        _uiState.value = _uiState.value.copy(isCancelConfirmationOpen = false)
    }

    fun confirmCancelWorkout(onCancelled: () -> Unit) {
        val detail = _uiState.value.sessionDetail ?: return
        dismissCancelConfirmation()
        viewModelScope.launch {
            workoutRepository.cancelActiveWorkout(detail.session.id)
            restTimerManager.dismissTimer()
            onCancelled()
        }
    }

    fun finishWorkout(onFinished: () -> Unit) {
        val detail = _uiState.value.sessionDetail ?: return
        viewModelScope.launch {
            val summary = workoutRepository.finishWorkout(
                sessionId = detail.session.id,
                finalDurationSeconds = _uiState.value.elapsedSeconds,
                notes = _uiState.value.workoutNotes
            )
            restTimerManager.dismissTimer()
            _uiState.value = _uiState.value.copy(
                workoutSummary = summary,
                isFinishSummaryOpen = true
            )
        }
    }

    fun dismissFinishSummary(onDismissed: () -> Unit) {
        _uiState.value = _uiState.value.copy(isFinishSummaryOpen = false)
        onDismissed()
    }

    class Factory(
        private val workoutRepository: WorkoutRepository,
        private val preferencesRepository: UserPreferencesRepository,
        private val restTimerManager: RestTimerManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ActiveWorkoutViewModel(workoutRepository, preferencesRepository, restTimerManager) as T
        }
    }
}
