package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Workouts : Screen("workouts", "Workouts", Icons.Default.FitnessCenter)
    object History : Screen("history", "History", Icons.Default.History)
    object Progress : Screen("progress", "Progress", Icons.Default.ShowChart)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    // Sub-destinations
    object ActiveWorkout : Screen("active_workout", "Active Workout")
    object RoutineDetail : Screen("routine_detail/{routineId}", "Routine Details") {
        fun createRoute(routineId: Long) = "routine_detail/$routineId"
    }
    object ExerciseLibrary : Screen("exercises", "Exercise Library")
    object BodyMeasurements : Screen("measurements", "Body Measurements")
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Workouts,
    Screen.History,
    Screen.Progress,
    Screen.Settings
)
