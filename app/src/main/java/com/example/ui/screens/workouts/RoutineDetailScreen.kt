package com.example.ui.screens.workouts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.RoutineExerciseEntity
import com.example.data.local.entity.WorkoutRoutineEntity
import com.example.data.repository.RoutineDetail
import com.example.data.repository.WorkoutRepository
import com.example.ui.components.EmptyStateView
import kotlinx.coroutines.launch

data class RoutineItemEditState(
    val exercise: ExerciseEntity,
    var sets: Int = 3,
    var reps: Int = 10,
    var weightKg: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(
    routineId: Long,
    workoutRepository: WorkoutRepository,
    onNavigateBack: () -> Unit,
    onStartWorkout: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var routineName by remember { mutableStateOf("") }
    var routineDesc by remember { mutableStateOf("") }
    var isTemplate by remember { mutableStateOf(false) }
    var isPickerOpen by remember { mutableStateOf(false) }
    var availableExercises by remember { mutableStateOf<List<ExerciseEntity>>(emptyList()) }

    val items = remember { mutableStateListOf<RoutineItemEditState>() }

    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(routineId) {
        val detail = workoutRepository.getRoutineDetail(routineId)
        if (detail != null) {
            routineName = detail.routine.name
            routineDesc = detail.routine.description
            isTemplate = detail.routine.isTemplate
            items.clear()
            items.addAll(detail.items.map {
                RoutineItemEditState(
                    exercise = it.exercise,
                    sets = it.routineExercise.defaultSets,
                    reps = it.routineExercise.defaultReps,
                    weightKg = it.routineExercise.defaultWeightKg
                )
            })
        }
        availableExercises = workoutRepository.getAllExercisesList()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (routineName.isNotBlank()) routineName else "Routine Details",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("routine_detail_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val routine = WorkoutRoutineEntity(
                                    id = routineId,
                                    name = routineName.ifBlank { "Routine" },
                                    description = routineDesc,
                                    isTemplate = isTemplate
                                )
                                val entities = items.mapIndexed { idx, itm ->
                                    RoutineExerciseEntity(
                                        routineId = routineId,
                                        exerciseId = itm.exercise.id,
                                        orderIndex = idx,
                                        defaultSets = itm.sets,
                                        defaultReps = itm.reps,
                                        defaultWeightKg = itm.weightKg
                                    )
                                }
                                workoutRepository.saveRoutine(routine, entities)
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.testTag("save_routine_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.testTag("routine_detail_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Edit Header info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = routineName,
                            onValueChange = { routineName = it },
                            label = { Text("Routine Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("routine_name_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = routineDesc,
                            onValueChange = { routineDesc = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val session = workoutRepository.startWorkoutFromRoutine(routineId)
                                    onStartWorkout(session)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_workout_from_detail"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Workout Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Exercises (${items.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Button(
                        onClick = { isPickerOpen = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("routine_add_exercise_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Exercise")
                    }
                }
            }

            if (items.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Add,
                        title = "No exercises in routine",
                        description = "Add exercises to this plan to specify target sets, reps, and weights."
                    )
                }
            } else {
                itemsIndexed(items) { index, item ->
                    RoutineExerciseEditCard(
                        item = item,
                        index = index + 1,
                        onUpdate = { sets, reps, weight ->
                            items[index] = item.copy(sets = sets, reps = reps, weightKg = weight)
                        },
                        onDelete = { items.removeAt(index) }
                    )
                }
            }
        }
    }

    if (isPickerOpen) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { isPickerOpen = false },
            title = { Text("Add Exercise to Routine") },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableExercises) { ex ->
                        androidx.compose.material3.Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    items.add(RoutineItemEditState(exercise = ex))
                                    isPickerOpen = false
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(ex.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${ex.muscleGroup.displayName} • ${ex.equipment.displayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { isPickerOpen = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun RoutineExerciseEditCard(
    item: RoutineItemEditState,
    index: Int,
    onUpdate: (Int, Int, Double) -> Unit,
    onDelete: () -> Unit
) {
    var setsInput by remember(item.sets) { mutableStateOf(item.sets.toString()) }
    var repsInput by remember(item.reps) { mutableStateOf(item.reps.toString()) }
    var weightInput by remember(item.weightKg) { mutableStateOf(if (item.weightKg > 0) item.weightKg.toString() else "0") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$index. ${item.exercise.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = setsInput,
                    onValueChange = {
                        setsInput = it
                        onUpdate(it.toIntOrNull() ?: item.sets, repsInput.toIntOrNull() ?: item.reps, weightInput.toDoubleOrNull() ?: item.weightKg)
                    },
                    label = { Text("Sets") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = repsInput,
                    onValueChange = {
                        repsInput = it
                        onUpdate(setsInput.toIntOrNull() ?: item.sets, it.toIntOrNull() ?: item.reps, weightInput.toDoubleOrNull() ?: item.weightKg)
                    },
                    label = { Text("Reps") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = weightInput,
                    onValueChange = {
                        weightInput = it
                        onUpdate(setsInput.toIntOrNull() ?: item.sets, repsInput.toIntOrNull() ?: item.reps, it.toDoubleOrNull() ?: item.weightKg)
                    },
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}
