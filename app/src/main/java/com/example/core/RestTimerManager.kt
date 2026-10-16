package com.example.core

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RestTimerUiState(
    val totalSeconds: Int = 90,
    val remainingSeconds: Int = 0,
    val isRunning: Boolean = false,
    val isVisible: Boolean = false
)

class RestTimerManager(private val context: Context) {

    private val _timerState = MutableStateFlow(RestTimerUiState())
    val timerState: StateFlow<RestTimerUiState> = _timerState.asStateFlow()

    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    fun startTimer(seconds: Int) {
        timerJob?.cancel()
        _timerState.value = RestTimerUiState(
            totalSeconds = seconds,
            remainingSeconds = seconds,
            isRunning = true,
            isVisible = true
        )

        timerJob = scope.launch {
            while (_timerState.value.remainingSeconds > 0 && _timerState.value.isRunning) {
                delay(1000)
                val newRemaining = _timerState.value.remainingSeconds - 1
                if (newRemaining <= 0) {
                    _timerState.value = _timerState.value.copy(
                        remainingSeconds = 0,
                        isRunning = false
                    )
                    triggerVibration()
                    break
                } else {
                    _timerState.value = _timerState.value.copy(remainingSeconds = newRemaining)
                }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(isRunning = false)
    }

    fun resumeTimer() {
        val remaining = _timerState.value.remainingSeconds
        if (remaining > 0) {
            startTimer(remaining)
        }
    }

    fun addTime(secondsToAdd: Int) {
        val currentRemaining = _timerState.value.remainingSeconds
        val currentTotal = _timerState.value.totalSeconds
        val newRemaining = (currentRemaining + secondsToAdd).coerceAtLeast(0)
        val newTotal = (currentTotal + secondsToAdd).coerceAtLeast(newRemaining)

        _timerState.value = _timerState.value.copy(
            remainingSeconds = newRemaining,
            totalSeconds = newTotal
        )
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(
            remainingSeconds = _timerState.value.totalSeconds,
            isRunning = false
        )
    }

    fun dismissTimer() {
        timerJob?.cancel()
        _timerState.value = RestTimerUiState(
            totalSeconds = _timerState.value.totalSeconds,
            remainingSeconds = 0,
            isRunning = false,
            isVisible = false
        )
    }

    private fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
        } catch (_: Exception) {
            // Graceful fallback if device lacks vibrator or permission
        }
    }
}
