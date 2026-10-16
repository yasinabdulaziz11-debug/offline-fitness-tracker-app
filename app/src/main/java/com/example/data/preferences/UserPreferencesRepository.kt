package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.entity.LengthUnit
import com.example.data.local.entity.ThemeMode
import com.example.data.local.entity.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fitness_settings")

data class UserPreferences(
    val weightUnit: WeightUnit = WeightUnit.KG,
    val lengthUnit: LengthUnit = LengthUnit.CM,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultRestSeconds: Int = 90,
    val vibrateOnTimerEnd: Boolean = true,
    val keepScreenOn: Boolean = true
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val LENGTH_UNIT = stringPreferencesKey("length_unit")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DEFAULT_REST_SECONDS = intPreferencesKey("default_rest_seconds")
        val VIBRATE_TIMER = booleanPreferencesKey("vibrate_timer")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val weightUnitStr = preferences[PreferencesKeys.WEIGHT_UNIT] ?: WeightUnit.KG.name
        val lengthUnitStr = preferences[PreferencesKeys.LENGTH_UNIT] ?: LengthUnit.CM.name
        val themeModeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val restSeconds = preferences[PreferencesKeys.DEFAULT_REST_SECONDS] ?: 90
        val vibrate = preferences[PreferencesKeys.VIBRATE_TIMER] ?: true
        val keepScreen = preferences[PreferencesKeys.KEEP_SCREEN_ON] ?: true

        UserPreferences(
            weightUnit = runCatching { WeightUnit.valueOf(weightUnitStr) }.getOrDefault(WeightUnit.KG),
            lengthUnit = runCatching { LengthUnit.valueOf(lengthUnitStr) }.getOrDefault(LengthUnit.CM),
            themeMode = runCatching { ThemeMode.valueOf(themeModeStr) }.getOrDefault(ThemeMode.SYSTEM),
            defaultRestSeconds = restSeconds,
            vibrateOnTimerEnd = vibrate,
            keepScreenOn = keepScreen
        )
    }

    suspend fun setWeightUnit(unit: WeightUnit) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIGHT_UNIT] = unit.name
        }
    }

    suspend fun setLengthUnit(unit: LengthUnit) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LENGTH_UNIT] = unit.name
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setDefaultRestSeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_REST_SECONDS] = seconds
        }
    }

    suspend fun setVibrateOnTimerEnd(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATE_TIMER] = enabled
        }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEEP_SCREEN_ON] = enabled
        }
    }
}
