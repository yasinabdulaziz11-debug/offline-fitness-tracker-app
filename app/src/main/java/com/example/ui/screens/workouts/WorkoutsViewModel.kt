package com.example.ui.screens.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.WorkoutRoutineEntity
import com.example.data.repository.RoutineDetail
import com.example.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WorkoutsUiState(
    val routines: List<WorkoutRoutineEntity> = emptyList(),
    val templates: List<WorkoutRoutineEntity> = emptyList(),
    val isCreateRoutineDialogOpen: Boolean = false,
    val isLoading: Boolean = true
)

class WorkoutsViewModel(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutsUiState())
    val uiState: StateFlow<WorkoutsUiState> = _uiState.asStateFlow()

    init {
        loadRoutines()
    }

    private fun loadRoutines() {
        viewModelScope.launch {
            workoutRepository.getAllRoutinesFlow().collect { allRoutines ->
                val templates = allRoutines.filter { it.isTemplate }
                val userRoutines = allRoutines.filter { !it.isTemplate }
                _uiState.value = WorkoutsUiState(
                    routines = userRoutines,
                    templates = templates,
                    isLoading = false
                )
            }
        }
    }

    fun startWorkoutFromRoutine(routineId: Long, onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val sessionId = workoutRepository.startWorkoutFromRoutine(routineId)
            onStarted(sessionId)
        }
    }

    fun duplicateRoutine(routineId: Long) {
        viewModelScope.launch {
            workoutRepository.duplicateRoutine(routineId)
        }
    }

    fun deleteRoutine(routineId: Long) {
        viewModelScope.launch {
            workoutRepository.deleteRoutine(routineId)
        }
    }

    fun createRoutine(name: String, description: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val newRoutine = WorkoutRoutineEntity(
                name = name.ifBlank { "New Routine" },
                description = description,
                isTemplate = false
            )
            val id = workoutRepository.saveRoutine(newRoutine, emptyList())
            onCreated(id)
        }
    }

    class Factory(private val workoutRepository: WorkoutRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WorkoutsViewModel(workoutRepository) as T
        }
    }
}
