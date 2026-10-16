package com.example.ui.screens.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.Equipment
import com.example.data.local.entity.ExerciseCategory
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.MuscleGroup
import com.example.data.local.entity.TrackingType
import com.example.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExerciseLibraryUiState(
    val exercises: List<ExerciseEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedMuscle: MuscleGroup? = null,
    val selectedEquipment: Equipment? = null,
    val isAddDialogOpen: Boolean = false,
    val selectedExerciseForDetail: ExerciseEntity? = null
)

class ExerciseLibraryViewModel(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseLibraryUiState())
    val uiState: StateFlow<ExerciseLibraryUiState> = _uiState.asStateFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            workoutRepository.getAllExercisesFlow().collect { list ->
                _uiState.value = _uiState.value.copy(exercises = list)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setSelectedMuscle(muscle: MuscleGroup?) {
        _uiState.value = _uiState.value.copy(selectedMuscle = muscle)
    }

    fun setSelectedEquipment(equipment: Equipment?) {
        _uiState.value = _uiState.value.copy(selectedEquipment = equipment)
    }

    fun openAddDialog() {
        _uiState.value = _uiState.value.copy(isAddDialogOpen = true)
    }

    fun closeAddDialog() {
        _uiState.value = _uiState.value.copy(isAddDialogOpen = false)
    }

    fun openExerciseDetail(exercise: ExerciseEntity) {
        _uiState.value = _uiState.value.copy(selectedExerciseForDetail = exercise)
    }

    fun closeExerciseDetail() {
        _uiState.value = _uiState.value.copy(selectedExerciseForDetail = null)
    }

    fun addCustomExercise(
        name: String,
        muscle: MuscleGroup,
        equipment: Equipment,
        category: ExerciseCategory,
        trackingType: TrackingType,
        instructions: String
    ) {
        closeAddDialog()
        viewModelScope.launch {
            workoutRepository.insertCustomExercise(
                ExerciseEntity(
                    name = name.trim(),
                    muscleGroup = muscle,
                    equipment = equipment,
                    category = category,
                    trackingType = trackingType,
                    instructions = instructions.trim(),
                    isCustom = true
                )
            )
        }
    }

    fun archiveExercise(exerciseId: Long) {
        closeExerciseDetail()
        viewModelScope.launch {
            workoutRepository.archiveExercise(exerciseId)
        }
    }

    class Factory(private val workoutRepository: WorkoutRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExerciseLibraryViewModel(workoutRepository) as T
        }
    }
}
