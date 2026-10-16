package com.example.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.WeightUnit
import com.example.data.local.entity.WorkoutSessionEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.WorkoutRepository
import com.example.data.repository.WorkoutSessionDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HistoryUiState(
    val sessions: List<WorkoutSessionEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedDetail: WorkoutSessionDetail? = null,
    val isDetailDialogOpen: Boolean = false,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val isLoading: Boolean = true
)

class HistoryViewModel(
    private val workoutRepository: WorkoutRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            combine(
                workoutRepository.getCompletedSessionsFlow(),
                preferencesRepository.userPreferencesFlow
            ) { list, prefs ->
                Pair(list, prefs)
            }.collect { (list, prefs) ->
                _uiState.value = _uiState.value.copy(
                    sessions = list,
                    weightUnit = prefs.weightUnit,
                    isLoading = false
                )
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun openSessionDetail(sessionId: Long) {
        viewModelScope.launch {
            val detail = workoutRepository.getSessionDetail(sessionId)
            _uiState.value = _uiState.value.copy(
                selectedDetail = detail,
                isDetailDialogOpen = true
            )
        }
    }

    fun closeSessionDetail() {
        _uiState.value = _uiState.value.copy(
            selectedDetail = null,
            isDetailDialogOpen = false
        )
    }

    fun updateSessionNotes(sessionId: Long, name: String, notes: String) {
        viewModelScope.launch {
            workoutRepository.updateSession(sessionId, name, notes)
            // Reload detail if open
            val updatedDetail = workoutRepository.getSessionDetail(sessionId)
            _uiState.value = _uiState.value.copy(selectedDetail = updatedDetail)
        }
    }

    fun deleteSession(sessionId: Long) {
        closeSessionDetail()
        viewModelScope.launch {
            workoutRepository.deleteSession(sessionId)
        }
    }

    class Factory(
        private val workoutRepository: WorkoutRepository,
        private val preferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(workoutRepository, preferencesRepository) as T
        }
    }
}
