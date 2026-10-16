package com.example.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupManager
import com.example.data.backup.BackupPreview
import com.example.data.local.entity.LengthUnit
import com.example.data.local.entity.ThemeMode
import com.example.data.local.entity.WeightUnit
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val exportStatusMessage: String? = null,
    val importPreview: BackupPreview? = null,
    val importJsonContent: String? = null,
    val isImportPreviewDialogOpen: Boolean = false,
    val isClearDataDialogOpen: Boolean = false,
    val isPrivacyDialogOpen: Boolean = false
)

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow.collect { prefs ->
                _uiState.value = _uiState.value.copy(preferences = prefs)
            }
        }
    }

    fun setWeightUnit(unit: WeightUnit) {
        viewModelScope.launch {
            preferencesRepository.setWeightUnit(unit)
            // Synchronize length unit accordingly
            val length = if (unit == WeightUnit.KG) LengthUnit.CM else LengthUnit.INCH
            preferencesRepository.setLengthUnit(length)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setDefaultRestSeconds(seconds: Int) {
        viewModelScope.launch {
            preferencesRepository.setDefaultRestSeconds(seconds)
        }
    }

    fun setVibrate(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setVibrateOnTimerEnd(enabled)
        }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setKeepScreenOn(enabled)
        }
    }

    suspend fun getJsonBackupString(): String {
        return backupManager.generateJsonBackup()
    }

    suspend fun getWorkoutsCsvString(): String {
        return backupManager.generateWorkoutsCsv()
    }

    suspend fun getMeasurementsCsvString(): String {
        return backupManager.generateMeasurementsCsv()
    }

    fun writeTextToUri(context: Context, uri: Uri, content: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    OutputStreamWriter(os).use { writer ->
                        writer.write(content)
                        writer.flush()
                    }
                }
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.localizedMessage ?: "Failed to write file")
                }
            }
        }
    }

    fun readBackupFromUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sb = StringBuilder()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line = reader.readLine()
                        while (line != null) {
                            sb.append(line).append("\n")
                            line = reader.readLine()
                        }
                    }
                }
                val jsonString = sb.toString()
                val preview = backupManager.validateAndPreviewBackup(jsonString)
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        importPreview = preview,
                        importJsonContent = jsonString,
                        isImportPreviewDialogOpen = true
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        exportStatusMessage = "Failed to read backup: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun confirmRestore(clearExisting: Boolean, onDone: (Boolean, String) -> Unit) {
        val content = _uiState.value.importJsonContent ?: return
        dismissImportPreviewDialog()
        viewModelScope.launch {
            val result = backupManager.restoreBackup(content, clearExisting)
            if (result.isSuccess) {
                onDone(true, "Successfully restored ${result.getOrNull()} items.")
            } else {
                onDone(false, "Restore failed: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    fun dismissImportPreviewDialog() {
        _uiState.value = _uiState.value.copy(
            isImportPreviewDialogOpen = false,
            importPreview = null,
            importJsonContent = null
        )
    }

    fun openClearDataDialog() {
        _uiState.value = _uiState.value.copy(isClearDataDialogOpen = true)
    }

    fun dismissClearDataDialog() {
        _uiState.value = _uiState.value.copy(isClearDataDialogOpen = false)
    }

    fun confirmClearAllData(onDone: () -> Unit) {
        dismissClearDataDialog()
        viewModelScope.launch {
            backupManager.clearAllUserData()
            onDone()
        }
    }

    fun openPrivacyDialog() {
        _uiState.value = _uiState.value.copy(isPrivacyDialogOpen = true)
    }

    fun dismissPrivacyDialog() {
        _uiState.value = _uiState.value.copy(isPrivacyDialogOpen = false)
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(exportStatusMessage = null)
    }

    class Factory(
        private val preferencesRepository: UserPreferencesRepository,
        private val backupManager: BackupManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(preferencesRepository, backupManager) as T
        }
    }
}
