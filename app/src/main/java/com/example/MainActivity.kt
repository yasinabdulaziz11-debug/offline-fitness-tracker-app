package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.RestTimerManager
import com.example.data.backup.BackupManager
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.WorkoutRepository
import com.example.ui.navigation.Screen
import com.example.ui.navigation.bottomNavScreens
import com.example.ui.screens.activeworkout.ActiveWorkoutScreen
import com.example.ui.screens.activeworkout.ActiveWorkoutViewModel
import com.example.ui.screens.exercises.ExerciseLibraryScreen
import com.example.ui.screens.exercises.ExerciseLibraryViewModel
import com.example.ui.screens.history.HistoryScreen
import com.example.ui.screens.history.HistoryViewModel
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.progress.BodyMeasurementsScreen
import com.example.ui.screens.progress.ProgressScreen
import com.example.ui.screens.progress.ProgressViewModel
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.workouts.RoutineDetailScreen
import com.example.ui.screens.workouts.WorkoutsScreen
import com.example.ui.screens.workouts.WorkoutsViewModel
import com.example.ui.theme.OfflineFitnessTheme

class MainActivity : ComponentActivity() {

    private lateinit var restTimerManager: RestTimerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val workoutRepository = WorkoutRepository(database)
        val preferencesRepository = UserPreferencesRepository(applicationContext)
        val backupManager = BackupManager(database)
        restTimerManager = RestTimerManager(applicationContext)

        setContent {
            val userPrefs by preferencesRepository.userPreferencesFlow.collectAsStateWithLifecycle(
                initialValue = com.example.data.preferences.UserPreferences()
            )

            OfflineFitnessTheme(themeMode = userPrefs.themeMode) {
                FitnessAppMain(
                    workoutRepository = workoutRepository,
                    preferencesRepository = preferencesRepository,
                    backupManager = backupManager,
                    restTimerManager = restTimerManager
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (::restTimerManager.isInitialized) {
            restTimerManager.dismissTimer()
        }
    }
}

@Composable
fun FitnessAppMain(
    workoutRepository: WorkoutRepository,
    preferencesRepository: UserPreferencesRepository,
    backupManager: BackupManager,
    restTimerManager: RestTimerManager
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelDestination = bottomNavScreens.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isTopLevelDestination) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    bottomNavScreens.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                screen.icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Home Destination
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(workoutRepository, preferencesRepository)
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToActiveWorkout = { navController.navigate(Screen.ActiveWorkout.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToProgress = { navController.navigate(Screen.Progress.route) },
                    onNavigateToWorkouts = { navController.navigate(Screen.Workouts.route) }
                )
            }

            // Workouts & Routines
            composable(Screen.Workouts.route) {
                val workoutsViewModel: WorkoutsViewModel = viewModel(
                    factory = WorkoutsViewModel.Factory(workoutRepository)
                )
                WorkoutsScreen(
                    viewModel = workoutsViewModel,
                    onNavigateToActiveWorkout = { navController.navigate(Screen.ActiveWorkout.route) },
                    onNavigateToRoutineDetail = { routineId ->
                        navController.navigate(Screen.RoutineDetail.createRoute(routineId))
                    },
                    onNavigateToExercises = { navController.navigate(Screen.ExerciseLibrary.route) }
                )
            }

            // History
            composable(Screen.History.route) {
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModel.Factory(workoutRepository, preferencesRepository)
                )
                HistoryScreen(
                    viewModel = historyViewModel,
                    onNavigateToWorkouts = { navController.navigate(Screen.Workouts.route) }
                )
            }

            // Progress
            composable(Screen.Progress.route) {
                val progressViewModel: ProgressViewModel = viewModel(
                    factory = ProgressViewModel.Factory(workoutRepository, preferencesRepository)
                )
                ProgressScreen(
                    viewModel = progressViewModel,
                    onNavigateToMeasurements = { navController.navigate(Screen.BodyMeasurements.route) }
                )
            }

            // Settings
            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(preferencesRepository, backupManager)
                )
                SettingsScreen(viewModel = settingsViewModel)
            }

            // Active Workout Sub-screen
            composable(Screen.ActiveWorkout.route) {
                val activeWorkoutViewModel: ActiveWorkoutViewModel = viewModel(
                    factory = ActiveWorkoutViewModel.Factory(workoutRepository, preferencesRepository, restTimerManager)
                )
                ActiveWorkoutScreen(
                    viewModel = activeWorkoutViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Routine Detail Sub-screen
            composable(
                route = Screen.RoutineDetail.route,
                arguments = listOf(navArgument("routineId") { type = NavType.LongType })
            ) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getLong("routineId") ?: 0L
                RoutineDetailScreen(
                    routineId = routineId,
                    workoutRepository = workoutRepository,
                    onNavigateBack = { navController.popBackStack() },
                    onStartWorkout = { navController.navigate(Screen.ActiveWorkout.route) }
                )
            }

            // Exercise Library Sub-screen
            composable(Screen.ExerciseLibrary.route) {
                val exerciseViewModel: ExerciseLibraryViewModel = viewModel(
                    factory = ExerciseLibraryViewModel.Factory(workoutRepository)
                )
                ExerciseLibraryScreen(
                    viewModel = exerciseViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Body Measurements Sub-screen
            composable(Screen.BodyMeasurements.route) {
                val progressViewModel: ProgressViewModel = viewModel(
                    factory = ProgressViewModel.Factory(workoutRepository, preferencesRepository)
                )
                BodyMeasurementsScreen(
                    viewModel = progressViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
